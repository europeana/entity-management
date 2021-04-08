package eu.europeana.entitymanagement.batch.processor;

import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.impl.AggregationImpl;
import eu.europeana.entitymanagement.exception.FunctionalRuntimeException;
import eu.europeana.entitymanagement.exception.ingestion.EntityUpdateException;
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

    private static final Logger logger = LogManager.getLogger(EntityUpdateProcessor.class);

    public EntityUpdateProcessor(EntityRecordService entityRecordService,
        ValidatorFactory emValidatorFactory, ScoringService scoringService,
        EntityManagementConfiguration entityManagementConfiguration) {
        this.entityRecordService = entityRecordService;
        this.emValidatorFactory = emValidatorFactory;
        this.scoringService = scoringService;
        this.entityManagementConfiguration = entityManagementConfiguration;
    }

    @Override
    public EntityRecord process(@NonNull EntityRecord entityRecord) throws Exception {
        logger.debug("Creating consolidated proxy for entityId={} ", entityRecord.getEntityId());
        entityRecordService.mergeEntity(entityRecord);

        logger.debug("Validating constraints for entityId={}", entityRecord.getEntityId());
        validateConstraints(entityRecord);

        logger.debug("Checking referential integrity for entityId={}", entityRecord.getEntityId());
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

    private void validateConstraints(EntityRecord entityRecord) {
        Set<ConstraintViolation<Entity>> violations = emValidatorFactory.getValidator().validate(entityRecord.getEntity());
        if (!violations.isEmpty()) {
            logger.warn("Entity validation failed for entityId={}", entityRecord.getEntityId());
            for (ConstraintViolation<Entity> violation : violations) {
                logger.warn("entityId={} - {}", entityRecord.getEntity(), violation.getMessage());
            }
            //TODO: throw here
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
