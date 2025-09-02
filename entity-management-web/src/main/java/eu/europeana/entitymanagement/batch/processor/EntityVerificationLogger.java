package eu.europeana.entitymanagement.batch.processor;

import eu.europeana.entitymanagement.definitions.LanguageCodes;
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.exception.EntityMismatchException;
import java.util.Objects;
import java.util.Set;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.BEAN_ENTITY_VERIFICATION_LOGGER;

/**
 * This processor checks the content of Entities after consolidation and logs warnings for any
 * potential issues with the record
 */
@Component(BEAN_ENTITY_VERIFICATION_LOGGER)
public class EntityVerificationLogger extends BaseEntityProcessor {

  private final Set<String> supportedLanguageCodes;

  public EntityVerificationLogger(LanguageCodes languageCodes) {
    this.supportedLanguageCodes = languageCodes.getSupportedLangCodes();
  }

  @Override
  BatchEntityRecord doProcessing(@NonNull BatchEntityRecord entityRecord) throws Exception {
    checkPrefLabels(entityRecord.getEntityRecord());
    return entityRecord;
  }

  /**
   * Checks if the number of prefLabels in the consolidated entity matches unique prefLabels in all
   * proxies. Only prefLabels for supported languages are taken into account.
   *
   * @param entityRecord record being processed
   * @throws EntityMismatchException if prefLabel counts do not match
   */
  private void checkPrefLabels(EntityRecord entityRecord) throws EntityMismatchException {

    int consolidatedPrefLabels = entityRecord.getEntity().getPrefLabel().size();

    long proxyPrefLabels =
        entityRecord.getProxies().stream()
            .map(p -> p.getEntity().getPrefLabel())
            .filter(Objects::nonNull)
            .flatMap(prefLabels -> prefLabels.keySet().stream())
            .filter(supportedLanguageCodes::contains)
            .distinct()
            .count();

    if (consolidatedPrefLabels != proxyPrefLabels) {
      throw new EntityMismatchException(
          String.format(
              "Consolidated entity for %s has %d prefLabels; expected %d",
              entityRecord.getEntityId(), consolidatedPrefLabels, proxyPrefLabels));
    }
  }
}
