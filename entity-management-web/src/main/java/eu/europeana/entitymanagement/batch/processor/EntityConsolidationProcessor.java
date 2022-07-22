package eu.europeana.entitymanagement.batch.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.common.config.HttpClient;
import eu.europeana.entitymanagement.config.DataSources;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.WebResource;
import eu.europeana.entitymanagement.exception.ingestion.EntityValidationException;
import eu.europeana.entitymanagement.normalization.EntityFieldsCleaner;
import eu.europeana.entitymanagement.normalization.EntityFieldsCompleteValidationGroup;
import eu.europeana.entitymanagement.normalization.EntityFieldsDataSourceProxyValidationGroup;
import eu.europeana.entitymanagement.utils.EntityObjectFactory;
import eu.europeana.entitymanagement.web.service.EntityRecordService;

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
  final EntityManagementConfiguration emConfiguration;

  public EntityConsolidationProcessor(
      EntityRecordService entityRecordService,
      ValidatorFactory emValidatorFactory,
      EntityFieldsCleaner emEntityFieldCleaner,
      DataSources datasources,
      EntityManagementConfiguration emConfiguration) {
    this.entityRecordService = entityRecordService;
    this.emValidatorFactory = emValidatorFactory;
    this.emEntityFieldCleaner = emEntityFieldCleaner;
    this.datasources = datasources;
    this.emConfiguration = emConfiguration;
  }

  @Override
  public EntityRecord process(@NonNull EntityRecord entityRecord) throws EuropeanaApiException {

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
    //if isShownBy or depiction are null, create the isShownBy using the Search and Record api 
    if(consolidatedEntity.getIsShownBy()==null && consolidatedEntity.getDepiction()==null) {
      WebResource isShownBy = generateIsShownBy(consolidatedEntity.getEntityId());
      if(isShownBy!=null) {
        consolidatedEntity.setIsShownBy(isShownBy);
      }
    }
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
  
  private WebResource generateIsShownBy (String entityUri) throws EuropeanaApiException {
    Map<String, String> params = new HashMap<String, String>();
    params.put("rows", "1");
    params.put("wskey", "apidemo");
    params.put("query", "\"" + entityUri + "\"");
    params.put("sort", "contentTier+desc,metadataTier+desc");
    params.put("profile", "minimal");

    String response = null;
    try {
      response = HttpClient.httpGetClient(emConfiguration.getSearchAndRecordUrl(), null, params);
    } catch (Exception e) {
      throw new EuropeanaApiException("Unable to get the valid response from the Search and Record API.", e);
    }
    if(response==null) return null;
    
    JSONObject responseJson = new JSONObject(response);
    String edmIsShownBy = null;
    String itemId = null;
    String edmPreview = null;
    if(responseJson.has("items")) {
      JSONArray itemsList = responseJson.getJSONArray("items");
      if(itemsList.length()>0) {
        JSONObject item = (JSONObject) itemsList.get(0);
        if(item.has("edmIsShownBy")) {
          JSONArray edmIsShownByList = item.getJSONArray("edmIsShownBy");
          if(edmIsShownByList.length()>0) {
            edmIsShownBy = edmIsShownByList.getString(0);
          }
        }
        if(item.has("id")) {
          itemId = item.getString("id");
        }
        if(item.has("edmPreview")) {
          JSONArray edmPreviewList = item.getJSONArray("edmPreview");
          if(edmPreviewList.length()>0) {
            edmPreview = edmPreviewList.getString(0);
          }
        }
      }
    }
    
    if(edmIsShownBy!=null && itemId!=null) {
      return new WebResource(edmIsShownBy, emConfiguration.getItemDataEndpoint() + itemId, edmPreview);
    }
    return null;
  }
}
