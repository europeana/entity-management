package eu.europeana.entitymanagement.batch.config;

import dev.morphia.query.experimental.filters.Filters;
import eu.europeana.entitymanagement.batch.EMBatchConstants;
import eu.europeana.entitymanagement.batch.listener.EntityUpdateListener;
import eu.europeana.entitymanagement.batch.model.BatchUpdateType;
import eu.europeana.entitymanagement.batch.processor.EntityDereferenceProcessor;
import eu.europeana.entitymanagement.batch.processor.EntityMetricsProcessor;
import eu.europeana.entitymanagement.batch.processor.EntityUpdateProcessor;
import eu.europeana.entitymanagement.batch.reader.EntityRecordDatabaseReader;
import eu.europeana.entitymanagement.batch.reader.ScheduledTaskDatabaseReader;
import eu.europeana.entitymanagement.batch.service.FailedTaskService;
import eu.europeana.entitymanagement.batch.service.ScheduledTaskService;
import eu.europeana.entitymanagement.batch.writer.EntityRecordDatabaseWriter;
import eu.europeana.entitymanagement.batch.writer.EntitySolrWriter;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.exception.EntityMismatchException;
import eu.europeana.entitymanagement.exception.MetisNotKnownException;
import eu.europeana.entitymanagement.solr.exception.SolrServiceException;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;

import static eu.europeana.entitymanagement.batch.BatchUtils.*;
import static eu.europeana.entitymanagement.batch.model.BatchUpdateType.FULL;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.STEP_EXECUTOR;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.WEB_REQUEST_JOB_EXECUTOR;
import static eu.europeana.entitymanagement.definitions.EntityRecordFields.ENTITY_ID;

@Component
public class EntityUpdateJobConfig {

    private static final String SINGLE_ENTITY_RECORD_READER = "singleEntityRecordReader";
    private static final String SCHEDULED_TASK_READER = "scheduledTaskReader";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final ItemReader<EntityRecord> singleEntityRecordReader;

    private final ItemReader<EntityRecord> scheduledTaskReader;
    private final EntityDereferenceProcessor dereferenceProcessor;
    private final EntityUpdateProcessor entityUpdateProcessor;
    private final EntityMetricsProcessor entityMetricsProcessor;
    private final EntityRecordDatabaseWriter dbWriter;
    private final EntitySolrWriter solrWriter;
    private final EntityRecordService entityRecordService;

    private final ScheduledTaskService scheduledTaskService;

    private final FailedTaskService failedTaskService;
    private final EntityUpdateListener entityUpdateListener;


    private final TaskExecutor stepThreadPoolExecutor;
    private final TaskExecutor synchronousTaskExecutor;

    private final int chunkSize;

    private final int throttleLimit;

    /**
     * SkipPolicy to ignore all failures when executing jobs, as they can be handled later
     */
    private final SkipPolicy noopSkipPolicy = (Throwable t, int skipCount) -> true;

