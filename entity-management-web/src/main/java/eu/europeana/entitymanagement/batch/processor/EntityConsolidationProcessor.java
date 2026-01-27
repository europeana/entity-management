package eu.europeana.entitymanagement.batch.processor;

import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.BEAN_ENTITY_CONSOLIDATION_PROCESSOR;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.config.DataSources;
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.definitions.exceptions.EntityModelCreationException;
import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.WebResource;
import eu.europeana.entitymanagement.exception.ingestion.EntityValidationException;
import eu.europeana.entitymanagement.normalization.EntityFieldsCleaner;
import eu.europeana.entitymanagement.normalization.EntityFieldsCompleteValidationGroup;
import eu.europeana.entitymanagement.normalization.EntityFieldsDataSourceProxyValidationGroup;
import eu.europeana.entitymanagement.utils.EntityObjectFactory;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.web.service.DepictionGeneratorService;
import eu.europeana.entitymanagement.web.service.EntityRecordService;

/**
 * This {@link ItemProcessor} validates Entity metadata, then creates a consolidated entity by
 * merging the metadata from all data sources .
 */
@Component(BEAN_ENTITY_CONSOLIDATION_PROCESSOR)
public class EntityConsolidationProcessor extends BaseEntityProcessor {

  private final EntityRecordService entityRecordService;
  private final ValidatorFactory emValidatorFactory;
  private final EntityFieldsCleaner emEntityFieldCleaner;
  private final DataSources datasources;
  private final DepictionGeneratorService depictionGeneratorService;

  public EntityConsolidationProcessor(
      EntityRecordService entityRecordService,
      ValidatorFactory emValidatorFactory,
      EntityFieldsCleaner emEntityFieldCleaner,
      DataSources datasources,
      DepictionGeneratorService depictionGeneratorService) {
    this.entityRecordService = entityRecordService;
    this.emValidatorFactory = emValidatorFactory;
    this.emEntityFieldCleaner = emEntityFieldCleaner;
    this.datasources = datasources;
    this.depictionGeneratorService = depictionGeneratorService;
  }

  public BatchEntityRecord doProcessing(BatchEntityRecord batchEntityRecord)
      throws EuropeanaApiException, EntityModelCreationException {

    EntityRecord entityRecord = batchEntityRecord.getEntityRecord();
    List<EntityProxy> externalProxies = entityRecord.getExternalProxies();

    EntityProxy primaryExternalProxy = externalProxies.get(0);
    Entity externalProxyEntity = primaryExternalProxy.getEntity();

    String proxyId = primaryExternalProxy.getProxyId();
    Optional<DataSource> dataSource = datasources.getDatasource(proxyId);
    boolean isStaticDataSource = dataSource.isPresent() && dataSource.get().isStatic();
    
    // do not validate static data sources
    if (!isStaticDataSource) {
      validateDataSourceProxyConstraints(externalProxyEntity);
    }

    // entities from static datasources should not have multiple proxies
    if (externalProxies.size() > 1) {

      // cumulatively merge all external proxies
      EntityProxy secondaryExternalProxy;
      Entity secondaryProxyEntity;
      for (int i = 1; i < externalProxies.size(); i++) {
        secondaryExternalProxy = externalProxies.get(i);
        secondaryProxyEntity = secondaryExternalProxy.getEntity();
        // validate each proxy's metadata before merging
        validateDataSourceProxyConstraints(secondaryProxyEntity);
        externalProxyEntity =
            entityRecordService.mergeEntities(externalProxyEntity, secondaryProxyEntity);
      }
    }

    performConsolidation(entityRecord, externalProxies, externalProxyEntity, isStaticDataSource);
       
    return batchEntityRecord;
  }

  /**
   * Method to perform consolidation and validation of the consolidated entity
   * @param entityRecord the original record
   * @param externalProxies the list of external proxies
   * @param externalProxyEntity primary external proxy
   * @param isStaticDataSource indicates if the primary external proxy is static data source
   * @throws EntityModelCreationException if the consolidated version cannot be instantiated 
   * @throws EuropeanaApiException in case of EntityUpdateException or EntityValidationException
   */
  void performConsolidation(EntityRecord entityRecord, List<EntityProxy> externalProxies,
      Entity externalProxyEntity, boolean isStaticDataSource) throws EntityModelCreationException,
      EuropeanaApiException {
    Entity europeanaProxyEntity = entityRecord.getEuropeanaProxy().getEntity();

    Entity consolidatedEntity = null;
    if (isStaticDataSource) {
      consolidatedEntity = EntityObjectFactory.createConsolidatedEntityObject(europeanaProxyEntity);
    } else {
      consolidatedEntity =
          entityRecordService.mergeEntities(europeanaProxyEntity, externalProxyEntity);
    }

    //for the organizations update reference fields
    entityRecordService.processReferenceFields(consolidatedEntity);

    // add external proxyIds to sameAs / exactMatch
    entityRecordService.addSameReferenceLinks(
        consolidatedEntity,
        externalProxies.stream().map(EntityProxy::getProxyId).collect(Collectors.toList()));
    
    emEntityFieldCleaner.cleanAndNormalize(consolidatedEntity);
    entityRecordService.performReferentialIntegrity(consolidatedEntity);

    if (hasToGenerateDepiction(consolidatedEntity)) {
      // if isShownBy or depiction are null, create the isShownBy using the Search and Record api
      WebResource isShownBy =
          depictionGeneratorService.generateIsShownBy(consolidatedEntity.getEntityId());
      if (isShownBy != null) {
        consolidatedEntity.setIsShownBy(isShownBy);
      }
    }
    
    //SG: #EA-4376 do not perform validation if the entity is disabled (allow consolidation as entity is not used anymore)
    if(!entityRecord.isDisabled()) {
         validateCompleteValidationConstraints(consolidatedEntity);
    }
    
    //Aggregation is not a merged field, need to copy it from the old consolidated entity 
    copyIsAggregatedBy(entityRecord, consolidatedEntity);
    
    
    entityRecordService.updateConsolidatedVersion(
        entityRecord, consolidatedEntity);
  }



  void copyIsAggregatedBy(EntityRecord entityRecord, Entity consolidatedEntity) {
    consolidatedEntity.setIsAggregatedBy(new Aggregation(entityRecord.getEntity().getIsAggregatedBy()));
  }

  /**
   * Indicates if a depiction (isShownBy) needs to be generated for the given entity
   *
   * @param consolidatedEntity
   * @return
   */
  boolean hasToGenerateDepiction(Entity consolidatedEntity) {
    boolean isOrganization = EntityTypes.isOrganizationType(consolidatedEntity.getType());
    return !isOrganization
        && consolidatedEntity.getIsShownBy() == null
        && consolidatedEntity.getDepiction() == null;
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
