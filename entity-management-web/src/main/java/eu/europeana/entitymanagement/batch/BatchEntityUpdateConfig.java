package eu.europeana.entitymanagement.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.morphia.query.experimental.filters.Filters;
import eu.europeana.entitymanagement.batch.listener.EntityUpdateListener;
import eu.europeana.entitymanagement.batch.processor.EntityDereferenceProcessor;
import eu.europeana.entitymanagement.batch.processor.EntityMetricsProcessor;
import eu.europeana.entitymanagement.batch.processor.EntityUpdateProcessor;
import eu.europeana.entitymanagement.batch.reader.EntityRecordDatabaseReader;
import eu.europeana.entitymanagement.batch.service.FailedTaskService;
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

import static eu.europeana.entitymanagement.batch.BatchUtils.JOB_UPDATE_SPECIFIC_ENTITIES;
import static eu.europeana.entitymanagement.batch.BatchUtils.STEP_UPDATE_ENTITY;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.*;
import static eu.europeana.entitymanagement.definitions.EntityRecordFields.ENTITY_ID;

@Component
public class BatchEntityUpdateConfig {

    private static final String SINGLE_ENTITYRECORD_READER = "specificItemEntityRecordReader";
    private static final String ALL_ITEM_ENTITYRECORD_READER = "allItemEntityRecordReader";
    private static final String FAILED_TASK_READER = "allItemEntityFailureReader";


    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final ItemReader<EntityRecord> singleEntityRecordReader;

    private final EntityDereferenceProcessor dereferenceProcessor;
    private final EntityUpdateProcessor entityUpdateProcessor;
    private final EntityMetricsProcessor entityMetricsProcessor;
    private final EntityRecordDatabaseWriter dbWriter;
    private final EntitySolrWriter solrWriter;
    private final EntityRecordService entityRecordService;

    private final FailedTaskService failedTaskService;

    private final EntityUpdateListener entityUpdateListener;

    private final TaskExecutor stepThreadPoolExecutor;
    private final TaskExecutor synchronousTaskExecutor;

    private final ObjectMapper mapper;

    private final int chunkSize;

    private final int throttleLimit;

    /**
     * SkipPolicy to ignore all failures when executing jobs, as they can be handled later
     */
    private final SkipPolicy noopSkipPolicy = (Throwable t, int skipCount) -> true;

    //TODO: Too many dependencies. Split up into multiple classes
    @Autowired
    public BatchEntityUpdateConfig(JobBuilderFactory jobBuilderFactory,
                                   StepBuilderFactory stepBuilderFactory,
                                   @Qualifier(SINGLE_ENTITYRECORD_READER) ItemReader<EntityRecord> singleEntityRecordReader,
                                   EntityDereferenceProcessor dereferenceProcessor,
                                   EntityUpdateProcessor entityUpdateProcessor,
                                   EntityMetricsProcessor entityMetricsProcessor,
                                   EntityRecordDatabaseWriter dbWriter,
                                   EntitySolrWriter solrWriter, EntityRecordService entityRecordService,
                                   FailedTaskService failedTaskService,
                                   EntityUpdateListener entityUpdateListener,
                                   @Qualifier(BEAN_STEP_EXECUTOR) TaskExecutor stepThreadPoolExecutor,
                                   @Qualifier(SYNC_TASK_EXECUTOR) TaskExecutor synchronousTaskExecutor,
                                   @Qualifier(BEAN_JSON_MAPPER) ObjectMapper mapper,
                                   EntityManagementConfiguration emConfig) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.singleEntityRecordReader = singleEntityRecordReader;
        this.dereferenceProcessor = dereferenceProcessor;
        this.entityUpdateProcessor = entityUpdateProcessor;
        this.entityMetricsProcessor = entityMetricsProcessor;
        this.dbWriter = dbWriter;
        this.solrWriter = solrWriter;
        this.entityRecordService = entityRecordService;
        this.failedTaskService = failedTaskService;
        this.entityUpdateListener = entityUpdateListener;
        this.stepThreadPoolExecutor = stepThreadPoolExecutor;
        this.synchronousTaskExecutor = synchronousTaskExecutor;
        this.mapper = mapper;
        this.chunkSize = emConfig.getBatchChunkSize();
        this.throttleLimit = emConfig.getBatchStepThrottleLimit();
    }

    /**
     * ItemReader that queries by entityId when retrieving EntityRecords from the database
     */
    @Bean(name = SINGLE_ENTITYRECORD_READER)
    @StepScope
    private EntityRecordDatabaseReader singleEntityRecordReader(@Value("#{jobParameters[entityId]}") String entityIdString)  {
        return new EntityRecordDatabaseReader(entityRecordService, chunkSize,
                Filters.eq(ENTITY_ID, entityIdString)
        );
    }



    /**
     * Creates a Composite ItemProcessor to perform Metis de-referencing, metadata update, and
     * metrics update.
     *
     * These are implemented as processors, instead of discrete steps to eliminate the overhead required
     * to pass data between steps (via the Execution context).
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


    private Step updateSingleEntityStep(){
        return this.stepBuilderFactory.get(STEP_UPDATE_ENTITY)
            .<EntityRecord, EntityRecord>chunk(1)
            // setting up listener for Read/Process/Write
            .listener(
                (ItemProcessListener<? super EntityRecord, ? super EntityRecord>) entityUpdateListener)
            .reader(singleEntityRecordReader)
            .processor(compositeUpdateProcessor())
                .faultTolerant()
                .skipPolicy(noopSkipPolicy)
                .skip(EntityMismatchException.class)
                .skip(MetisNotKnownException.class)
                .skip(SolrServiceException.class)
            .writer(compositeEntityWriter())
            .taskExecutor(synchronousTaskExecutor)
            .build();
    }

    Job updateSingleEntityJob() {
        return this.jobBuilderFactory.get(JOB_UPDATE_SPECIFIC_ENTITIES)
                .incrementer(new RunIdIncrementer())
                .start(updateSingleEntityStep())
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
