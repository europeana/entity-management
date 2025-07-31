package eu.europeana.entitymanagement.batch.config;

import eu.europeana.entitymanagement.batch.listener.EntityUpdateStepListener;
import eu.europeana.entitymanagement.batch.listener.ScheduledTaskItemListener;
import eu.europeana.entitymanagement.batch.model.JobDescription;
import eu.europeana.entitymanagement.batch.model.Task;
import eu.europeana.entitymanagement.batch.reader.EntityRecordDatabaseReader;
import eu.europeana.entitymanagement.batch.service.ScheduledTaskService;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.config.AppAutoconfig;
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
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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

    @Resource
    AppAutoconfig emAutoConfig;

    public EntityUpdateJobFactory(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory,
                                  ScheduledTaskItemListener itemListener,
                                  ScheduledTaskService scheduledTaskService) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.itemListener = itemListener;
        this.scheduledTaskService = scheduledTaskService;
    }

    /**
     * Creates Job via JobBuilderFactory.
     * Expects `entityId` string in JobParameters. This would
     * typically be run synchronously
     */
    public Job createJob(JobDescription jobDescription) {
        return this.jobBuilderFactory
                .get(JOB_UPDATE_SINGLE_ENTITY)
                .incrementer(new RunIdIncrementer())
                // this job is always launched from web requests, so synchronousTaskExecutor is used. It
                // also directly retrieves entities from the EntityRecord database.
                .start(synchronousUpdate(jobDescription))
                .build();
    }

    /**
     * Creates a step for synchronous update
     * @return
     */
    private Step synchronousUpdate(JobDescription jobDescription) {
        return this.stepBuilderFactory
                .get(STEP_UPDATE_ENTITY)
                .<BatchEntityRecord, BatchEntityRecord>chunk(1)
                .reader(getReader(true))
                .processor(getProcessor(jobDescription))
                .writer(getWriter(jobDescription))
                .listener((ItemProcessListener<? super BatchEntityRecord, ? super BatchEntityRecord>) itemListener)
                .faultTolerant()
                .skipPolicy(noopSkipPolicy)
                .taskExecutor(getTaskExecutor())
                .throttleLimit(emConfig.getBatchUpdatesThrottleLimit())
                .listener(stepExecutionListener(List.of(ScheduledUpdateType.FULL_UPDATE), true))
                .build();

    }

    private TaskExecutor getTaskExecutor() {
        return (TaskExecutor)getApplicationContext().getBean(WEB_REQUEST_JOB_EXECUTOR);
    }

    /**
     * Creates Item reader based on synchronus and async updates
     * @param synchronous
     * @return
     */
    private ItemReader<BatchEntityRecord> getReader(boolean synchronous) {
        return synchronous ? (EntityRecordDatabaseReader) getApplicationContext().getBean(SINGLE_ENTITY_RECORD_READER)
                : (SynchronizedItemStreamReader)getApplicationContext().getBean(SCHEDULED_TASK_READER);
    }

    private ItemProcessor<BatchEntityRecord, BatchEntityRecord> getProcessor(JobDescription jobDescription) {
        return jobDescription.isFullUpdate() ?
                (ItemProcessor<BatchEntityRecord, BatchEntityRecord>) getApplicationContext().getBean(FULL_ENTITY_UPDATE_PROCESSOR)
                 : emAutoConfig.compositeProcessor(jobDescription.getProcessors());

    }

    private ItemWriter<BatchEntityRecord> getWriter(JobDescription jobDescription) {
        ItemWriter<BatchEntityRecord> writer = new CompositeItemWriter<>();
        if (jobDescription.isFullUpdate()) {
            writer = (ItemWriter<BatchEntityRecord>) getApplicationContext().getBean(ENTITY_UPDATE_WRITERS);
        } else if (jobDescription.mongoUpdate()) {
            writer = emAutoConfig.recordDBInsertionWriter();
        } else if (jobDescription.solrInsertion()) {
            writer = emAutoConfig.entitySolrInsertionWriter();
        }
        return writer;
    }

    /** Creates a StepExecutionListener that's called before / after the step runs
     *
     * */
    private StepExecutionListener stepExecutionListener(
            List<? extends ScheduledTaskType> updateType, boolean isSynchronous) {
        return new EntityUpdateStepListener(
                scheduledTaskService, updateType, isSynchronous, emConfig.getMaxFailedTaskRetries());
    }

    private boolean isFullUpdate(List<Task> processors) {
        return processors.contains(Task.DEREFERENCE) && processors.contains(Task.CONSOLIDATION)
                && processors.contains(Task.VALIDATION) && processors.contains(Task.METRICS);
    }

    public ApplicationContext getApplicationContext() {
        return emAutoConfig.getApplicationContext();
    }
}
