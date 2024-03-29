package eu.europeana.entitymanagement.batch.processor;

import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.config.DataSources;
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledUpdateType;
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
public class EntityDereferenceProcessor extends BaseEntityProcessor {

  private static final String MISMATCH_EXCEPTION_STRING =
      "DataSource type %s does not match entity type %s for entityId=%s, proxyId=%s";
  private static final Logger logger = LogManager.getLogger(EntityDereferenceProcessor.class);
  private final DereferenceServiceLocator dereferenceServiceLocator;
  private final DataSources datasources;
  private final EntityRecordService entityRecordService;

  @Autowired
  public EntityDereferenceProcessor(
      DereferenceServiceLocator dereferenceServiceLocator,
      DataSources datasources,
      EntityRecordService entityRecordService) {
    super(ScheduledUpdateType.FULL_UPDATE);
    this.dereferenceServiceLocator = dereferenceServiceLocator;
    this.datasources = datasources;
    this.entityRecordService = entityRecordService;
  }

  @Override
  public BatchEntityRecord doProcessing(@NonNull BatchEntityRecord entityRecord) throws Exception {

    Date timestamp = new Date();
    // might be multiple wikidata IDs in case of redirections
    TreeSet<String> wikidataEntityIds = new TreeSet<>();
    collectWikidataEntityIds(
        entityRecord.getEntityRecord().getEuropeanaProxy().getEntity(), wikidataEntityIds);

    for (EntityProxy externalProxy : entityRecord.getEntityRecord().getExternalProxies()) {
      Optional<DataSource> dataSource = datasources.getDatasource(externalProxy.getProxyId());
      if (dataSource.isPresent() && dataSource.get().isStatic()) {
        // do not update external proxy for static data sources
        continue;
      }

      Entity externalEntity =
          dereferenceAndUpdateProxy(externalProxy, entityRecord.getEntityRecord(), timestamp);
      // also for wikidata proxy we can collect redirection links
      collectWikidataEntityIds(externalEntity, wikidataEntityIds);
    }

    if (EntityTypes.Organization.getEntityType()
        .equals(entityRecord.getEntityRecord().getEntity().getType())) {
      // cross-check wikidata proxy, if reference is lost of changed update the proxy list
      // accordingly
      handleWikidataReferenceChange(wikidataEntityIds, entityRecord.getEntityRecord(), timestamp);
    }

    return entityRecord;
  }

  /**
   * The wikidata ids may change in Zoho. Therefore the changes needs to be identified and the
   * proxies need to be updated accordingly
   *
   * @param wikidataEntityIds
   * @param entityRecord
   * @param timestamp
   * @throws Exception
   */
  private void handleWikidataReferenceChange(
      @NonNull TreeSet<String> wikidataEntityIds,
      EntityRecord entityRecord,
      @NonNull Date timestamp)
      throws Exception {
    EntityProxy wikidataProxy = entityRecord.getWikidataProxy();
    boolean noWikidataProxy = (wikidataProxy == null && wikidataEntityIds.isEmpty());
    boolean noChangeForProxy =
        wikidataProxy != null && wikidataEntityIds.contains(wikidataProxy.getProxyId());
    boolean noWikidataId = wikidataEntityIds.isEmpty();
    if (noWikidataProxy || noChangeForProxy) {
      // nothing to do, no wikidata reference or the wikidata proxy is correct
      return;
    }

    if (noWikidataId && wikidataProxy != null) {
      // remove wikidata proxy
      entityRecord.getProxies().remove(wikidataProxy);
      return;
    }

    // wikidataEntityIdswikidataEntityIds cannot be empty at this stage
    String wikidataId = wikidataEntityIds.first();
    if (wikidataProxy == null) {
      // create the wikidata proxy and dereference, only if a wikidata reference is found
      EntityProxy newProxy = addWikidataProxyAndDeref(wikidataId, entityRecord, timestamp);
      logNewProxyCreated(entityRecord, newProxy);

    } else if (!wikidataEntityIds.contains(wikidataProxy.getProxyId())) {
      handleNewWikidataId(entityRecord, wikidataEntityIds, wikidataId, wikidataProxy, timestamp);
    }
  }

  private void handleNewWikidataId(
      EntityRecord entityRecord,
      TreeSet<String> wikidataEntityIds,
      String wikidataId,
      EntityProxy wikidataProxy,
      Date timestamp)
      throws Exception {
    // wikidataProxy != null
    Optional<String> redirectedWikidataId =
        WikidataUtils.getWikidataId(wikidataProxy.getEntity().getSameReferenceLinks());
    boolean hasRedirectionCoref =
        redirectedWikidataId.isPresent() && wikidataEntityIds.contains(redirectedWikidataId.get());
    if (hasRedirectionCoref) {
      // possible wikidata redirections, the proxy is found in coreferences, only show debug
      // message
      logProxyWithRedirectionExists(entityRecord, wikidataId);
    } else {
      // proxy not found in coreferences, update the proxy with the new wikidata ID
      updateWikidataProxies(entityRecord, wikidataId, wikidataProxy, wikidataEntityIds, timestamp);
    }
  }

