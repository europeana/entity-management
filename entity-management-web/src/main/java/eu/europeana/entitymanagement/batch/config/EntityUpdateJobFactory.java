package eu.europeana.entitymanagement.batch.config;

import static eu.europeana.entitymanagement.batch.utils.BatchUtils.JOB_UPDATE_SCHEDULED_ENTITIES;
import static eu.europeana.entitymanagement.batch.utils.BatchUtils.JOB_UPDATE_SINGLE_ENTITY;
import static eu.europeana.entitymanagement.batch.utils.BatchUtils.STEP_UPDATE_ENTITY;
import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.ENTITY_UPDATE_WRITERS;
import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.FULL_ENTITY_UPDATE_PROCESSOR;
import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.SCHEDULED_TASK_READER;
import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.SINGLE_ENTITY_RECORD_READER;
import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.UPDATES_STEP_EXECUTOR;
import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.WEB_REQUEST_JOB_EXECUTOR;
import java.util.List;
import javax.annotation.Resource;
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
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import eu.europeana.entitymanagement.batch.listener.EntityUpdateStepListener;
import eu.europeana.entitymanagement.batch.listener.ScheduledTaskItemListener;
import eu.europeana.entitymanagement.batch.model.JobDescription;
import eu.europeana.entitymanagement.batch.reader.EntityRecordDatabaseReader;
import eu.europeana.entitymanagement.batch.service.ScheduledTaskService;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.config.AppAutoconfig;
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.definitions.batch.model.TaskType;

/**
 * Entity Job update factory class
 * @author srishti singh
 * @since 14 July 2025
 */
@Component
public class EntityUpdateJobFactory {

    @Resource
    private JobBuilderFactory jobBuilderFactory;
    @Resource
    private StepBuilderFactory stepBuilderFactory;
    @Resource
    private ScheduledTaskItemListener itemListener;
    @Resource
    private ScheduledTaskService scheduledTaskService;

    /** SkipPolicy to ignore all failures when executing jobs, as they can be handled later */
    private final SkipPolicy noopSkipPolicy = (Throwable t, int skipCount) -> true;

    @Resource
    EntityManagementConfiguration emConfig;
    
//    @Resource
//    AppAutoconfig emAutoConfig;
    @Autowired
    ApplicationContext appContext;

    public EntityUpdateJobFactory() {
      super();
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
                .start(entityUpdate(jobDescription, true))
                .build();
    }

    /**
     * Job for updating entities scheduled via the ScheduledTasks collection Expects
     * `currentStartTime` date and `updateType` string in JobParameters.
     */
    public Job createScheduledUpdateJob(JobDescription jobDescription) {
        return this.jobBuilderFactory
                .get(JOB_UPDATE_SCHEDULED_ENTITIES)
                // This job is always launched via a @Scheduled method.
                .start(entityUpdate(jobDescription, false))
                .build();
    }

    /**
     * Creates a dynamic step for synchronous and Asynchronous entity update
     * @return
     */
    private Step entityUpdate(JobDescription jobDescription, boolean isSynchronous) {
        return this.stepBuilderFactory
                .get(STEP_UPDATE_ENTITY)
                .<BatchEntityRecord, BatchEntityRecord>chunk(getChunkSize(isSynchronous))
                .reader(getReader(isSynchronous))
                .processor(getProcessor(jobDescription))
                .writer(getWriter())
                .listener((ItemProcessListener<? super BatchEntityRecord, ? super BatchEntityRecord>) itemListener)
                .faultTolerant()
                .skipPolicy(noopSkipPolicy)
                .taskExecutor(getTaskExecutor(isSynchronous))
                .throttleLimit(emConfig.getBatchUpdatesThrottleLimit())
                .listener(stepExecutionListener(
                        List.of(TaskType.FULL_UPDATE, TaskType.METRICS_UPDATE),
                        isSynchronous))
                .build();
    }



    private TaskExecutor getTaskExecutor(boolean isSynchronous) {
        return isSynchronous ? (TaskExecutor)getApplicationContext().getBean(WEB_REQUEST_JOB_EXECUTOR)
                : (TaskExecutor) getApplicationContext().getBean(UPDATES_STEP_EXECUTOR);
    }

    private int getChunkSize(boolean isSynchronous) {
        return isSynchronous ? 1 : emConfig.getBatchChunkSize();
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
                (ItemProcessor<BatchEntityRecord, BatchEntityRecord>) getApplicationContext().getBean(FULL_ENTITY_UPDATE_PROCESSOR) : null;
//                 : emAutoConfig.compositeProcessor(jobDescription.getProcessors());
 //TODO: move to factory 
    }

    private ItemWriter<BatchEntityRecord> getWriter() {
       return (ItemWriter<BatchEntityRecord>) getApplicationContext().getBean(ENTITY_UPDATE_WRITERS);
    }

    /** Creates a StepExecutionListener that's called before / after the step runs
     *
     * */
    private StepExecutionListener stepExecutionListener(
            List<TaskType> updateType, boolean isSynchronous) {
        return new EntityUpdateStepListener(
                scheduledTaskService, updateType, isSynchronous, emConfig.getMaxFailedTaskRetries());
    }

    ApplicationContext getApplicationContext() {
        return appContext;
    }
}
