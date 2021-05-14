package eu.europeana.entitymanagement.batch.processor;

import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.impl.AggregationImpl;
import eu.europeana.entitymanagement.exception.FunctionalRuntimeException;
import eu.europeana.entitymanagement.exception.ingestion.EntityUpdateException;
import eu.europeana.entitymanagement.exception.ingestion.EntityValidationException;
import eu.europeana.entitymanagement.normalization.EntityFieldsCleaner;
import eu.europeana.entitymanagement.web.model.scoring.EntityMetrics;
import eu.europeana.entitymanagement.web.service.ScoringService;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;
import java.util.Date;
import java.util.Set;

/**
 * This {@link ItemProcessor} updates Entity metadata.
 */
@Component
public class EntityUpdateProcessor implements ItemProcessor<EntityRecord, EntityRecord> {
    private final EntityRecordService entityRecordService;
    private final ValidatorFactory emValidatorFactory;
    private final ScoringService scoringService;
    private final EntityManagementConfiguration entityManagementConfiguration;
    private final EntityFieldsCleaner emEntityFieldCleaner;

    private static final Logger logger = LogManager.getLogger(EntityUpdateProcessor.class);

    public EntityUpdateProcessor(EntityRecordService entityRecordService,
        ValidatorFactory emValidatorFactory, 
        EntityFieldsCleaner emEntityFieldCleaner,
        ScoringService scoringService,
        EntityManagementConfiguration entityManagementConfiguration) {
        this.entityRecordService = entityRecordService;
        this.emValidatorFactory = emValidatorFactory;
        this.emEntityFieldCleaner = emEntityFieldCleaner;
        this.scoringService = scoringService;
        this.entityManagementConfiguration = entityManagementConfiguration;
    }

    @Override
    public EntityRecord process(@NonNull EntityRecord entityRecord) throws Exception {
        //TODO: Validate entity metadata from Proxy Data Source
        emEntityFieldCleaner.cleanAndNormalize(entityRecord.getExternalProxy().getEntity());
	      entityRecordService.mergeEntity(entityRecord);
        validateConstraints(entityRecord);
        entityRecordService.performReferentialIntegrity(entityRecord.getEntity());

      /*
       *  Metrics not computed by default, as it requires access to the PageRank and Search API
       *  Solr servers. To prevent Jobs from failing, we make this conditional.
       */
        if(entityManagementConfiguration.shouldComputeMetrics()){
            logger.debug("Computing ranking metrics for entityId={}", entityRecord.getEntityId());
            computeRankingMetrics(entityRecord);
        }

        return entityRecord;
    }

    private void validateConstraints(EntityRecord entityRecord) throws EntityValidationException {
        Set<ConstraintViolation<Entity>> violations = emValidatorFactory.getValidator().validate(entityRecord.getEntity());
        if (!violations.isEmpty()) {
            //TODO: throw exception here when the implementation is stable and correct
            logger.debug("The record with the following id has constraint validation errors: " + entityRecord.getEntityId());
        }
    }


    private void computeRankingMetrics(EntityRecord entityRecord) throws EntityUpdateException {
        Entity entity = entityRecord.getEntity();
        if (entity == null) {
            throw new EntityUpdateException(
                    "An entity object needs to be available in EntityRecord in order to compute the scoring metrics!");
        }


        EntityMetrics metrics;
        try {
            metrics = scoringService.computeMetrics(entity);
        } catch (FunctionalRuntimeException | UnsupportedEntityTypeException e) {
            throw new EntityUpdateException(
                    "Cannot compute ranking metrics for entityId=" + entity.getEntityId(), e);
        }

        Aggregation aggregation = entity.getIsAggregatedBy();
        if (aggregation == null) {
            aggregation = new AggregationImpl();
            entity.setIsAggregatedBy(aggregation);
        }

        aggregation.setPageRank(metrics.getPageRank());
        aggregation.setRecordCount(metrics.getEnrichmentCount());
        aggregation.setScore(metrics.getScore());
        aggregation.setModified(new Date());
    }
}
