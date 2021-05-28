package eu.europeana.entitymanagement.batch.processor;

import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.exception.FunctionalRuntimeException;
import eu.europeana.entitymanagement.exception.ingestion.EntityUpdateException;
import eu.europeana.entitymanagement.web.model.scoring.EntityMetrics;
import eu.europeana.entitymanagement.web.service.ScoringService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Updates Metrics for EntityRecords
 */
@Component
public class EntityMetricsProcessor implements ItemProcessor<EntityRecord, EntityRecord> {

  private final ScoringService scoringService;
  private final EntityManagementConfiguration entityManagementConfiguration;

  private static final Logger logger = LogManager.getLogger(EntityMetricsProcessor.class);

  public EntityMetricsProcessor(
      ScoringService scoringService,
      EntityManagementConfiguration entityManagementConfiguration) {
    this.scoringService = scoringService;
    this.entityManagementConfiguration = entityManagementConfiguration;
  }


  @Override
  public EntityRecord process(@NonNull EntityRecord entityRecord) throws Exception {
    /*
     *  Metrics not computed by default, as it requires access to the PageRank and Search API
     *  Solr servers. To prevent Jobs from failing, we make this conditional.
     */
    if(entityManagementConfiguration.shouldComputeMetrics()){
      if(logger.isTraceEnabled()) {
        logger.trace("Computing ranking metrics for entityId={}", entityRecord.getEntityId());
      }

      computeRankingMetrics(entityRecord);
    }

    return entityRecord;
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
      aggregation = new Aggregation();
      entity.setIsAggregatedBy(aggregation);
    }

    aggregation.setPageRank(metrics.getPageRank());
    aggregation.setRecordCount(metrics.getEnrichmentCount());
    aggregation.setScore(metrics.getScore());
    aggregation.setModified(new Date());
  }
}
