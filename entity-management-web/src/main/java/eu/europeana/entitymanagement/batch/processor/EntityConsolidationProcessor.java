package eu.europeana.entitymanagement.batch.processor;

import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.config.DataSources;
import eu.europeana.entitymanagement.definitions.exceptions.EntityModelCreationException;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.exception.ingestion.EntityValidationException;
import eu.europeana.entitymanagement.normalization.EntityFieldsCleaner;
import eu.europeana.entitymanagement.normalization.EntityFieldsCompleteValidationGroup;
import eu.europeana.entitymanagement.normalization.EntityFieldsDataSourceProxyValidationGroup;
import eu.europeana.entitymanagement.utils.EntityObjectFactory;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * This {@link ItemProcessor} validates Entity metadata, then creates a consolidated entity by
 * merging the metadata from all data sources .
 */
@Component
public class EntityConsolidationProcessor implements ItemProcessor<EntityRecord, EntityRecord> {

  private final EntityRecordService entityRecordService;
  private final ValidatorFactory emValidatorFactory;

  private final EntityFieldsCleaner emEntityFieldCleaner;
  private final DataSources datasources;

  public EntityConsolidationProcessor(
      EntityRecordService entityRecordService,
      ValidatorFactory emValidatorFactory,
      EntityFieldsCleaner emEntityFieldCleaner,
      DataSources datasources) {
    this.entityRecordService = entityRecordService;
    this.emValidatorFactory = emValidatorFactory;
    this.emEntityFieldCleaner = emEntityFieldCleaner;
    this.datasources = datasources;
  }

  @Override
  public EntityRecord process(@NonNull EntityRecord entityRecord)
      throws EuropeanaApiException, EntityModelCreationException {

    List<EntityProxy> externalProxies = entityRecord.getExternalProxies();

    Entity externalProxyEntity = externalProxies.get(0).getEntity();
    String proxyId = externalProxies.get(0).getProxyId();
    Optional<DataSource> dataSource = datasources.getDatasource(proxyId);
    boolean isStaticDataSource = dataSource.isPresent() && dataSource.get().isStatic();

    // do not validate static data sources
    if (!isStaticDataSource) {
      validateDataSourceProxyConstraints(externalProxyEntity);
    }

    // entities from static datasources should not have multiple proxies
    if (externalProxies.size() > 1) {
      // cumulatively merge all external proxies
      for (int i = 1; i < externalProxies.size(); i++) {
        Entity secondaryProxyEntity = externalProxies.get(i).getEntity();
        // validate each proxy's metadata before merging
        validateDataSourceProxyConstraints(secondaryProxyEntity);
        externalProxyEntity =
            entityRecordService.mergeEntities(externalProxyEntity, secondaryProxyEntity);
      }
    }

    Entity europeanaProxyEntity = entityRecord.getEuropeanaProxy().getEntity();

    Entity consolidatedEntity = null;
    if (isStaticDataSource) {
      consolidatedEntity = EntityObjectFactory.createConsolidatedEntityObject(europeanaProxyEntity);
    } else {
      consolidatedEntity =
          entityRecordService.mergeEntities(europeanaProxyEntity, externalProxyEntity);
    }

    // add external proxyIds to sameAs / exactMatch
    entityRecordService.addSameReferenceLinks(
        consolidatedEntity,
        externalProxies.stream().map(EntityProxy::getProxyId).collect(Collectors.toList()));

    emEntityFieldCleaner.cleanAndNormalize(consolidatedEntity);
    entityRecordService.performReferentialIntegrity(consolidatedEntity);
    validateCompleteValidationConstraints(consolidatedEntity);
    entityRecordService.updateConsolidatedVersion(entityRecord, consolidatedEntity);

    return entityRecord;
  }

  private void validateCompleteValidationConstraints(Entity entity)
      throws EntityValidationException {
    Set<ConstraintViolation<Entity>> violations =
        emValidatorFactory
            .getValidator()
            .validate(entity, EntityFieldsCompleteValidationGroup.class);
    if (!violations.isEmpty()) {
      throw new EntityValidationException(
          "The consolidated entity contains invalid data!", violations);
    }
  }

  private void validateDataSourceProxyConstraints(Entity entity) throws EntityValidationException {
    Set<ConstraintViolation<Entity>> violations =
        emValidatorFactory
            .getValidator()
            .validate(entity, EntityFieldsDataSourceProxyValidationGroup.class);
    if (!violations.isEmpty()) {
      throw new EntityValidationException(
          "The entity from the external data source contains invalid data!", violations);
    }
  }
}
