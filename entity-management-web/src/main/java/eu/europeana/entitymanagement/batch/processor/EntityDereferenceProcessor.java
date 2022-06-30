package eu.europeana.entitymanagement.batch.processor;

import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.config.DataSources;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.dereference.Dereferencer;
import eu.europeana.entitymanagement.exception.DatasourceDereferenceException;
import eu.europeana.entitymanagement.exception.EntityMismatchException;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.web.service.DereferenceServiceLocator;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import eu.europeana.entitymanagement.zoho.utils.WikidataUtils;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
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
  private final EntityRecordService entityRecordService;
  private final EntityManagementConfiguration emConfiguration;

  @Autowired
  public EntityDereferenceProcessor(
      DereferenceServiceLocator dereferenceServiceLocator,
      DataSources datasources,
      EntityRecordService entityRecordService,
      EntityManagementConfiguration emConfiguration) {
    this.dereferenceServiceLocator = dereferenceServiceLocator;
    this.datasources = datasources;
    this.entityRecordService = entityRecordService;
    this.emConfiguration = emConfiguration;
  }

  @Override
  public EntityRecord process(@NonNull EntityRecord entityRecord) throws Exception {

    // might be multiple wikidata IDs in case of redirections
    TreeSet<String> wikidataEntityIds = new TreeSet<>();
    collectWikidataEntityIds(
        entityRecord.getEuropeanaProxy(emConfiguration.getBaseDataEuropeanaUri()).getEntity(),
        wikidataEntityIds);

    for (EntityProxy externalProxy :
        entityRecord.getExternalProxies(emConfiguration.getBaseDataEuropeanaUri())) {
      Optional<DataSource> dataSource = datasources.getDatasource(externalProxy.getProxyId());
      if (dataSource.isPresent() && dataSource.get().isStatic()) {
        // do not update external proxy for static data sources
        continue;
      }

      Entity externalEntity = dereferenceAndUpdateProxy(externalProxy, entityRecord);
      // also for wikidata proxy we can collect redirection links
      collectWikidataEntityIds(externalEntity, wikidataEntityIds);
    }

    if (EntityTypes.Organization.getEntityType().equals(entityRecord.getEntity().getType())) {
      // cross-check wikidata proxy, if reference is lost of changed update the proxy list
      // accordingly
      handleWikidataReferenceChange(wikidataEntityIds, entityRecord);
    }

    return entityRecord;
  }

  void handleWikidataReferenceChange(TreeSet<String> wikidataEntityIds, EntityRecord entityRecord)
      throws Exception {
    EntityProxy wikidataProxy = entityRecord.getWikidataProxy();
    if (wikidataProxy == null && wikidataEntityIds.isEmpty()) {
      // nothing to do, no wikidata reference
    } else if (wikidataProxy != null && wikidataEntityIds.contains(wikidataProxy.getProxyId())) {
      // nothing to do, the wikidata proxy is correct
    } else if (!wikidataEntityIds.isEmpty()) {
      String wikidataId = wikidataEntityIds.first();

      if (wikidataProxy == null) {
        // create the wikidata proxy and dereference, only if a wikidata reference is found
        EntityProxy newProxy = addWikidataProxyAndDeref(wikidataId, entityRecord);
        if (logger.isInfoEnabled()) {
          logger.info(
              "For Entity Record with id:{}, wikidata proxy with id: {}  was created",
              entityRecord.getEntityId(),
              newProxy.getProxyId());
        }

      } else if (!wikidataEntityIds.contains(wikidataProxy.getProxyId())) {
        // wikidataProxy != null
        Optional<String> redirectedWikidataId =
            WikidataUtils.getWikidataId(wikidataProxy.getEntity().getSameReferenceLinks());
        boolean hasRedirectionCoref =
            redirectedWikidataId.isPresent()
                && wikidataEntityIds.contains(redirectedWikidataId.get());
        if (hasRedirectionCoref) {
          // possible wikidata redirections, the proxy is found in coreferences, only show debug
          // message
          String message =
              "For Entity Record with id:{}, wikidata proxy was not replaced as the proxy id: {} was found in coreferences.";
          logger.debug(message, entityRecord.getEntityId(), wikidataId);
        } else if (logger.isDebugEnabled()) {
          updateWikidataProxies(entityRecord, wikidataId, wikidataProxy, wikidataEntityIds);
        }
      }
    }
  }

  void updateWikidataProxies(
      EntityRecord entityRecord,
      String wikidataId,
      EntityProxy wikidataProxy,
      TreeSet<String> wikidataEntityIds)
      throws Exception {
    // remove wikidata proxy, if nor the proxy id or redirection id is found in coreferences
    entityRecord.getProxies().remove(wikidataProxy);
    if (logger.isInfoEnabled()) {
      logger.info(
          "For Entity Record with id:{}, wikidata proxy with id: {}  was removed",
          entityRecord.getEntityId(),
          wikidataId);
    }

    // create new proxy if wikidata id is available
    if (!wikidataEntityIds.isEmpty()) {
      // create the wikidata proxy and dereference, only if a wikidata reference is found
      EntityProxy newProxy = addWikidataProxyAndDeref(wikidataId, entityRecord);
      if (logger.isInfoEnabled()) {
        logger.info(
            "For Entity Record with id:{}, wikidata proxy was replaced, new proxy id: {}",
            entityRecord.getEntityId(),
            newProxy.getProxyId());
      }
    }
  }

  private EntityProxy addWikidataProxyAndDeref(String wikidataId, EntityRecord entityRecord)
      throws Exception {
    EntityProxy wikidataProxy =
        entityRecordService.appendWikidataProxy(
            entityRecord, wikidataId, entityRecord.getEntity().getType(), new Date());
    dereferenceAndUpdateProxy(wikidataProxy, entityRecord);
    return wikidataProxy;
  }

  private void collectWikidataEntityIds(Entity entity, @NonNull TreeSet<String> wikidataEntityIds) {
    List<String> wikidataIds = WikidataUtils.getAllWikidataIds(entity.getSameReferenceLinks());
    if (!wikidataIds.isEmpty()) {
      wikidataEntityIds.addAll(wikidataIds);
    }
  }

  private Entity dereferenceAndUpdateProxy(
      @NonNull EntityProxy externalProxy, @NonNull EntityRecord entityRecord) throws Exception {
    String entityId = entityRecord.getEntityId();
    String proxyId = externalProxy.getProxyId();
    String entityType = entityRecord.getEntity().getType();

    Dereferencer dereferencer = dereferenceServiceLocator.getDereferencer(proxyId, entityType);
    Optional<Entity> proxyResponseOptional = dereferencer.dereferenceEntityById(proxyId);
    if (proxyResponseOptional.isEmpty()) {
      throw new DatasourceDereferenceException(
          "Unsuccessful dereferenciation (empty response) for externalId="
              + proxyId
              + "; entityId="
              + entityId);
    }

    Entity proxyResponse = proxyResponseOptional.get();
    String proxyResponseType = proxyResponse.getType();
    if (!proxyResponseType.equals(entityType)) {
      throw new EntityMismatchException(
          String.format(
              MISMATCH_EXCEPTION_STRING, proxyResponseType, entityType, entityId, proxyId));
    }

    // always replace external proxy with proxy response
    externalProxy.setEntity(proxyResponse);
    handleDatasourceRedirections(externalProxy, proxyResponse);
    externalProxy.getProxyIn().setModified(new Date());
    return proxyResponse;
  }

  void handleDatasourceRedirections(EntityProxy externalProxy, Entity proxyResponse) {
    // in case of redirections also update proxy id
    if (!externalProxy.getProxyId().equals(proxyResponse.getEntityId())) {
      // add old id to sameAs references
      proxyResponse.addSameReferenceLink(externalProxy.getProxyId());

      // update proxy ID with the id of the main entity
      if (logger.isInfoEnabled()) {
        logger.info(
            "Updating proxy id with the actual value from the external entity {} -> {}",
            externalProxy.getProxyId(),
            proxyResponse.getEntityId());
      }
      externalProxy.setProxyId(proxyResponse.getEntityId());
    }
  }
}
