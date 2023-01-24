package eu.europeana.entitymanagement.batch.processor;

import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledUpdateType;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * This processor checks the content of Entities after consolidation and logs warnings for any
 * potential issues with the record
 */
@Component
public class EntityVerificationLogger extends BaseEntityProcessor {

  private static final Logger logger = LogManager.getLogger(EntityVerificationLogger.class);

  public EntityVerificationLogger() {
    super(ScheduledUpdateType.FULL_UPDATE, ScheduledUpdateType.METRICS_UPDATE);
  }

  @Override
  BatchEntityRecord doProcessing(@NonNull BatchEntityRecord entityRecord) throws Exception {
    checkPrefLabels(entityRecord.getEntityRecord());
    return entityRecord;
  }

  private void checkPrefLabels(EntityRecord entityRecord) {
    int consolidatedPrefLabels = entityRecord.getEntity().getPrefLabel().size();

    // mismatch occurs if the number of prefLabels in the consolidated entity is
    // less than all unique prefLabels in proxies
    long proxyPrefLabels =
        entityRecord.getProxies().stream()
            .map(p -> p.getEntity().getPrefLabel())
            .filter(Objects::nonNull)
            .flatMap(prefLabels -> prefLabels.keySet().stream())
            .distinct()
            .count();

    if (consolidatedPrefLabels < proxyPrefLabels) {
      logger.warn(
          "Consolidated entity for {} has {} prefLabels; expected {}",
          entityRecord.getEntityId(),
          consolidatedPrefLabels,
          proxyPrefLabels);
    }
  }
}
