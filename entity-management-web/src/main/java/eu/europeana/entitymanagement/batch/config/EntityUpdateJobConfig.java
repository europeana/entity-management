package eu.europeana.entitymanagement.batch.config;

import static eu.europeana.entitymanagement.batch.utils.BatchUtils.JOB_REMOVE_SCHEDULED_ENTITIES;
import static eu.europeana.entitymanagement.batch.utils.BatchUtils.JOB_UPDATE_SCHEDULED_ENTITIES;
import static eu.europeana.entitymanagement.batch.utils.BatchUtils.JOB_UPDATE_SINGLE_ENTITY;
import static eu.europeana.entitymanagement.batch.utils.BatchUtils.STEP_REMOVE_ENTITY;
import static eu.europeana.entitymanagement.batch.utils.BatchUtils.STEP_UPDATE_ENTITY;
import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.REMOVALS_STEP_EXECUTOR;
import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.UPDATES_STEP_EXECUTOR;
import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.WEB_REQUEST_JOB_EXECUTOR;
import static eu.europeana.entitymanagement.definitions.EntityRecordFields.ENTITY_ID;
import static eu.europeana.entitymanagement.definitions.batch.EMBatchConstants.UPDATE_TYPE;

import dev.morphia.query.experimental.filters.Filters;
import eu.europeana.entitymanagement.batch.listener.EntityUpdateStepListener;
import eu.europeana.entitymanagement.batch.listener.ScheduledTaskItemListener;
import eu.europeana.entitymanagement.batch.processor.EntityConsolidationProcessor;
import eu.europeana.entitymanagement.batch.processor.EntityDereferenceProcessor;
import eu.europeana.entitymanagement.batch.processor.EntityMetricsProcessor;
import eu.europeana.entitymanagement.batch.processor.EntityVerificationLogger;
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
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledRemovalType;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledUpdateType;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

  private final ItemReader<BatchEntityRecord> singleEntityRecordReader;

  private final ItemReader<BatchEntityRecord> scheduledTaskReader;
  private final EntityDereferenceProcessor dereferenceProcessor;
  private final EntityConsolidationProcessor entityUpdateProcessor;
  private final EntityVerificationLogger entityVerificationLogger;

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

  private final int configuredBatchChunkSize;

  private final int updatesThrottleLimit;
  private final int removalsThrottleLimit;

  private final int maxFailedTaskRetries;

  /** SkipPolicy to ignore all failures when executing jobs, as they can be handled later */
  private final SkipPolicy noopSkipPolicy = (Throwable t, int skipCount) -> true;

  // TODO: Too many dependencies. Split up into multiple classes
  @Autowired
  public EntityUpdateJobConfig(
      JobBuilderFactory jobBuilderFactory,
      StepBuilderFactory stepBuilderFactory,
      @Qualifier(SINGLE_ENTITY_RECORD_READER)
          ItemReader<BatchEntityRecord> singleEntityRecordReader,
      @Qualifier(SCHEDULED_TASK_READER) ItemReader<BatchEntityRecord> scheduledTaskReader,
      EntityDereferenceProcessor dereferenceProcessor,
      EntityConsolidationProcessor entityUpdateProcessor,
      EntityVerificationLogger entityVerificationLogger,
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
    this.entityVerificationLogger = entityVerificationLogger;
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
    this.configuredBatchChunkSize = emConfig.getBatchChunkSize();
    this.updatesThrottleLimit = emConfig.getBatchUpdatesThrottleLimit();
    removalsThrottleLimit = emConfig.getBatchRemovalsThrottleLimit();
    maxFailedTaskRetries = emConfig.getMaxFailedTaskRetries();
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
      @Value("#{jobParameters[entityId]}") String entityIdString,
      @Value("#{jobParameters[updateType]}") String updateType) {
    return new EntityRecordDatabaseReader(
        updateType,
        entityRecordService,
        configuredBatchChunkSize,
        Filters.eq(ENTITY_ID, entityIdString));
  }

  @Bean(name = SCHEDULED_TASK_READER)
  @StepScope
  private SynchronizedItemStreamReader<BatchEntityRecord> scheduledTaskReader(
      @Value("#{jobParameters[currentStartTime]}") Date currentStartTime,
      @Value("#{jobParameters[updateType]}") String updateType) {

    List<String> updateTypeList =
        Stream.of(updateType.split(",")).map(String::trim).collect(Collectors.toList());

    ScheduledTaskDatabaseReader reader =
        new ScheduledTaskDatabaseReader(
            scheduledTaskService,
            configuredBatchChunkSize,
            Filters.lte(EMBatchConstants.CREATED, currentStartTime),
            Filters.in(UPDATE_TYPE, updateTypeList));

    return threadSafeReader(reader);
  }

  @Bean
  @StepScope
  /*
   * Creates a listener that's called while processing a single item
   *
   * JobParameters cannot be boolean, so the isSynchronous value is converted from its string representation
   */
  private ScheduledTaskItemListener entityUpdateListener(
      // see JobParameter enum for string values
      @Value("#{jobParameters[isSynchronous]}") String isSynchronousString) {
    return new ScheduledTaskItemListener(
        failedTaskService, scheduledTaskService, Boolean.parseBoolean(isSynchronousString));
  }

  /** Creates a StepExecutionListener that's called before / after the step runs */
  private StepExecutionListener stepExecutionListener(
      List<? extends ScheduledTaskType> updateType, boolean isSynchronous) {
    return new EntityUpdateStepListener(
        scheduledTaskService, updateType, isSynchronous, maxFailedTaskRetries);
  }

  /**
   * Creates a Composite ItemProcessor to perform Metis de-referencing, metadata update, and metrics
   * update.
   *
   * <p>These are implemented as processors, instead of discrete steps to eliminate the overhead
   * required to pass data between steps via the Execution context.
   */
  @Bean
  private ItemProcessor<BatchEntityRecord, BatchEntityRecord> compositeUpdateProcessor() {
    CompositeItemProcessor<BatchEntityRecord, BatchEntityRecord> compositeItemProcessor =
        new CompositeItemProcessor<>();
    compositeItemProcessor.setDelegates(
        Arrays.asList(
            dereferenceProcessor,
            entityUpdateProcessor,
            entityMetricsProcessor,
            entityVerificationLogger));
    return compositeItemProcessor;
  }

  /** Creates a Composite ItemWriter for persisting Entities to Mongo and Solr */
  private ItemWriter<BatchEntityRecord> compositeEntityInsertionWriter() {
    CompositeItemWriter<BatchEntityRecord> compositeWriter = new CompositeItemWriter<>();
    compositeWriter.setDelegates(Arrays.asList(dbInsertionWriter, solrInsertionWriter));
    return compositeWriter;
  }

  private ItemWriter<BatchEntityRecord> compositeEntityDeprecationDeletionWriter() {
    CompositeItemWriter<BatchEntityRecord> compositeWriter = new CompositeItemWriter<>();
    compositeWriter.setDelegates(
        Arrays.asList(dbDeprecationWriter, dbRemovalWriter, solrRemovalWriter));
    return compositeWriter;
  }

  /**
   * Step for updating entities. Uses a different reader and processor, based on the value of
   * isSynchronous.
   *
   * @param updateType types of update
   * @param isSynchronous indicates whether this update is executed synchronously or async
   * @return step
   */
  private Step updateEntity(List<ScheduledUpdateType> updateType, boolean isSynchronous) {

    // use different thread executor, reader and chunkSize for sync / async requests
    ItemReader<BatchEntityRecord> reader =
        isSynchronous ? singleEntityRecordReader : scheduledTaskReader;
    TaskExecutor executor = isSynchronous ? synchronousTaskExecutor : updatesStepExecutor;
    // synchronous requests only update a single entity
    int chunkSize = isSynchronous ? 1 : configuredBatchChunkSize;

    SimpleStepBuilder<BatchEntityRecord, BatchEntityRecord> step =
        this.stepBuilderFactory
            .get(STEP_UPDATE_ENTITY)
            .<BatchEntityRecord, BatchEntityRecord>chunk(chunkSize)
            .reader(reader);

    step.processor(compositeUpdateProcessor());

    return step.writer(compositeEntityInsertionWriter())
        .listener(
            (ItemProcessListener<? super BatchEntityRecord, ? super BatchEntityRecord>)
                itemListener)
        .faultTolerant()
        .skipPolicy(noopSkipPolicy)
        .taskExecutor(executor)
        .throttleLimit(updatesThrottleLimit)
        .listener(stepExecutionListener(updateType, isSynchronous))
        .build();
  }

  private Step removeEntity(
      List<ScheduledRemovalType> removalType,
      int chunkSize,
      TaskExecutor executor,
      ItemReader<BatchEntityRecord> reader) {

    SimpleStepBuilder<BatchEntityRecord, BatchEntityRecord> step =
        this.stepBuilderFactory
            .get(STEP_REMOVE_ENTITY)
            .<BatchEntityRecord, BatchEntityRecord>chunk(chunkSize)
            .reader(reader);

    step.writer(compositeEntityDeprecationDeletionWriter());

    return step.listener(
            (ItemProcessListener<? super BatchEntityRecord, ? super BatchEntityRecord>)
                itemListener)
        .faultTolerant()
        .skipPolicy(noopSkipPolicy)
        .taskExecutor(executor)
        .throttleLimit(removalsThrottleLimit)
        // removal steps are always async
        .listener(stepExecutionListener(removalType, false))
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
        .start(updateEntity(List.of(ScheduledUpdateType.FULL_UPDATE), true))
        .build();
  }

  /**
   * Job for updating entities scheduled via the ScheduledTasks collection Expects
   * `currentStartTime` date and `updateType` string in JobParameters.
   */
  public Job updateScheduledEntities(List<ScheduledUpdateType> updateType) {
    return this.jobBuilderFactory
        .get(JOB_UPDATE_SCHEDULED_ENTITIES)
        // This job is always launched via a @Scheduled method.
        .start(updateEntity(updateType, false))
        .build();
  }

  public Job removeScheduledEntities(List<ScheduledRemovalType> removalType) {
    return this.jobBuilderFactory
        .get(JOB_REMOVE_SCHEDULED_ENTITIES)
        .start(
            removeEntity(
                removalType, configuredBatchChunkSize, removalsStepExecutor, scheduledTaskReader))
        .build();
  }
}
