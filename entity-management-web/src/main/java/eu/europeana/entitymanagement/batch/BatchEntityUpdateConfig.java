package eu.europeana.entitymanagement.batch;

import dev.morphia.query.experimental.filters.Filters;
import eu.europeana.entitymanagement.batch.processor.EntityDereferenceProcessor;
import eu.europeana.entitymanagement.batch.reader.EntityRecordDatabaseReader;
import eu.europeana.entitymanagement.batch.reader.EntityRecordExecutionContextReader;
import eu.europeana.entitymanagement.batch.writer.EntityRecordDatabaseWriter;
import eu.europeana.entitymanagement.batch.writer.EntityRecordExecutionContextWriter;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.web.service.impl.EntityRecordService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Date;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.ENTITY_RECORD_CTX_KEY;
import static eu.europeana.entitymanagement.mongo.repository.EntityRecordFields.ENTITY_ID;
import static eu.europeana.entitymanagement.mongo.repository.EntityRecordFields.ENTITY_MODIFIED;

@Component
public class BatchEntityUpdateConfig {

    private static final String SINGLE_READER_BEAN = "singleItemReader";
    private static final String MULTI_READER_BEAN = "multiItemReader";

    private static final Logger logger = LogManager.getLogger(BatchEntityUpdateConfig.class);
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final ItemReader<EntityRecord> singleItemReader;
    private final ItemReader<EntityRecord> multipleItemReader;

    private final EntityDereferenceProcessor dereferenceProcessor;
    private final EntityRecordDatabaseWriter dbWriter;
    private final EntityRecordService entityRecordService;

    private final EntityRecordExecutionContextReader contextReader;
    private final EntityRecordExecutionContextWriter contextWriter;

    @Value("${batch.chunkSize: 10}")
    private int chunkSize;

    @Autowired
    public BatchEntityUpdateConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, @Qualifier(SINGLE_READER_BEAN) ItemReader<EntityRecord> singleItemReader, @Qualifier(MULTI_READER_BEAN) ItemReader<EntityRecord> multipleItemReader, EntityDereferenceProcessor dereferenceProcessor, EntityRecordDatabaseWriter dbWriter, EntityRecordService entityRecordService, EntityRecordExecutionContextReader contextReader, EntityRecordExecutionContextWriter contextWriter) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.singleItemReader = singleItemReader;
        this.multipleItemReader = multipleItemReader;
        this.dereferenceProcessor = dereferenceProcessor;
        this.dbWriter = dbWriter;
        this.entityRecordService = entityRecordService;
        this.contextReader = contextReader;
        this.contextWriter = contextWriter;
    }

    @Bean(name = SINGLE_READER_BEAN)
    @StepScope
    private ItemReader<EntityRecord> singleEntityRecordReader(@Value("#{jobParameters[entityId]}") String entityId) {
        return new EntityRecordDatabaseReader(entityRecordService, 1,
                Filters.eq(ENTITY_ID, entityId)
        );
    }

    @Bean(name = MULTI_READER_BEAN)
    @StepScope
    private ItemReader<EntityRecord> multipleItemReader(@Value("#{jobParameters[runTime]}") Date runTime) {
        EntityRecordDatabaseReader reader = new EntityRecordDatabaseReader(entityRecordService, chunkSize,
                Filters.lte(ENTITY_MODIFIED, runTime));

        // Make ItemReader thread-safe
        final SynchronizedItemStreamReader<EntityRecord> synchronizedItemStreamReader = new SynchronizedItemStreamReader<>();
        synchronizedItemStreamReader.setDelegate(reader);

        return synchronizedItemStreamReader;
    }


    private Step metisStep(boolean singleEntity) {
        return this.stepBuilderFactory.get("metisDerefStep")
                .<EntityRecord, EntityRecord>chunk(chunkSize)
                .reader(singleEntity ? singleItemReader : multipleItemReader)
                .processor(dereferenceProcessor)
                .writer(contextWriter)
                .listener(promotionListener())
                .build();
    }


    private Step entityValidationStep() {
        return this.stepBuilderFactory.get("entityValidationStep")
                .<EntityRecord, EntityRecord>chunk(chunkSize)
                .reader(contextReader)
                .writer(dbWriter)
                .listener(promotionListener())
                .build();
    }

    /**
     * Promotes variables in a StepExecutionContext (only available within a Step) to
     * the JobExecutionContext (available for the entire Job).
     * <p>
     * This mechanism is needed to pass data between Steps
     *
     * @return
     */
    @Bean
    private ExecutionContextPromotionListener promotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{ENTITY_RECORD_CTX_KEY});
        return listener;
    }


    @Bean
    public Job updateSingleEntity() {
        logger.info("Starting update job for single entity");
        return this.jobBuilderFactory.get("singleEntityUpdateJob")
                .incrementer(new RunIdIncrementer())
                .start(metisStep(true))
                .next(entityValidationStep())
                .build();
    }

    public Job updateAllEntities() {
        logger.info("Starting update job for ALL entities");
        return this.jobBuilderFactory.get("multiEntityUpdateJob")
                .incrementer(new RunIdIncrementer())
                .start(metisStep(false))
                .next(entityValidationStep())
                .build();
    }
}
