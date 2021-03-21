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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Date;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.ENTITY_RECORD_CTX_KEY;
import static eu.europeana.entitymanagement.mongo.repository.EntityRecordFields.ENTITY_ID;
import static eu.europeana.entitymanagement.mongo.repository.EntityRecordFields.ENTITY_MODIFIED;

@Component
public class BatchEntityUpdateConfig {

    private static final Logger logger = LogManager.getLogger(BatchEntityUpdateConfig.class);

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityRecordDatabaseReader dbReader;
    private final EntityDereferenceProcessor dereferenceProcessor;
    private final EntityRecordDatabaseWriter dbWriter;
    private final EntityRecordService entityRecordService;

    private final EntityRecordExecutionContextReader contextReader;
    private final EntityRecordExecutionContextWriter contextWriter;

    @Autowired
    public BatchEntityUpdateConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, EntityRecordDatabaseReader dbReader, EntityDereferenceProcessor dereferenceProcessor, EntityRecordDatabaseWriter dbWriter, EntityRecordService entityRecordService, EntityRecordExecutionContextReader contextReader, EntityRecordExecutionContextWriter contextWriter) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.dbReader = dbReader;
        this.dereferenceProcessor = dereferenceProcessor;
        this.dbWriter = dbWriter;
        this.entityRecordService = entityRecordService;
        this.contextReader = contextReader;
        this.contextWriter = contextWriter;
    }

    @Bean
    @StepScope
    private EntityRecordDatabaseReader entityRecordReader(@Value("#{jobParameters[entityId]}") String entityId, @Value("#{jobParameters[runTime]}") Date runTime) {
        return new EntityRecordDatabaseReader(entityRecordService, 1,
                Filters.eq(ENTITY_ID, entityId),
                // only update entities last modified before job starts
                Filters.lte(ENTITY_MODIFIED, runTime)
        );
    }


    private Step metisStep() {
        return this.stepBuilderFactory.get("metisDerefStep")
                .<EntityRecord, EntityRecord>chunk(1)
                .reader(dbReader)
                .processor(dereferenceProcessor)
                .writer(contextWriter)
                .listener(promotionListener())
                .build();
    }


    private Step entityValidationStep() {
        return this.stepBuilderFactory.get("entityValidationStep")
                .<EntityRecord, EntityRecord>chunk(1)
                .reader(contextReader)
                .writer(dbWriter)
                .listener(promotionListener())
                .build();
    }

    /**
     * Promotes variables in a StepExecutionContext (only available within a Step) to
     * the JobExecutionContext (available for the entire Job).
     *
     * This mechanism is needed to pass data between Steps
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
        return this.jobBuilderFactory.get("updateJob")
                .incrementer(new RunIdIncrementer())
                .start(metisStep())
                .next(entityValidationStep())
                .build();
    }
}
