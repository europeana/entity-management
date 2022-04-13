package eu.europeana.entitymanagement.batch.processor;

import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.config.DataSources;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.dereference.Dereferencer;
import eu.europeana.entitymanagement.exception.DatasourceNotKnownException;
import eu.europeana.entitymanagement.exception.EntityMismatchException;
import eu.europeana.entitymanagement.web.service.DereferenceServiceLocator;
import eu.europeana.entitymanagement.zoho.utils.WikidataUtils;
import eu.europeana.entitymanagement.zoho.utils.ZohoUtils;
import java.util.Date;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * This {@link ItemProcessor} retrieves Entity metadata from all proxy datasources, and then
 * overwrites the local metadata if datasource response is different.
 */
@Component
public class EntityDereferenceProcessor implements ItemProcessor<EntityRecord, EntityRecord> {

  private static final String MISMATCH_EXCEPTION_STRING =
      "DataSource type %s does not match entity type %s for entityId=%s, proxyId=%s";
  private static final Logger logger = LogManager.getLogger(EntityDereferenceProcessor.class);
  private final DereferenceServiceLocator dereferenceServiceLocator;
  private final DataSources datasources;

  @Autowired
  public EntityDereferenceProcessor(
      DereferenceServiceLocator dereferenceServiceLocator, DataSources datasources) {
    this.dereferenceServiceLocator = dereferenceServiceLocator;
    this.datasources = datasources;
  }

  @Override
  public EntityRecord process(@NonNull EntityRecord entityRecord) throws Exception {
    String entityId = entityRecord.getEntityId();
    // used to update the wikidata proxyId in case the zoho sameAs values have been changed
    String newWikidataProxyId = null;
    EntityProxy wikidataProxyToBeRemoved = null;
    for (EntityProxy externalProxy : entityRecord.getExternalProxies()) {
      // in case of wikidata proxy, update the proxyId with the new data from the zoho sameAs values
      if (WikidataUtils.isWikidataOrganization(
              externalProxy.getProxyId(), entityRecord.getEntity().getType())
          && !externalProxy.getProxyId().equals(newWikidataProxyId)) {
        if (newWikidataProxyId == null) {
          wikidataProxyToBeRemoved = externalProxy;
          continue;
        }
        externalProxy.setProxyId(newWikidataProxyId);
      }

      String proxyId = externalProxy.getProxyId();
      // do not update external proxy for static data sources
      Optional<DataSource> dataSource = datasources.getDatasource(proxyId);
      if (dataSource.isPresent() && dataSource.get().isStatic()) {
        continue;
      }

      Dereferencer dereferencer =
          dereferenceServiceLocator.getDereferencer(proxyId, entityRecord.getEntity().getType());
      Optional<Entity> proxyResponseOptional = dereferencer.dereferenceEntityById(proxyId);
      if (proxyResponseOptional.isEmpty()) {
        throw new DatasourceNotKnownException(
            "Unsuccessful dereferenciation for externalId=" + proxyId + "; entityId=" + entityId);
      }

      Entity proxyResponse = proxyResponseOptional.get();

      Entity entity = entityRecord.getEntity();

      /*
       * in case of the zoho proxy, save the new wikidata proxy id from the zoho sameAs values
       * (this is used to update the wikidata proxy)
       */
      Optional<String> newWikidataIdFromZoho;
      if (ZohoUtils.isZohoOrganization(proxyId, entity.getType())
          && (newWikidataIdFromZoho =
                  WikidataUtils.getWikidataId(proxyResponse.getSameReferenceLinks()))
              .isPresent()) {
        newWikidataProxyId = newWikidataIdFromZoho.get();
      }

      String proxyResponseType = proxyResponse.getType();
      String entityType = entity.getType();
      if (!proxyResponseType.equals(entityType)) {
        throw new EntityMismatchException(
            String.format(
                MISMATCH_EXCEPTION_STRING, proxyResponseType, entityType, entityId, proxyId));
      }

      // always replace external proxy with proxy response
      externalProxy.setEntity(proxyResponse);
      handleDatasourceRedirections(externalProxy, proxyResponse);
      externalProxy.getProxyIn().setModified(new Date());
    }

    if(wikidataProxyToBeRemoved != null) {
      entityRecord.removeProxy(wikidataProxyToBeRemoved);
    }
    
    return entityRecord;
  }

  void handleDatasourceRedirections(EntityProxy externalProxy, Entity proxyResponse) {
    // in case of redirections also update proxy id
    if (!externalProxy.getProxyId().equals(proxyResponse.getEntityId())) {
      if (datasources.hasDataSource(proxyResponse.getEntityId())) {
        // do not allow redirection to unknow data sources
      }
      proxyResponse.addSameReferenceLink(externalProxy.getProxyId());
      externalProxy.setProxyId(proxyResponse.getEntityId());
      logger.info(
          "Updated proxy id with the actual value from the external entity {} -> {}",
          externalProxy.getProxyId(),
          proxyResponse.getEntityId());
    }
  }
}
