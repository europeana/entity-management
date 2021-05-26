package eu.europeana.entitymanagement.batch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.morphia.query.experimental.filters.Filters;
import eu.europeana.entitymanagement.batch.errorhandling.EntitySkipPolicy;
import eu.europeana.entitymanagement.batch.errorhandling.FailedTaskService;
import eu.europeana.entitymanagement.batch.errorhandling.FailedTaskUtils;
import eu.europeana.entitymanagement.batch.listener.EntityUpdateListener;
import eu.europeana.entitymanagement.batch.processor.EntityDereferenceProcessor;
import eu.europeana.entitymanagement.batch.processor.EntityUpdateProcessor;
import eu.europeana.entitymanagement.batch.reader.EntityRecordDatabaseReader;
import eu.europeana.entitymanagement.batch.reader.FailedTaskDatabaseReader;
import eu.europeana.entitymanagement.batch.writer.EntityRecordDatabaseWriter;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.exception.EntityMismatchException;
import eu.europeana.entitymanagement.exception.MetisNotKnownException;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.support.CompositeItemProcessor;
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
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.*;
import static eu.europeana.entitymanagement.definitions.EntityRecordFields.*;

@Component
public class BatchEntityUpdateConfig {

    private static final String SPECIFIC_ITEM_ENTITYRECORD_READER = "specificItemEntityRecordReader";
    private static final String ALL_ITEM_ENTITYRECORD_READER = "allItemEntityRecordReader";
    private static final String FAILED_TASK_READER = "allItemEntityFailureReader";


    private static final Logger logger = LogManager.getLogger(BatchEntityUpdateConfig.class);
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final ItemReader<EntityRecord> singleItemReader;
    private final ItemReader<EntityRecord> multipleEntityRecordReader;
    private final ItemReader<EntityRecord> failedTaskReader;

    private final EntityDereferenceProcessor dereferenceProcessor;
    private final EntityUpdateProcessor entityUpdateProcessor;
    private final EntityRecordDatabaseWriter dbWriter;
    private final EntityRecordService entityRecordService;

    private final FailedTaskService failedTaskService;

    private final EntityUpdateListener entityUpdateListener;

    private final TaskExecutor stepThreadPoolExecutor;
    private final TaskExecutor synchronousTaskExecutor;

    private final ObjectMapper mapper;

    private final int chunkSize;

    private final int throttleLimit;

