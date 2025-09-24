package eu.europeana.entitymanagement.batch.config;

import static eu.europeana.entitymanagement.batch.utils.BatchUtils.*;
import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Resource;

import eu.europeana.entitymanagement.batch.model.EntityUpdateStats;
import eu.europeana.entitymanagement.batch.service.ReportSenderTasklet;
import eu.europeana.entitymanagement.web.service.SlackConnection;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
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
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import eu.europeana.entitymanagement.batch.listener.EntityUpdateStepListener;
import eu.europeana.entitymanagement.batch.listener.ScheduledTaskItemListener;
import eu.europeana.entitymanagement.batch.model.JobDescription;
import eu.europeana.entitymanagement.batch.model.Task;
import eu.europeana.entitymanagement.batch.reader.EntityRecordDatabaseReader;
import eu.europeana.entitymanagement.batch.service.ScheduledTaskService;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.definitions.batch.model.TaskType;
import org.springframework.stereotype.Component;

/**
 * Entity Job update factory class
 * @author srishti singh
 * @since 14 July 2025
 */
@Component(ENTITY_UPDATE_JOB_FACTORY)
@EnableBatchProcessing
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
    
    @Autowired
    ApplicationContext appContext;

    @Resource
    private EntityUpdateStats stats;

    /**
     * Constructor default
     */
    public EntityUpdateJobFactory() {
        super();
    }

    /**
     * Creates Job via JobBuilderFactory.
     * Expects `entityId` string in JobParameters. This would
     * typically be run synchronously
     * @param jobDescription the job to be run ( contains list of processors and writers to be executed)
     * @return Job for entity update synchronous
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
     * `currentStartTime` date and `updateType` string in JobParameters
     * @param jobDescription the job to be run ( contains list of processors and writers to be executed)
     * @return Job for scheduled updates
     */
    public Job createScheduledUpdateJob(JobDescription jobDescription) {
        return this.jobBuilderFactory
                .get(JOB_UPDATE_SCHEDULED_ENTITIES)
                // This job is always launched via a @Scheduled method.
                .start(initStats(stats, jobDescription.getTaskType()))
                .next(entityUpdate(jobDescription, false))
                .next(finishStats())
                .next(sendStatusReportStep())
                .build();
    }

    /**
     * Job for updating entities scheduled via the ScheduledTasks collection Expects
     * `currentStartTime` date and `updateType` string in JobParameters.
     * @param jobDescription the job to be run ( contains list of processors and writers to be executed)
     * @param jobDescription the job to be run ( contains list of processors and writers to be executed)
     * @return Job for scheduled removal
     */
    public Job removeScheduledEntities(JobDescription jobDescription) {
        return this.jobBuilderFactory
                .get(JOB_REMOVE_SCHEDULED_ENTITIES)
                .start(removeEntity(jobDescription))
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
                        jobDescription.getTaskType(),
                        isSynchronous))
                .build();
    }

    private Step removeEntity(JobDescription jobDescription) {
        return this.stepBuilderFactory
                .get(STEP_REMOVE_ENTITY)
                .<BatchEntityRecord, BatchEntityRecord>chunk(emConfig.getBatchChunkSize())
                .reader(getReader(false))
                .writer(compositeWriters(jobDescription.getWriters()))
                .listener((ItemProcessListener<? super BatchEntityRecord, ? super BatchEntityRecord>)
                        itemListener)
                .faultTolerant()
                .skipPolicy(noopSkipPolicy)
                .taskExecutor(getRemovalTaskExecutor())
                .throttleLimit(emConfig.getBatchRemovalsThrottleLimit())
                // removal steps are always async
                .listener(stepExecutionListener(jobDescription.getTaskType(), false))
                .build();
    }


    private Step initStats(EntityUpdateStats stats, TaskType taskType) {
        return stepBuilderFactory
                .get("initStatsStep")
                .tasklet(
                        ((stepContribution, chunkContext) -> {
                            stats.reset();
                            stats.setTaskType(taskType);
                            return RepeatStatus.FINISHED;
                        }))
                .build();
    }

    private Step finishStats() {
        return stepBuilderFactory
                .get("finishStatsStep")
                .tasklet(
                        ((stepContribution, chunkContext) -> {
                            return RepeatStatus.FINISHED;
                        }))
                .build();
    }

    private Step sendStatusReportStep() {
        return stepBuilderFactory
                .get("sendStatusReport")
                .tasklet(new ReportSenderTasklet(stats,
                        emConfig.getEntityManagementBaseUrl(),
                        getApplicationContext().getBean(SLACK_CONNECTION, SlackConnection.class)))
                .build();
    }

    private TaskExecutor getTaskExecutor(boolean isSynchronous) {
        return isSynchronous ? (TaskExecutor)getApplicationContext().getBean(WEB_REQUEST_JOB_EXECUTOR)
                : (TaskExecutor) getApplicationContext().getBean(UPDATES_STEP_EXECUTOR);
    }

    private TaskExecutor getRemovalTaskExecutor() {
        return (TaskExecutor) getApplicationContext().getBean(REMOVALS_STEP_EXECUTOR);
    }

    private int getChunkSize(boolean isSynchronous) {
        return isSynchronous ? 1 : emConfig.getBatchChunkSize();
    }

    /**
     * Creates Item reader based on synchronus and async updates
     * @param synchronous
     * @return
     */
    @SuppressWarnings("unchecked")
    private ItemReader<BatchEntityRecord> getReader(boolean synchronous) {
        return synchronous ? (EntityRecordDatabaseReader) getApplicationContext().getBean(SINGLE_ENTITY_RECORD_READER)
                : (SynchronizedItemStreamReader<BatchEntityRecord>)getApplicationContext().getBean(SCHEDULED_TASK_READER);
    }

    @SuppressWarnings("unchecked")
    private ItemProcessor<BatchEntityRecord, BatchEntityRecord> getProcessor(JobDescription jobDescription) {
      switch (jobDescription.getTaskType()) {
        case full_update:
          //SG: should evaluate performance and eventually cash referenced beans
          return (ItemProcessor<BatchEntityRecord, BatchEntityRecord>) getApplicationContext().getBean(FULL_ENTITY_UPDATE_PROCESSOR);
        default:
          return compositeProcessor(jobDescription.getProcessors());
      }      
    }

    @SuppressWarnings("unchecked")
    private ItemWriter<BatchEntityRecord> getWriter() {
       return (ItemWriter<BatchEntityRecord>) getApplicationContext().getBean(ENTITY_UPDATE_WRITERS);
    }

    /** Creates a StepExecutionListener that's called before / after the step runs
     *
     * */
    private StepExecutionListener stepExecutionListener(
            TaskType updateType, boolean isSynchronous) {
        return new EntityUpdateStepListener(
                scheduledTaskService, Collections.singletonList(updateType), isSynchronous, emConfig.getMaxFailedTaskRetries());
    }

    /**
     * Creating it as a bean as the writer list is same for all the Internal Task of EM.
     * For performance will access them from application context than creating a list for every request
     * @see <a href="http://docs.google.com/document/d/16k9PcCMFwl2LXjnnzotZRPc-QqM-Ar1D0VELHt4t_hA/edit?tab=t.0#heading=h.fj6e15rbq64q"></a> }
     * Creates the writer list -
     *    Writer: Db update + Solr update
     * @return ItemWriter<BatchEntityRecord> with above mentioned list
     */
    public ItemWriter<BatchEntityRecord> buildEntityUpdateWriters() {
        return compositeWriters(JobDescription.PERSISTENCE_ITEM_WRITERS);
    }

    @SuppressWarnings("unchecked")
    private ItemWriter<BatchEntityRecord> compositeWriters(List<Task> writers) {
        CompositeItemWriter<BatchEntityRecord> compositeWriter = new CompositeItemWriter<>();
        List<ItemWriter<? super BatchEntityRecord>> delegates = new ArrayList<>(writers.size());
        for (Task writer: writers) {
            delegates.add((ItemWriter<BatchEntityRecord>) getApplicationContext().getBean(writer.getBeanName()));
        }
        compositeWriter.setDelegates(delegates);
        return compositeWriter;
    }


    /**
     * Creating it as a bean as this processor list is used for most of the Internal Task of EM.
     * For performnace will access them from application context than creating a list for every request
     * @see <a href="http://docs.google.com/document/d/16k9PcCMFwl2LXjnnzotZRPc-QqM-Ar1D0VELHt4t_hA/edit?tab=t.0#heading=h.fj6e15rbq64q"></a> }
     *
     * Creates the processor list -
     *    Processors: Dereference + consolidation + metrics + validation
     * @return ItemProcessor with the above list
     */
    public ItemProcessor<BatchEntityRecord, BatchEntityRecord> createFullEntityUpdateProcessor() {
      return compositeProcessor(JobDescription.PROCESSORS_FULL_UPDATE);
    }

    /**
     * More generic composite processor,
     * Creates a composite processor with the list of processors provided
     * @param processors list of processors
     * @return ItemProcessor - composite list of processors
     */
    @SuppressWarnings("unchecked")
    public ItemProcessor<BatchEntityRecord, BatchEntityRecord> compositeProcessor(List<Task> processors) {
      CompositeItemProcessor<BatchEntityRecord, BatchEntityRecord> compositeItemProcessor =
              new CompositeItemProcessor<>();
      List<ItemProcessor<BatchEntityRecord, BatchEntityRecord>> delegates = new ArrayList<>(processors.size());
      for (Task process: processors) {
        delegates.add((ItemProcessor<BatchEntityRecord, BatchEntityRecord>) getApplicationContext().getBean(process.getBeanName()));
      }
      compositeItemProcessor.setDelegates(delegates);
      return compositeItemProcessor;
    }

    /**
     * local getter method for application context
     * @return the {@link ApplicationContext}
     */
    ApplicationContext getApplicationContext() {
      return appContext;
  }

}
