package eu.europeana.entitymanagement.batch.processor;

import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledUpdateType;
import eu.europeana.entitymanagement.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.exception.FunctionalRuntimeException;
import eu.europeana.entitymanagement.exception.ingestion.EntityUpdateException;
import eu.europeana.entitymanagement.web.model.scoring.EntityMetrics;
import eu.europeana.entitymanagement.web.service.ScoringService;
import java.util.Date;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/** Updates Metrics for EntityRecords */
@Component
public class EntityMetricsProcessor implements ItemProcessor<BatchEntityRecord, BatchEntityRecord> {

  private static final Set<ScheduledTaskType> supportedScheduledTasks =
      Set.of(ScheduledUpdateType.FULL_UPDATE, ScheduledUpdateType.METRICS_UPDATE);

  private final ScoringService scoringService;
  private final EntityManagementConfiguration entityManagementConfiguration;

  private static final Logger logger = LogManager.getLogger(EntityMetricsProcessor.class);

  public EntityMetricsProcessor(
      ScoringService scoringService, EntityManagementConfiguration entityManagementConfiguration) {
    this.scoringService = scoringService;
    this.entityManagementConfiguration = entityManagementConfiguration;
  }

  @Override
  public BatchEntityRecord process(@NonNull BatchEntityRecord entityRecord) throws Exception {
    if (supportedScheduledTasks.contains(entityRecord.getScheduledTaskType())) {
      Date now = new Date();
      if (entityRecord.getEntityRecord().getEntity().getIsAggregatedBy() == null) {
        Aggregation aggregation = new Aggregation();
        aggregation.setCreated(now);
        entityRecord.getEntityRecord().getEntity().setIsAggregatedBy(aggregation);
      }

      /*
       *  Metrics not computed by default, as it requires access to the PageRank and Search API
       *  Solr servers. To prevent Jobs from failing, we make this conditional.
       */
      if (entityManagementConfiguration.shouldComputeMetrics()) {
        if (logger.isTraceEnabled()) {
          logger.trace(
              "Computing ranking metrics for entityId={}",
              entityRecord.getEntityRecord().getEntityId());
        }

        computeRankingMetrics(entityRecord.getEntityRecord());
      }

      // Always set modified time (even if metrics weren't updated) as this is used for ETag
      // generation
      entityRecord.getEntityRecord().getEntity().getIsAggregatedBy().setModified(now);
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

    // cannot be null here as it's set in process()
    Aggregation aggregation = entity.getIsAggregatedBy();

    aggregation.setPageRank((double) metrics.getPageRank());
    aggregation.setRecordCount(metrics.getEnrichmentCount());
    aggregation.setScore(metrics.getScore());
  }
}