    //TODO: Too many dependencies. Split up into multiple classes
    @Autowired
    public EntityUpdateJobConfig(JobBuilderFactory jobBuilderFactory,
                                 StepBuilderFactory stepBuilderFactory,
                                 @Qualifier(SINGLE_ENTITY_RECORD_READER) ItemReader<EntityRecord> singleEntityRecordReader,
                                 @Qualifier(SCHEDULED_TASK_READER) ItemReader<EntityRecord> scheduledTaskReader,
                                 EntityDereferenceProcessor dereferenceProcessor,
                                 EntityUpdateProcessor entityUpdateProcessor,
                                 EntityMetricsProcessor entityMetricsProcessor,
                                 EntityRecordDatabaseWriter dbWriter,
                                 EntitySolrWriter solrWriter, EntityRecordService entityRecordService,
                                 ScheduledTaskService scheduledTaskService,
                                 FailedTaskService failedTaskService, EntityUpdateListener entityUpdateListener,
                                 @Qualifier(STEP_EXECUTOR) TaskExecutor stepThreadPoolExecutor,
                                 @Qualifier(WEB_REQUEST_JOB_EXECUTOR) TaskExecutor synchronousTaskExecutor,
                                 EntityManagementConfiguration emConfig) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.singleEntityRecordReader = singleEntityRecordReader;
        this.scheduledTaskReader = scheduledTaskReader;
        this.dereferenceProcessor = dereferenceProcessor;
        this.entityUpdateProcessor = entityUpdateProcessor;
        this.entityMetricsProcessor = entityMetricsProcessor;
        this.dbWriter = dbWriter;
        this.solrWriter = solrWriter;
        this.entityRecordService = entityRecordService;
        this.scheduledTaskService = scheduledTaskService;
        this.failedTaskService = failedTaskService;
        this.entityUpdateListener = entityUpdateListener;
        this.stepThreadPoolExecutor = stepThreadPoolExecutor;
        this.synchronousTaskExecutor = synchronousTaskExecutor;
        this.chunkSize = emConfig.getBatchChunkSize();
        this.throttleLimit = emConfig.getBatchStepThrottleLimit();
    }

    /**
     * Makes ItemReader thread-safe
     */
    private <T> SynchronizedItemStreamReader<T> threadSafeReader(
            ItemStreamReader<T> reader) {
        final SynchronizedItemStreamReader<T> synchronizedItemStreamReader = new SynchronizedItemStreamReader<>();
        synchronizedItemStreamReader.setDelegate(reader);
        return synchronizedItemStreamReader;
    }

    /**
     * ItemReader that queries by entityId when retrieving EntityRecords from the database
     */
    @Bean(name = SINGLE_ENTITY_RECORD_READER)
    @StepScope
    private EntityRecordDatabaseReader singleEntityRecordReader(@Value("#{jobParameters[entityId]}") String entityIdString)  {
        return new EntityRecordDatabaseReader(entityRecordService, chunkSize,
                Filters.eq(ENTITY_ID, entityIdString)
        );
    }

    @Bean(name = SCHEDULED_TASK_READER)
    @StepScope
    private SynchronizedItemStreamReader<EntityRecord> allEntityFailureReader(
            @Value("#{jobParameters[currentStartTime]}") Date currentStartTime,
            @Value("#{jobParameters[updateType]}") String updateType
    ) {
        ScheduledTaskDatabaseReader reader = new ScheduledTaskDatabaseReader(scheduledTaskService, chunkSize,
                Filters.lte(EMBatchConstants.CREATED, currentStartTime),
                Filters.eq(EMBatchConstants.UPDATE_TYPE, updateType));

        return threadSafeReader(reader);
    }

    @Bean
    @StepScope
    private EntityUpdateListener entityUpdateListener(@Value("#{jobParameters[updateType]}") String updateType){
        return new EntityUpdateListener(failedTaskService, scheduledTaskService, BatchUpdateType.valueOf(updateType));
    }

    /**
     * Creates a Composite ItemProcessor to perform Metis de-referencing, metadata update, and
     * metrics update.
     *
     * These are implemented as processors, instead of discrete steps to eliminate the overhead required
     * to pass data between steps via the Execution context.
     */
    @Bean
    private ItemProcessor<EntityRecord, EntityRecord> compositeUpdateProcessor() {
        CompositeItemProcessor<EntityRecord, EntityRecord> compositeItemProcessor = new CompositeItemProcessor<>();
        compositeItemProcessor.setDelegates(Arrays.asList(dereferenceProcessor, entityUpdateProcessor, entityMetricsProcessor));
        return compositeItemProcessor;
    }

    /**
     * Creates a Composite ItemWriter for persisting Entities to Mongo and Solr
     */
    @Bean
    private ItemWriter<EntityRecord> compositeEntityWriter(){
        CompositeItemWriter<EntityRecord> compositeWriter = new CompositeItemWriter<>();
        compositeWriter.setDelegates(Arrays.asList(dbWriter, solrWriter));
        return compositeWriter;
    }

    /**
     * Step for updating entities.
     * Uses a different reader and processor, based on the value of updateType.
     * @param updateType type of update – either METRICS or FULL
     * @param chunkSize chunk size for step
     * @param executor task executor to use. If chunkSize is 1, this should typically be a synchronous executor.
     * @param reader ItemReader to use in step.
     *
     * @return step
     */
    private Step updateEntity(BatchUpdateType updateType, int chunkSize, TaskExecutor executor,
                              ItemReader<EntityRecord> reader){
        SimpleStepBuilder<EntityRecord, EntityRecord> step = this.stepBuilderFactory.get(STEP_UPDATE_ENTITY)
                .<EntityRecord, EntityRecord>chunk(chunkSize)
                .reader(reader);
        // set processor based on the update type
        step.processor(updateType == FULL ? compositeUpdateProcessor() : entityMetricsProcessor);

        return step.writer(compositeEntityWriter())
                .listener(
                        (ItemProcessListener<? super EntityRecord, ? super EntityRecord>) entityUpdateListener)
                .faultTolerant()
                .skipPolicy(noopSkipPolicy)
                .skip(EntityMismatchException.class)
                .skip(MetisNotKnownException.class)
                .skip(SolrServiceException.class)
                .taskExecutor(executor)
                .throttleLimit(throttleLimit)
                .build();
    }


    /**
     * Job for updating a single entity.
     * Expects `entityId` string in JobParameters. This would typically be run synchronously
     */
    public Job updateSingleEntity() {
        return this.jobBuilderFactory.get(JOB_UPDATE_SINGLE_ENTITY)
                .incrementer(new RunIdIncrementer())
                // this job is always launched from web requests, so synchronousTaskExecutor is used. It
                // also directly retrieves entities from the EntityRecord database.
                .start(updateEntity(FULL, 1, synchronousTaskExecutor, singleEntityRecordReader))
                .build();
    }

    /**
     * Job for updating entities scheduled via the ScheduledTasks collection
     * Expects `currentStartTime` date and `updateType` string in JobParameters.
     */
    public Job updateScheduledEntities(BatchUpdateType updateType){
        return this.jobBuilderFactory.get(JOB_UPDATE_SCHEDULED_ENTITIES)
                // This job is always launched via a @Scheduled method.
                .start(updateEntity(updateType, chunkSize, stepThreadPoolExecutor, scheduledTaskReader))
                .build();
    }
}
