package eu.europeana.entitymanagement.batch.config;

import static eu.europeana.entitymanagement.batch.utils.BatchUtils.*;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.REMOVALS_STEP_EXECUTOR;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.UPDATES_STEP_EXECUTOR;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.WEB_REQUEST_JOB_EXECUTOR;
import static eu.europeana.entitymanagement.definitions.EntityRecordFields.ENTITY_ID;
import static eu.europeana.entitymanagement.definitions.batch.ScheduledTaskUtils.scheduledTaskTypeValueOf;

import dev.morphia.query.experimental.filters.Filters;
import eu.europeana.entitymanagement.batch.listener.EntityUpdateStepListener;
import eu.europeana.entitymanagement.batch.listener.ScheduledTaskItemListener;
import eu.europeana.entitymanagement.batch.processor.EntityDereferenceProcessor;
import eu.europeana.entitymanagement.batch.processor.EntityMetricsProcessor;
import eu.europeana.entitymanagement.batch.processor.EntityUpdateProcessor;
import eu.europeana.entitymanagement.batch.reader.EntityRecordDatabaseReader;
import eu.europeana.entitymanagement.batch.reader.ScheduledTaskDatabaseReader;
import eu.europeana.entitymanagement.batch.service.FailedTaskService;
import eu.europeana.entitymanagement.batch.service.ScheduledTaskService;
import eu.europeana.entitymanagement.batch.writer.EntityRecordDatabaseDeprecationWriter;
import eu.europeana.entitymanagement.batch.writer.EntityRecordDatabaseInsertionWriter;
import eu.europeana.entitymanagement.batch.writer.EntityRecordDatabaseRemovalWriter;
import eu.europeana.entitymanagement.batch.writer.EntitySolrInsertionWriter;
import eu.europeana.entitymanagement.batch.writer.EntitySolrRemovalWriter;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.batch.EMBatchConstants;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledRemovalType;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledUpdateType;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import java.util.Arrays;
import java.util.Date;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
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
  private final EntityRecordDatabaseInsertionWriter dbInsertionWriter;
  private final EntitySolrInsertionWriter solrInsertionWriter;

  private final EntitySolrRemovalWriter solrRemovalWriter;
  private final EntityRecordDatabaseRemovalWriter dbRemovalWriter;
  private final EntityRecordDatabaseDeprecationWriter dbDeprecationWriter;

  private final EntityRecordService entityRecordService;
  private final ScheduledTaskService scheduledTaskService;

  private final FailedTaskService failedTaskService;
  private final ScheduledTaskItemListener itemListener;

  private final TaskExecutor updatesStepExecutor;
  private final TaskExecutor removalsStepExecutor;
  private final TaskExecutor synchronousTaskExecutor;

  private final int chunkSize;

  private final int updatesThrottleLimit;
  private final int removalsThrottleLimit;

  /** SkipPolicy to ignore all failures when executing jobs, as they can be handled later */
  private final SkipPolicy noopSkipPolicy = (Throwable t, int skipCount) -> true;

  // TODO: Too many dependencies. Split up into multiple classes
  @Autowired
  public EntityUpdateJobConfig(
      JobBuilderFactory jobBuilderFactory,
      StepBuilderFactory stepBuilderFactory,
      @Qualifier(SINGLE_ENTITY_RECORD_READER) ItemReader<EntityRecord> singleEntityRecordReader,
      @Qualifier(SCHEDULED_TASK_READER) ItemReader<EntityRecord> scheduledTaskReader,
      EntityDereferenceProcessor dereferenceProcessor,
      EntityUpdateProcessor entityUpdateProcessor,
      EntityMetricsProcessor entityMetricsProcessor,
      EntityRecordDatabaseInsertionWriter dbInsertionWriter,
      EntitySolrInsertionWriter solrInsertionWriter,
      EntitySolrRemovalWriter solrRemovalWriter,
      EntityRecordDatabaseRemovalWriter dbRemovalWriter,
      EntityRecordDatabaseDeprecationWriter dbDeprecationWriter,
      EntityRecordService entityRecordService,
      ScheduledTaskService scheduledTaskService,
      FailedTaskService failedTaskService,
      ScheduledTaskItemListener itemListener,
      @Qualifier(UPDATES_STEP_EXECUTOR) TaskExecutor updatesStepExecutor,
      @Qualifier(REMOVALS_STEP_EXECUTOR) TaskExecutor removalsStepExecutor,
      @Qualifier(WEB_REQUEST_JOB_EXECUTOR) TaskExecutor synchronousTaskExecutor,
      EntityManagementConfiguration emConfig) {
    this.jobBuilderFactory = jobBuilderFactory;
    this.stepBuilderFactory = stepBuilderFactory;
    this.singleEntityRecordReader = singleEntityRecordReader;
    this.scheduledTaskReader = scheduledTaskReader;
    this.dereferenceProcessor = dereferenceProcessor;
    this.entityUpdateProcessor = entityUpdateProcessor;
    this.entityMetricsProcessor = entityMetricsProcessor;
    this.dbInsertionWriter = dbInsertionWriter;
    this.solrInsertionWriter = solrInsertionWriter;
    this.solrRemovalWriter = solrRemovalWriter;
    this.dbRemovalWriter = dbRemovalWriter;
    this.dbDeprecationWriter = dbDeprecationWriter;
    this.entityRecordService = entityRecordService;
    this.scheduledTaskService = scheduledTaskService;
    this.failedTaskService = failedTaskService;
    this.itemListener = itemListener;
    this.updatesStepExecutor = updatesStepExecutor;
    this.removalsStepExecutor = removalsStepExecutor;
    this.synchronousTaskExecutor = synchronousTaskExecutor;
    this.chunkSize = emConfig.getBatchChunkSize();
    this.updatesThrottleLimit = emConfig.getBatchUpdatesThrottleLimit();
    removalsThrottleLimit = emConfig.getBatchRemovalsThrottleLimit();
  }

  /** Makes ItemReader thread-safe */
  private <T> SynchronizedItemStreamReader<T> threadSafeReader(ItemStreamReader<T> reader) {
    final SynchronizedItemStreamReader<T> synchronizedItemStreamReader =
        new SynchronizedItemStreamReader<>();
    synchronizedItemStreamReader.setDelegate(reader);
    return synchronizedItemStreamReader;
  }

  /** ItemReader that queries by entityId when retrieving EntityRecords from the database */
  @Bean(name = SINGLE_ENTITY_RECORD_READER)
  @StepScope
  private EntityRecordDatabaseReader singleEntityRecordReader(
      @Value("#{jobParameters[entityId]}") String entityIdString) {
    return new EntityRecordDatabaseReader(
        entityRecordService, chunkSize, Filters.eq(ENTITY_ID, entityIdString));
  }

  @Bean(name = SCHEDULED_TASK_READER)
  @StepScope
  private SynchronizedItemStreamReader<EntityRecord> allEntityFailureReader(
      @Value("#{jobParameters[currentStartTime]}") Date currentStartTime,
      @Value("#{jobParameters[updateType]}") String updateType) {
    ScheduledTaskDatabaseReader reader =
        new ScheduledTaskDatabaseReader(
            scheduledTaskService,
            chunkSize,
            Filters.lte(EMBatchConstants.CREATED, currentStartTime),
            Filters.eq(EMBatchConstants.UPDATE_TYPE, updateType));

    return threadSafeReader(reader);
  }

  @Bean
  @StepScope
  private ScheduledTaskItemListener entityUpdateListener(
      @Value("#{jobParameters[updateType]}") String updateType) {
    return new ScheduledTaskItemListener(
        failedTaskService, scheduledTaskService, scheduledTaskTypeValueOf(updateType));
  }

  private StepExecutionListener stepExecutionListener(ScheduledTaskType updateType) {
    return new EntityUpdateStepListener(scheduledTaskService, updateType);
  }

  /**
   * Creates a Composite ItemProcessor to perform Metis de-referencing, metadata update, and metrics
   * update.
   *
   * <p>These are implemented as processors, instead of discrete steps to eliminate the overhead
   * required to pass data between steps via the Execution context.
   */
  @Bean
  private ItemProcessor<EntityRecord, EntityRecord> compositeUpdateProcessor() {
    CompositeItemProcessor<EntityRecord, EntityRecord> compositeItemProcessor =
        new CompositeItemProcessor<>();
    compositeItemProcessor.setDelegates(
        Arrays.asList(dereferenceProcessor, entityUpdateProcessor, entityMetricsProcessor));
    return compositeItemProcessor;
  }

  /** Creates a Composite ItemWriter for persisting Entities to Mongo and Solr */
  private ItemWriter<EntityRecord> compositeEntityInsertionWriter() {
    CompositeItemWriter<EntityRecord> compositeWriter = new CompositeItemWriter<>();
    compositeWriter.setDelegates(Arrays.asList(dbInsertionWriter, solrInsertionWriter));
    return compositeWriter;
  }

  private ItemWriter<EntityRecord> compositeEntityDeletionWriter() {
    CompositeItemWriter<EntityRecord> compositeWriter = new CompositeItemWriter<>();
    compositeWriter.setDelegates(Arrays.asList(dbRemovalWriter, solrRemovalWriter));
    return compositeWriter;
  }

  private ItemWriter<EntityRecord> compositeEntityDeprecationWriter() {
    CompositeItemWriter<EntityRecord> compositeWriter = new CompositeItemWriter<>();
    compositeWriter.setDelegates(Arrays.asList(dbDeprecationWriter, solrRemovalWriter));
    return compositeWriter;
  }

  /**
   * Step for updating entities. Uses a different reader and processor, based on the value of
   * updateType.
   *
   * @param updateType type of update – either METRICS or FULL
   * @param chunkSize chunk size for step
   * @param executor task executor to use. If chunkSize is 1, this should typically be a synchronous
   *     executor.
   * @param reader ItemReader to use in step.
   * @return step
   */
  private Step updateEntity(
      ScheduledUpdateType updateType,
      int chunkSize,
      TaskExecutor executor,
      ItemReader<EntityRecord> reader) {
    SimpleStepBuilder<EntityRecord, EntityRecord> step =
        this.stepBuilderFactory
            .get(STEP_UPDATE_ENTITY)
            .<EntityRecord, EntityRecord>chunk(chunkSize)
            .reader(reader);

    // set processor based on the update type
    switch (updateType) {
      case FULL_UPDATE:
        step.processor(compositeUpdateProcessor());
        break;
      case METRICS_UPDATE:
        step.processor(entityMetricsProcessor);
        break;
      default:
        throw new IllegalStateException("No processor configured for updateType " + updateType);
    }

    return step.writer(compositeEntityInsertionWriter())
        .listener((ItemProcessListener<? super EntityRecord, ? super EntityRecord>) itemListener)
        .faultTolerant()
        .skipPolicy(noopSkipPolicy)
        .taskExecutor(executor)
        .throttleLimit(updatesThrottleLimit)
        .listener(stepExecutionListener(updateType))
        .build();
  }

  private Step removeEntity(
      ScheduledRemovalType removalType,
      int chunkSize,
      TaskExecutor executor,
      ItemReader<EntityRecord> reader) {

    SimpleStepBuilder<EntityRecord, EntityRecord> step =
        this.stepBuilderFactory
            .get(STEP_REMOVE_ENTITY)
            .<EntityRecord, EntityRecord>chunk(chunkSize)
            .reader(reader);

    switch (removalType) {
      case DEPRECATION:
        step.writer(compositeEntityDeprecationWriter());
        break;
      case PERMANENT_DELETION:
        step.writer(compositeEntityDeletionWriter());
        break;
      default:
        throw new IllegalStateException(
            "No Spring Batch writer configured for removalType " + removalType);
    }

    return step.listener(
            (ItemProcessListener<? super EntityRecord, ? super EntityRecord>) itemListener)
        .faultTolerant()
        .skipPolicy(noopSkipPolicy)
        .taskExecutor(executor)
        .throttleLimit(removalsThrottleLimit)
        .listener(stepExecutionListener(removalType))
        .build();
  }

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
        .start(
            updateEntity(
                ScheduledUpdateType.FULL_UPDATE,
                1,
                synchronousTaskExecutor,
                singleEntityRecordReader))
        .build();
  }

  /**
   * Job for updating entities scheduled via the ScheduledTasks collection Expects
   * `currentStartTime` date and `updateType` string in JobParameters.
   */
  public Job updateScheduledEntities(ScheduledUpdateType updateType) {
    return this.jobBuilderFactory
        .get(JOB_UPDATE_SCHEDULED_ENTITIES)
        // This job is always launched via a @Scheduled method.
        .start(updateEntity(updateType, chunkSize, updatesStepExecutor, scheduledTaskReader))
        .build();
  }

  public Job removeScheduledEntities(ScheduledRemovalType removalType) {
    return this.jobBuilderFactory
        .get(JOB_REMOVE_SCHEDULED_ENTITIES)
        .start(removeEntity(removalType, chunkSize, removalsStepExecutor, scheduledTaskReader))
        .build();
  }
}
