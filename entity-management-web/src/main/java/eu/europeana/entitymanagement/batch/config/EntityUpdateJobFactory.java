package eu.europeana.entitymanagement.batch.config;

import eu.europeana.entitymanagement.batch.listener.EntityUpdateStepListener;
import eu.europeana.entitymanagement.batch.listener.ScheduledTaskItemListener;
import eu.europeana.entitymanagement.batch.processor.EntityConsolidationProcessor;
import eu.europeana.entitymanagement.batch.processor.EntityDereferenceProcessor;
import eu.europeana.entitymanagement.batch.processor.EntityMetricsProcessor;
import eu.europeana.entitymanagement.batch.processor.EntityVerificationLogger;
import eu.europeana.entitymanagement.batch.reader.EntityRecordDatabaseReader;
import eu.europeana.entitymanagement.batch.service.ScheduledTaskService;
import eu.europeana.entitymanagement.batch.writer.EntityRecordDatabaseInsertionWriter;
import eu.europeana.entitymanagement.batch.writer.EntitySolrInsertionWriter;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledUpdateType;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static eu.europeana.entitymanagement.batch.utils.BatchUtils.*;
import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.*;

/**
 * Entity Job update factory class
 * @author srishti singh
 * @since 14 July 2025
 */
@Component
public class EntityUpdateJobFactory {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final ScheduledTaskItemListener itemListener;
    private final ScheduledTaskService scheduledTaskService;

    /** SkipPolicy to ignore all failures when executing jobs, as they can be handled later */
    private final SkipPolicy noopSkipPolicy = (Throwable t, int skipCount) -> true;

    @Resource
    EntityManagementConfiguration emConfig;

    @Autowired
    ApplicationContext applicationContext;

    public EntityUpdateJobFactory(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory,
                                  ScheduledTaskItemListener itemListener,
                                  ScheduledTaskService scheduledTaskService) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.itemListener = itemListener;
        this.scheduledTaskService = scheduledTaskService;
    }

    // TODO this is a execution, we should seperate it from the factory methods and step creations
    /**
     * Job for updating a single entity. Expects `entityId` string in JobParameters. This would
     * typically be run synchronously
     */
    public Job updateSingleEntity() {
        return this.jobBuilderFactory
                .get(JOB_UPDATE_SINGLE_ENTITY)
                .incrementer(new RunIdIncrementer())
                // this job is always launched from web requests, so synchronousTaskExecutor is used. It
                // also directly retrieves entities from the EntityRecord database.
                .start(synchronousUpdate())
                .build();
    }

    /**
     * Creates a step for synchronous update
     * @return
     */
    private Step synchronousUpdate() {
        return this.stepBuilderFactory
                .get(STEP_UPDATE_ENTITY)
                .<BatchEntityRecord, BatchEntityRecord>chunk(1)
                .reader(getReader(true))
                .processor(compositeUpdateProcessor())
                .writer(compositeEntityWriter())
                .listener((ItemProcessListener<? super BatchEntityRecord, ? super BatchEntityRecord>) itemListener)
                .faultTolerant()
                .skipPolicy(noopSkipPolicy)
                .taskExecutor((TaskExecutor)applicationContext.getBean(WEB_REQUEST_JOB_EXECUTOR))
                .throttleLimit(emConfig.getBatchUpdatesThrottleLimit())
                .listener(stepExecutionListener(List.of(ScheduledUpdateType.FULL_UPDATE), true))
                .build();

    }


    /**
     * Creates Item reader based on synchronus and async updates
     * @param synchronous
     * @return
     */
    private ItemReader<BatchEntityRecord> getReader(boolean synchronous) {
        return synchronous ? (EntityRecordDatabaseReader) applicationContext.getBean(SINGLE_ENTITY_RECORD_READER)
                : (SynchronizedItemStreamReader)applicationContext.getBean(SCHEDULED_TASK_READER);
    }


    /**
     * Creates the processor list -
     *    Processors: Dereference + consolidation + metrics + validation
     * @return
     */
    private ItemProcessor<BatchEntityRecord, BatchEntityRecord> compositeUpdateProcessor() {
        CompositeItemProcessor<BatchEntityRecord, BatchEntityRecord> compositeItemProcessor =
                new CompositeItemProcessor<>();
        compositeItemProcessor.setDelegates(
                Arrays.asList(
                        (EntityDereferenceProcessor) applicationContext.getBean(EntityDereferenceProcessor.class.getSimpleName()),
                        (EntityConsolidationProcessor) applicationContext.getBean(EntityConsolidationProcessor.class.getSimpleName()),
                        (EntityMetricsProcessor) applicationContext.getBean(EntityMetricsProcessor.class.getSimpleName()),
                        (EntityVerificationLogger) applicationContext.getBean(EntityVerificationLogger.class.getSimpleName())));
        return compositeItemProcessor;
    }

    /**
     * Creates the writer list -
     *    Writer: Db update + Solr update
     * @return
     */
    private ItemWriter<BatchEntityRecord> compositeEntityWriter() {
        CompositeItemWriter<BatchEntityRecord> compositeWriter = new CompositeItemWriter<>();
        compositeWriter.setDelegates(Arrays.asList(
                (EntityRecordDatabaseInsertionWriter) applicationContext.getBean(EntityRecordDatabaseInsertionWriter.class.getSimpleName()),
                (EntitySolrInsertionWriter) applicationContext.getBean(EntitySolrInsertionWriter.class.getSimpleName())));
        return compositeWriter;
    }

    /** Creates a StepExecutionListener that's called before / after the step runs
     *
     * */
    private StepExecutionListener stepExecutionListener(
            List<? extends ScheduledTaskType> updateType, boolean isSynchronous) {
        return new EntityUpdateStepListener(
                scheduledTaskService, updateType, isSynchronous, emConfig.getMaxFailedTaskRetries());
    }

}
