package eu.europeana.entitymanagement.batch;

import static eu.europeana.entitymanagement.batch.BatchUtils.JOB_UPDATE_ALL_ENTITIES;
import static eu.europeana.entitymanagement.batch.BatchUtils.JOB_UPDATE_SPECIFIC_ENTITIES;
import static eu.europeana.entitymanagement.batch.BatchUtils.STEP_UPDATE_ENTITY;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.BEAN_JSON_MAPPER;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.BEAN_STEP_EXECUTOR;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.SYNC_TASK_EXECUTOR;
import static eu.europeana.entitymanagement.mongo.repository.EntityRecordFields.ENTITY_ID;
import static eu.europeana.entitymanagement.mongo.repository.EntityRecordFields.ENTITY_MODIFIED;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.morphia.query.experimental.filters.Filters;
import eu.europeana.entitymanagement.batch.processor.EntityDereferenceProcessor;
import eu.europeana.entitymanagement.batch.processor.EntityUpdateProcessor;
import eu.europeana.entitymanagement.batch.reader.EntityRecordDatabaseReader;
import eu.europeana.entitymanagement.batch.writer.EntityRecordDatabaseWriter;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import java.util.Arrays;
import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class BatchEntityUpdateConfig {

    private static final String SPECIFIC_ITEM_READER = "specificItemReader";
    private static final String ALL_ITEM_READER = "allItemReader";

    private static final Logger logger = LogManager.getLogger(BatchEntityUpdateConfig.class);
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final ItemReader<EntityRecord> singleItemReader;
    private final ItemReader<EntityRecord> multipleItemReader;

    private final EntityDereferenceProcessor dereferenceProcessor;
    private final EntityUpdateProcessor entityUpdateProcessor;
    private final EntityRecordDatabaseWriter dbWriter;
    private final EntityRecordService entityRecordService;

    private final TaskExecutor stepThreadPoolExecutor;
    private final TaskExecutor synchronousTaskExecutor;

    private final ObjectMapper mapper;

    private final int chunkSize;

    //TODO: Too many dependencies. Split up into multiple classes
    @Autowired
    public BatchEntityUpdateConfig(JobBuilderFactory jobBuilderFactory,
        StepBuilderFactory stepBuilderFactory,
        @Qualifier(SPECIFIC_ITEM_READER) ItemReader<EntityRecord> singleItemReader,
        @Qualifier(ALL_ITEM_READER) ItemReader<EntityRecord> multipleItemReader,
        EntityDereferenceProcessor dereferenceProcessor,
        EntityUpdateProcessor entityUpdateProcessor,
        EntityRecordDatabaseWriter dbWriter,
        EntityRecordService entityRecordService,
        @Qualifier(BEAN_STEP_EXECUTOR) TaskExecutor stepThreadPoolExecutor,
        @Qualifier(SYNC_TASK_EXECUTOR)TaskExecutor synchronousTaskExecutor,
        @Qualifier(BEAN_JSON_MAPPER) ObjectMapper mapper,
        EntityManagementConfiguration emConfig) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.singleItemReader = singleItemReader;
        this.multipleItemReader = multipleItemReader;
        this.dereferenceProcessor = dereferenceProcessor;
        this.entityUpdateProcessor = entityUpdateProcessor;
        this.dbWriter = dbWriter;
        this.entityRecordService = entityRecordService;
        this.stepThreadPoolExecutor = stepThreadPoolExecutor;
        this.synchronousTaskExecutor = synchronousTaskExecutor;
        this.mapper = mapper;
        this.chunkSize = emConfig.getBatchChunkSize();
    }

    @Bean(name = SPECIFIC_ITEM_READER)
    @StepScope
    private EntityRecordDatabaseReader specificEntityRecordReader(@Value("#{jobParameters[entityIds]}") String entityIdString) throws JsonProcessingException {
        String[] entityIds = mapper.readValue(entityIdString, String[].class);
        return new EntityRecordDatabaseReader(entityRecordService, chunkSize,
                Filters.in(ENTITY_ID, Arrays.asList(entityIds))
        );
    }

    @Bean(name = ALL_ITEM_READER)
    @StepScope
    private SynchronizedItemStreamReader<EntityRecord> allEntityRecordReader(@Value("#{jobParameters[currentStartTime]}") Date currentStartTime) {
        EntityRecordDatabaseReader reader = new EntityRecordDatabaseReader(entityRecordService, chunkSize,
                Filters.lte(ENTITY_MODIFIED, currentStartTime));

        // Make ItemReader thread-safe
        final SynchronizedItemStreamReader<EntityRecord> synchronizedItemStreamReader = new SynchronizedItemStreamReader<>();
        synchronizedItemStreamReader.setDelegate(reader);
        return synchronizedItemStreamReader;
    }


    /**
     * Creates a Composite ItemProcessor to perform Metis de-referencing and Entity update in a single
     * step.
     */
    @Bean
    private ItemProcessor<EntityRecord, EntityRecord> compositeItemProcessor() {
        CompositeItemProcessor<EntityRecord, EntityRecord> compositeItemProcessor = new CompositeItemProcessor<>();
        compositeItemProcessor.setDelegates(Arrays.asList(dereferenceProcessor, entityUpdateProcessor));
        return compositeItemProcessor;
    }


    private Step updateEntityStep(boolean singleEntity){
        return this.stepBuilderFactory.get(STEP_UPDATE_ENTITY)
            .<EntityRecord, EntityRecord>chunk(chunkSize)
            .reader(singleEntity ? singleItemReader : multipleItemReader)
            .processor(compositeItemProcessor())
            .writer(dbWriter)
            .taskExecutor(singleEntity ? synchronousTaskExecutor : stepThreadPoolExecutor)
            .build();
    }

    @Bean
    Job updateSpecificEntities() {
        logger.info("Starting update job for specific entities");
        return this.jobBuilderFactory.get(JOB_UPDATE_SPECIFIC_ENTITIES)
                .incrementer(new RunIdIncrementer())
                .start(updateEntityStep(true))
                .build();
    }

    @Bean
    Job updateAllEntities() {
        logger.info("Starting update job for ALL entities");
        return this.jobBuilderFactory.get(JOB_UPDATE_ALL_ENTITIES)
                .start(updateEntityStep(false))
                .build();
    }
}