    //TODO: Too many dependencies. Split up into multiple classes
    @Autowired
    public BatchEntityUpdateConfig(JobBuilderFactory jobBuilderFactory,
                                   StepBuilderFactory stepBuilderFactory,
                                   @Qualifier(SPECIFIC_ITEM_ENTITYRECORD_READER) ItemReader<EntityRecord> singleEntityRecordReader,
                                   @Qualifier(ALL_ITEM_ENTITYRECORD_READER) ItemReader<EntityRecord> multipleEntityRecordReader,
                                   @Qualifier(FAILED_TASK_READER) ItemReader<EntityRecord> failedTaskReader,
                                   EntityDereferenceProcessor dereferenceProcessor,
                                   EntityUpdateProcessor entityUpdateProcessor,
                                   EntityRecordDatabaseWriter dbWriter,
                                   EntityRecordService entityRecordService,
                                   FailedTaskService failedTaskService,
                                   EntityUpdateListener entityUpdateListener,
                                   @Qualifier(BEAN_STEP_EXECUTOR) TaskExecutor stepThreadPoolExecutor,
                                   @Qualifier(SYNC_TASK_EXECUTOR) TaskExecutor synchronousTaskExecutor,
                                   @Qualifier(BEAN_JSON_MAPPER) ObjectMapper mapper,
                                   EntityManagementConfiguration emConfig) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.singleItemReader = singleEntityRecordReader;
        this.multipleEntityRecordReader = multipleEntityRecordReader;
        this.failedTaskReader = failedTaskReader;
        this.dereferenceProcessor = dereferenceProcessor;
        this.entityUpdateProcessor = entityUpdateProcessor;
        this.dbWriter = dbWriter;
        this.entityRecordService = entityRecordService;
        this.failedTaskService = failedTaskService;
        this.entityUpdateListener = entityUpdateListener;
        this.stepThreadPoolExecutor = stepThreadPoolExecutor;
        this.synchronousTaskExecutor = synchronousTaskExecutor;
        this.mapper = mapper;
        this.chunkSize = emConfig.getBatchChunkSize();
        this.throttleLimit = emConfig.getBatchStepThrottleLimit();
    }

    @Bean(name = SPECIFIC_ITEM_ENTITYRECORD_READER)
    @StepScope
    private EntityRecordDatabaseReader specificEntityRecordReader(@Value("#{jobParameters[entityIds]}") String entityIdString) throws JsonProcessingException {
        String[] entityIds = mapper.readValue(entityIdString, String[].class);
        return new EntityRecordDatabaseReader(entityRecordService, chunkSize,
                Filters.in(ENTITY_ID, Arrays.asList(entityIds))
        );
    }

    @Bean(name = ALL_ITEM_ENTITYRECORD_READER)
    @StepScope
    private SynchronizedItemStreamReader<EntityRecord> allEntityRecordReader(@Value("#{jobParameters[currentStartTime]}") Date currentStartTime) {
        EntityRecordDatabaseReader reader = new EntityRecordDatabaseReader(entityRecordService, chunkSize,
                Filters.lte(ENTITY_MODIFIED, currentStartTime),
                // temp filter during migration. Only fetch records without a consolidated entity
               Filters.exists(ENTITY_TYPE).not());

        return threadSafeReader(reader);
    }

    @Bean(name = FAILED_TASK_READER)
    @StepScope
    private SynchronizedItemStreamReader<EntityRecord> allEntityFailureReader(@Value("#{jobParameters[currentStartTime]}") Date currentStartTime) {
        FailedTaskDatabaseReader reader = new FailedTaskDatabaseReader(failedTaskService, chunkSize,
            Filters.lte(FailedTaskUtils.CREATED, currentStartTime));

        return threadSafeReader(reader);
    }


    /**
     * Creates a Composite ItemProcessor to perform Metis de-referencing and Entity update
     */
    private ItemProcessor<EntityRecord, EntityRecord> compositeMetisDerefEntityUpdateProcessor() {
        CompositeItemProcessor<EntityRecord, EntityRecord> compositeItemProcessor = new CompositeItemProcessor<>();
        compositeItemProcessor.setDelegates(Arrays.asList(dereferenceProcessor, entityUpdateProcessor));
        return compositeItemProcessor;
    }


    private Step updateEntityStep(boolean singleEntity){
        return this.stepBuilderFactory.get(STEP_UPDATE_ENTITY)
            .<EntityRecord, EntityRecord>chunk(chunkSize)
            // setting up listener for Read/Process/Write
            .listener(
                (ItemProcessListener<? super EntityRecord, ? super EntityRecord>) entityUpdateListener)
            .reader(singleEntity ? singleItemReader : multipleEntityRecordReader)
            .processor(compositeMetisDerefEntityUpdateProcessor())
                .faultTolerant()
                .skipPolicy(new EntitySkipPolicy())
                .skip(EntityMismatchException.class)
                .skip(MetisNotKnownException.class)
            .writer(dbWriter)
            .taskExecutor(singleEntity ? synchronousTaskExecutor : stepThreadPoolExecutor)
                .throttleLimit(throttleLimit)
            .build();
    }

    private Step retryFailedEntitiesStep() {
        return this.stepBuilderFactory.get(STEP_RETRY_FAILED_ENTITIES)
            .<EntityRecord, EntityRecord>chunk(chunkSize)
            .reader(failedTaskReader)
            .processor(compositeMetisDerefEntityUpdateProcessor())
            .writer(dbWriter)
            .listener(
                (ItemProcessListener<? super EntityRecord, ? super EntityRecord>) entityUpdateListener)
                .faultTolerant()
                .skipPolicy(new EntitySkipPolicy())
                .skip(EntityMismatchException.class)
                .skip(MetisNotKnownException.class)
            .taskExecutor(stepThreadPoolExecutor)
                .throttleLimit(throttleLimit)
            .build();
    }


    Job updateSpecificEntities() {
        logger.info("Starting update job for specific entities");
        return this.jobBuilderFactory.get(JOB_UPDATE_SPECIFIC_ENTITIES)
                .incrementer(new RunIdIncrementer())
                .start(updateEntityStep(true))
                .build();
    }


    Job updateAllEntities() {
        logger.info("Starting update job for ALL entities");
        return this.jobBuilderFactory.get(JOB_UPDATE_ALL_ENTITIES)
                .start(updateEntityStep(false))
                .build();
    }

    Job retryFailedTasks(){
        logger.info("Starting job to updated entities in FailedTasks collection");
        return this.jobBuilderFactory.get(JOB_RETRY_FAILED_ENTITIES)
            .start(retryFailedEntitiesStep())
            .build();
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
}