  void logNewProxyCreated(EntityRecord entityRecord, EntityProxy newProxy) {
    if (logger.isDebugEnabled()) {
      logger.debug(
          "For Entity Record with id:{}, wikidata proxy with id: {}  was created",
          entityRecord.getEntityId(),
          newProxy.getProxyId());
    }
  }

  void logProxyWithRedirectionExists(EntityRecord entityRecord, String wikidataId) {
    if (logger.isDebugEnabled()) {
      String message =
          "For Entity Record with id:{}, wikidata proxy was not replaced as the proxy id: {} was found in coreferences.";
      logger.debug(message, entityRecord.getEntityId(), wikidataId);
    }
  }

  private void updateWikidataProxies(
      EntityRecord entityRecord,
      String wikidataId,
      EntityProxy wikidataProxy,
      TreeSet<String> wikidataEntityIds,
      @NonNull Date timestamp)
      throws Exception {
    // remove wikidata proxy, if nor the proxy id or redirection id is found in coreferences
    entityRecord.getProxies().remove(wikidataProxy);
    if (logger.isDebugEnabled()) {
      logger.debug(
          "For Entity Record with id:{}, wikidata proxy with id: {}  was removed",
          entityRecord.getEntityId(),
          wikidataId);
    }

    // create new proxy if wikidata id is available
    if (!wikidataEntityIds.isEmpty()) {
      // create the wikidata proxy and dereference, only if a wikidata reference is found
      EntityProxy newProxy = addWikidataProxyAndDeref(wikidataId, entityRecord, timestamp);
      if (logger.isDebugEnabled()) {
        logger.debug(
            "For Entity Record with id:{}, wikidata proxy was replaced, new proxy id: {}",
            entityRecord.getEntityId(),
            newProxy.getProxyId());
      }
    }
  }

  private EntityProxy addWikidataProxyAndDeref(
      String wikidataId, EntityRecord entityRecord, @NonNull Date timestamp) throws Exception {
    EntityProxy wikidataProxy =
        entityRecordService.appendWikidataProxy(
            entityRecord, wikidataId, entityRecord.getEntity().getType(), timestamp);
    dereferenceAndUpdateProxy(wikidataProxy, entityRecord, timestamp);
    return wikidataProxy;
  }

  private void collectWikidataEntityIds(Entity entity, @NonNull TreeSet<String> wikidataEntityIds) {
    List<String> wikidataIds = WikidataUtils.getAllWikidataIds(entity.getSameReferenceLinks());
    if (!wikidataIds.isEmpty()) {
      wikidataEntityIds.addAll(wikidataIds);
    }
  }

  private Entity dereferenceAndUpdateProxy(
      @NonNull EntityProxy externalProxy,
      @NonNull EntityRecord entityRecord,
      @NonNull Date timestamp)
      throws Exception {
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
    // ensure rights are up to date for all external proxies
    updateRights(externalProxy);

    // reset modified field
    externalProxy.getProxyIn().setModified(timestamp);
    return proxyResponse;
  }

  private void handleDatasourceRedirections(EntityProxy externalProxy, Entity proxyResponse) {
    // in case of redirections also update proxy id
    if (!externalProxy.getProxyId().equals(proxyResponse.getEntityId())) {
      // add old id to sameAs references
      proxyResponse.addSameReferenceLink(externalProxy.getProxyId());

      // update proxy ID with the id of the main entity
      if (logger.isDebugEnabled()) {
        logger.debug(
            "Updating proxy id with the actual value from the external entity {} -> {}",
            externalProxy.getProxyId(),
            proxyResponse.getEntityId());
      }
      externalProxy.setProxyId(proxyResponse.getEntityId());
    }
  }

  protected void updateRights(EntityProxy externalProxy) {
    // update rights
    Optional<DataSource> dataSource = datasources.getDatasource(externalProxy.getProxyId());
    if (dataSource.isPresent() && hasChangedRights(externalProxy, dataSource.get())) {
      externalProxy.getProxyIn().setRights(dataSource.get().getRights());
    }
  }

  boolean hasChangedRights(EntityProxy externalProxy, DataSource dataSource) {
    return !dataSource.getRights().equals(externalProxy.getProxyIn().getRights());
  }
}
