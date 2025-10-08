package eu.europeana.entitymanagement.web.service;

import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.JOB_DESCRIPTION_FACTORY;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import com.zoho.crm.api.record.DeletedRecord;
import com.zoho.crm.api.record.Record;
import dev.morphia.query.filters.Filter;
import dev.morphia.query.filters.Filters;
import eu.europeana.api.commons.definitions.utils.DateUtils;
import eu.europeana.api.commons.web.model.vocabulary.Operations;
import eu.europeana.entitymanagement.batch.config.JobDescriptionFactory;
import eu.europeana.entitymanagement.batch.service.EntityUpdateService;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.config.AppAutoconfig;
import eu.europeana.entitymanagement.config.DataSources;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.exception.ingestion.EntityUpdateException;
import eu.europeana.entitymanagement.mongo.repository.ZohoSyncRepository;
import eu.europeana.entitymanagement.serialization.JsonLdSerializer;
import eu.europeana.entitymanagement.solr.exception.SolrServiceException;
import eu.europeana.entitymanagement.web.model.BatchOperations;
import eu.europeana.entitymanagement.web.model.FailedOperation;
import eu.europeana.entitymanagement.web.model.Operation;
import eu.europeana.entitymanagement.web.model.ZohoSyncReport;
import eu.europeana.entitymanagement.web.model.ZohoSyncReportFields;
import eu.europeana.entitymanagement.zoho.organization.ZohoConfiguration;
import eu.europeana.entitymanagement.zoho.organization.ZohoDereferenceService;
import eu.europeana.entitymanagement.zoho.organization.ZohoOrganizationConverter;
import eu.europeana.entitymanagement.zoho.utils.ZohoConstants;
import eu.europeana.entitymanagement.zoho.utils.ZohoException;

/**
 * Service class for scheduling taks for entity synchronization with external data sources
 */
@Service(AppAutoconfig.BEAN_ZOHO_SYNC_SERVICE)
public class EntitySynchronizationService extends BaseZohoAccess {

  public static final String ZOHO_SYNC_SLACK_TEMPLATE =
      "%d organisations in Zoho were synchronised with the following actions:%n"
          + "created: %d, updated: %d, deprecated: %d, undeprecated: %d, permanently deleted: %d, failed: %d%n"
          + "%s";


  private final JsonLdSerializer emJsonldSerializer;

  /**
   * Constructor using autowired beans
   * 
   * @param entityRecordService the entity record service
   * @param entityUpdateService the entity update service
   * @param emConfiguration the app configuration
   * @param datasources the datasources
   * @param zohoConfiguration the zoho access configuration
   * @param zohoSyncRepo the zoho sync repository
   * @param zohoDereferenceService the zoho dereference service
   * @param emJsonldSerializer the json serializer
   * @param jobDescriptionFactory the factory for instantiating job descriptions
   */
  @Autowired
  public EntitySynchronizationService(EntityRecordService entityRecordService,
      EntityUpdateService entityUpdateService, EntityManagementConfiguration emConfiguration,
      DataSources datasources, ZohoConfiguration zohoConfiguration, ZohoSyncRepository zohoSyncRepo,
      ZohoDereferenceService zohoDereferenceService, JsonLdSerializer emJsonldSerializer,
      @Qualifier(JOB_DESCRIPTION_FACTORY) JobDescriptionFactory jobDescriptionFactory) {

    super(entityRecordService, entityUpdateService, emConfiguration, datasources, zohoConfiguration,
        zohoSyncRepo, zohoDereferenceService, jobDescriptionFactory);
    this.emJsonldSerializer = emJsonldSerializer;
  }

  /**
   * method to run the zoho synchronization for organizations updated since last successfully
   * scheduled synchronization (updates are run asynchronously)
   *
   * @return the report on performed operations
   * @throws EntityUpdateException
   */
  public ZohoSyncReport synchronizeModifiedZohoOrganizations() {

    ZohoSyncReport previousSync = zohoSyncRepo.findLastZohoSyncReport();
    OffsetDateTime modifiedSince;
    if (previousSync == null || previousSync.getStartDate() == null) {
      // first sync, schedule all
      modifiedSince = OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
    } else {
      // schedule modified since last update
      modifiedSince = DateUtils.toOffsetDateTime(previousSync.getStartDate());
    }
    // for development debugging purposes use
    boolean locallDebugging = false;
    if (locallDebugging) {
      String since = "29-Sep-2025 10:30:00";
      modifiedSince = generateFixDate(since);
      if (modifiedSince == null) {
        // date parsing error
        return null;
      }
    }

    return synchronizeZohoOrganizations(modifiedSince);
  }

  /**
   * main method to run the zoho synchronization
   *
   * @param modifiedSince the start date from which the Zoho modifications must be synchronized
   * @return the report on performed operations
   * @throws EntityUpdateException
   */
  public ZohoSyncReport synchronizeZohoOrganizations(@NonNull OffsetDateTime modifiedSince) {

    OffsetDateTime deletedSince =
        modifiedSince.minusDays(emConfiguration.getZohoSyncDeleteOffsetDays());
    if (logger.isInfoEnabled()) {
      logger.info("Synchronizing organizations updated after date: {}, and delete after date :{}",
          modifiedSince, deletedSince);
    }

    ZohoSyncReport zohoSyncReport = new ZohoSyncReport(new Date());
    // synchronize updated in Zoho
    synchronizeZohoOrganizations(modifiedSince, zohoSyncReport);
    // synchronize deleted in zoho
    synchronizeDeletedZohoOrganizations(deletedSince, zohoSyncReport);

    logger.info("Zoho update operations completed successfully:\n {}", zohoSyncReport);
    
    return zohoSyncRepo.save(zohoSyncReport);
  }


  public void publishReport(ZohoSyncReport zohoSyncReport) {
    
    try {
      SlackConnection slackConnection = new SlackConnection(emConfiguration.getZohoSlackWebHook());
      String jsonMessage = buildReportMessage(zohoSyncReport);
      slackConnection.publishStatusReport(jsonMessage);
    } catch (IOException e) {
      logger.warn("Exception occurred while buiding the slack message for ZohoReport: {}", zohoSyncReport, e);
    }
  }

  String buildReportMessage(ZohoSyncReport zohoSyncReport) throws IOException {
    long synced = zohoSyncReport.getCreatedItems() + zohoSyncReport.getUpdatedItems()
        + zohoSyncReport.getDeprecatedItems();

    long failed = zohoSyncReport.getFailed() == null ? 0 : zohoSyncReport.getFailed().size();

    String slackMessage = String.format(ZOHO_SYNC_SLACK_TEMPLATE, synced,
        zohoSyncReport.getCreatedItems(), zohoSyncReport.getUpdatedItems(),
        zohoSyncReport.getDeprecatedItems(), zohoSyncReport.getEnabledItems(),
        zohoSyncReport.getDeletedItems(), failed, generateFailedMessage(zohoSyncReport));

    // could use a proper object and json serializer later
    Map<String, String> body = new ConcurrentHashMap<>();
    body.put("text", slackMessage);
    return emJsonldSerializer.serializeObject(body);
  }



  private String generateFailedMessage(ZohoSyncReport zohoSyncReport) {
    if (zohoSyncReport.getFailed() == null || zohoSyncReport.getFailed().isEmpty()) {
      return "";
    }

    String headLine = "\\n\\nThe following organisations failed synchronisation:\\n";
    final int avgUrlSize = 100;
    int estimatedSize = headLine.length() + (zohoSyncReport.getFailed().size() * avgUrlSize);
    StringBuilder builder = new StringBuilder(estimatedSize);
    builder.append(headLine);
    for (FailedOperation failed : zohoSyncReport.getFailed()) {
      builder.append(failed.getZohoId()).append(" because of error: ").append(failed.getMessage())
          .append("\\n");
    }

    return builder.toString();
  }

  void synchronizeZohoOrganizations(@NonNull OffsetDateTime modifiedSince,
      ZohoSyncReport zohoSyncReport) {
    List<Record> orgList;
    BatchOperations operations;

    int page = 1;
    int pageSize = emConfiguration.getZohoSyncBatchSize();
    boolean hasNext = true;
    // Zoho doesn't return the number of organizations in get response.

    while (hasNext) {
      // retrieve modified organizations
      try {
        orgList = zohoConfiguration.getZohoAccessClient().getZcrmRecordOrganizations(page, pageSize,
            modifiedSince);

        logExecutionProgress(orgList, page, pageSize);
      } catch (ZohoException e) {
        // break if Zoho access failures occurs
        // sets also execution status
        zohoSyncReport.addFailedOperation(null, ZohoSyncReportFields.ZOHO_ACCESS_ERROR,
            "Zoho synchronization exception occured when handling organizations modified in Zoho, "
            + "the execution was interupted without updating all organizations",
            e);
        // stop execution if the organizations cannot be read from zoho
        return;
      }

      // collect operations to be run on Metis and Entity API
      operations = fillOperations(orgList);
      // perform operations on all systems
      performOperations(operations, zohoSyncReport);

      if (isLastPage(orgList.size(), pageSize)) {
        if (logger.isDebugEnabled()) {
          logger.debug(
              "Processing updated zoho records finished at page {}, pageSize {}, items on last page {}",
              page, pageSize, orgList.size());
        }
        hasNext = false;
      } else {
        // go to next page
        page++;
      }
    }
  }

  /**
   * Retrieve deleted in Zoho organizations and remove them from the Enrichment database
   * 
   * @param modifiedSince optional, select only organizations updated after this date
   * @param zohoSyncReport report collectign the results of the execution
   */
  void synchronizeDeletedZohoOrganizations(OffsetDateTime modifiedSince,
      ZohoSyncReport zohoSyncReport) {

    // do not delete organizations for individual entity importer
    // in case of full import the database should be manually cleaned. No need to delete
    // organizations
    List<DeletedRecord> deletedRecordsInZoho;
    int startPage = 1;
    int pageSize = emConfiguration.getZohoSyncBatchSize();
    boolean hasNext = true;
    int currentPageSize = 0;
    List<String> entitiesZohoCoref = null;
    List<String> entityIdsToDelete;
    // Zoho doesn't return the total results
    while (hasNext) {
      try {
        // list of (europeana) organizations ids
        deletedRecordsInZoho = zohoConfiguration.getZohoAccessClient()
            .getZohoDeletedRecordOrganizations(modifiedSince, startPage, pageSize);

        currentPageSize = deletedRecordsInZoho.size();
        // check exists in EM (Note: zoho doesn't support filtering by lastModified for deleted
        // entities)
        // build the Zoho Coref URL
        entitiesZohoCoref = getDeletedEntitiesZohoCoref(deletedRecordsInZoho);
        entityIdsToDelete = getEntityIdsByZohoCorefs(entitiesZohoCoref);

        // perform permanent deletion
        runPermanentDelete(entityIdsToDelete, zohoSyncReport);
      } catch (ZohoException e) {
        logger.error(
            "Zoho synchronization exception occured when handling organizations deleted in Zoho",
            e);
        zohoSyncReport.addFailedOperation(null, ZohoSyncReportFields.ZOHO_ACCESS_ERROR, e);
      } catch (SolrServiceException | RuntimeException e) {
        logger.error(
            "Zoho synchronization exception occured when handling organizations deleted in Zoho",
            e);
        String message = buildErrorMessage(
            "Unexpected error occured when deleting organizations with ids: ", entitiesZohoCoref);
        zohoSyncReport.addFailedOperation(null, ZohoSyncReportFields.ENTITY_DELETION_ERROR, message,
            e);
      }

      if (isLastPage(currentPageSize, pageSize)) {
        // last page: if no more organizations exist in Zoho
        if (logger.isDebugEnabled()) {
          logger.debug(
              "Processing of deleted records is completes at page {}, pageSize {}, items on last page {}",
              startPage, pageSize, currentPageSize);
        }
        hasNext = false;
      } else {
        // go to next page
        startPage++;
      }
    }
  }

  List<String> getEntityIdsByZohoCorefs(List<String> entitiesZohoCoref) {
    List<EntityRecord> recordsToDelete;
    List<String> entityIdsToDelete = new ArrayList<>(entitiesZohoCoref.size());
    // retrieve records by coref
    recordsToDelete =
        entityRecordService.retrieveMultipleByEntityIdsOrCoreference(entitiesZohoCoref, null);
    String zohoProxyId;
    for (EntityRecord entityRecord : recordsToDelete) {
      zohoProxyId = getZohoProxyId(entityRecord);
      if (zohoProxyId == null && logger.isWarnEnabled()) {
        logger.warn(
            "Cannot get zohoProxyId! Organization does not have a zoho proxy, but a zoho coref: {} ",
            entityRecord);
      }
      // for deprecated organizations, make sure to not delete the valid organizations which may
      // contain the deprecated id in corefs
      // threrefore, delete only records which have the deleted zoho record id in zoho proxy id
      if (entitiesZohoCoref.contains(zohoProxyId)) {
        entityIdsToDelete.add(entityRecord.getEntityId());
      }
    }

    return entityIdsToDelete;
  }

  String getZohoProxyId(EntityRecord entityRecord) {
    for (String proxyId : entityRecord.getExternalProxyIds()) {
      if (proxyId.startsWith(zohoConfiguration.getZohoBaseUrlOrganizations())) {
        return proxyId;
      }
    }
    return null;
  }

  boolean isLastPage(int currentPageSize, int maxItemsPerPage) {
    // END LOOP: if no more organizations exist in Zoho
    return currentPageSize < maxItemsPerPage;
  }


  /**
   * This method performs synchronization of organizations between Zoho and Entity API database
   * addressing deleted and unwanted (defined by owner criteria) organizations. We separate to
   * update from to delete types
   *
   * <p>
   * If full import -> only update/create else Update operation -> 1) search criteria is empty,
   * check if the owner is present in the zoho record 2) search criteria present, then zoho record
   * owner should match with the search filter ZOHO_OWNER_CRITERIA Delete the record if none of the
   * above matches.
   *
   * @param orgList The list of retrieved Zoho objects
   */
  BatchOperations fillOperations(final List<Record> orgList) {
    BatchOperations operations = new BatchOperations();

    Set<String> modifiedZohoUrls = getZohoOrganizationUrls(orgList);
    List<EntityRecord> existingEntityRecords = findEntityRecordsByProxyId(modifiedZohoUrls);

    Long zohoId;
    for (Record zohoOrg : orgList) {
      // if full import then always update no deletion required
      zohoId = zohoOrg.getId();
      Optional<EntityRecord> entityRecordOptional = findRecordInList(zohoId, existingEntityRecords);
      EntityRecord entityRecord = null;
      if (entityRecordOptional.isPresent()) {
        entityRecord = entityRecordOptional.get();
      }

      addOperation(operations, zohoId, zohoOrg, entityRecord);
    }
    return operations;
  }

  /**
   * Find
   * 
   * @param modifiedInZoho
   * @return
   */
  List<EntityRecord> findEntityRecordsByProxyId(Set<String> modifiedInZoho) {
    Filter proxyIdsFilter = Filters.in("proxies.proxyId", modifiedInZoho);
    // save to index operations are run asynchronuously, no need to dereference organizations here
    return entityRecordService.findEntitiesWithFilter(0, modifiedInZoho.size(),
        new Filter[] {proxyIdsFilter}, null);

  }

  private void addOperation(BatchOperations operations, Long zohoId, Record zohoOrg,
      EntityRecord entityRecord) {

    String zohoRecordEuropeanaID = ZohoOrganizationConverter.getEuropeanaIdFieldValue(zohoOrg);

    boolean hasDpsOwner = hasRequiredOwnership(zohoOrg);
    boolean markedForDeletion = ZohoOrganizationConverter.isMarkedForDeletion(zohoOrg);

    String emOperation = identifyOperationType(zohoId, zohoRecordEuropeanaID, entityRecord,
        hasDpsOwner, markedForDeletion, zohoOrg);

    if (Objects.nonNull(emOperation)) {
      // only if there is an operation to perform in EM
      // zohoRecordEuropeanaID might be null at this stage
      Operation operation =
          new Operation(zohoRecordEuropeanaID, emOperation, zohoOrg, entityRecord);
      operations.addOperation(operation);
    } else {
      // skip
      if (logger.isDebugEnabled()) {
        Object entityId = (entityRecord == null) ? "null" : entityRecord.getEntityId();
        logger.debug(
            "Organization has changed in zoho, but there is no operation to perform on entity database, "
                + "probably becasue the zoho organization doesn't have the required role, it was marked for deletion, "
                + "or the generation of Organizations is not allowed for this job instance. Zoho id: {}, "
                + "hasDpsOwner: {}, markedForDeletion: {}, entityRecord: {}",
            zohoId, hasDpsOwner, markedForDeletion,
            entityId);
      }
    }
  }

  String identifyOperationType(Long zohoId, String zohoRecordEuropeanaID, EntityRecord entityRecord,
      boolean hasDpsOwner, boolean markedForDeletion, Record zohoOrg) {

    String operation = null;
    if (entityRecord == null) {
      operation = shouldCreate(zohoId, zohoRecordEuropeanaID, hasDpsOwner, markedForDeletion)
          ? Operations.CREATE
          : null;
    } else if (emConfiguration.isGenerateOrganizationEuropeanaId() && isModifiedByApi(zohoOrg)) {
      if (logger.isInfoEnabled()) {
        logger.info("Skip Organization organization, it was last modified by the API by the  {}",
            zohoId);
      }
      return null;
    } else if (shouldDisable(hasDpsOwner, markedForDeletion)) {
      // if needsToBeDisabled
      operation = Operations.DELETE;
      // NOTE: the perform deletion needs to be updated to schedule updates
    } else if (shouldEnable(entityRecord, hasDpsOwner, markedForDeletion)) {
      // enable and update
      // if needsToBeDidabled
      operation = Operations.ENABLE;
    } else {
      if (StringUtils.isBlank(zohoRecordEuropeanaID)) {
        logger.warn(
            "Zoho Organization doesn't have a Europeana ID, it should be set in Zoho before running the update workflow {}",
            zohoId);
      }
      operation = Operations.UPDATE;
    }
    return operation;
  }

  private boolean isModifiedByApi(Record zohoOrg) {
    String modifiedBy = ZohoOrganizationConverter.getOwnerName(zohoOrg);
    return ZohoConstants.ZOHO_USER_EUROPEANA_APIS.equals(modifiedBy);
  }

  boolean shouldEnable(EntityRecord entityRecord, boolean hasDpsOwner, boolean markedForDeletion) {
    return entityRecord.isDisabled() && !shouldDisable(hasDpsOwner, markedForDeletion);
  }

  boolean shouldCreate(Long zohoId, String zohoRecordEuropeanaID, boolean hasDpsOwner,
      boolean markedForDeletion) {

    if (skipNonExisting(hasDpsOwner, markedForDeletion)) {
      logger.debug(
          "Organization has changed in zoho, but it is marked for deletion or doesn't have DPS as Owner. "
          + "Skipped creation for Zoho id: {}, hasDpsOwner: {}, markedForDeletion: {}",
          zohoId, hasDpsOwner, markedForDeletion);
      // skipped
      return false;
    }

    if (skipNoZohoEuropeanaId(zohoRecordEuropeanaID,
        emConfiguration.isGenerateOrganizationEuropeanaId())) {
      logger.debug(
          "Organization has changed in zoho, but the job instance is not allowed to generate entity ids for Organizations. "
          + "Skipped entity registration for Zoho id: {}",
          zohoId);
      // skipped if Europeana ID Generation is not allowed
      return false;
    }

    // entity not in entity management database,
    // create operation if ownership is correct and (EuropeanaID is available or organization id
    // generation is enabled)
    logger.debug("New Organization to Create for zoho ID: {}", zohoId);

    return true;
  }

  boolean shouldDisable(boolean hasDpsOwner, boolean markedForDeletion) {
    // lost ownership, or marked for deletion - > disable
    return !hasDpsOwner || markedForDeletion;
  }

  private boolean skipNoZohoEuropeanaId(String zohoRecordEuropeanaID,
      boolean organizationIdGenerationEnabled) {
    return StringUtils.isBlank(zohoRecordEuropeanaID) && !organizationIdGenerationEnabled;
  }

  boolean skipNonExisting(boolean hasDpsOwner, boolean markedForDeletion) {
    // skip deprecated if not explicitly enabled in configs
    // skip if owner is not DPS Team
    return (markedForDeletion && !emConfiguration.isRegisterDeprecated()) || !hasDpsOwner;
  }

  boolean needsToBeEnabled(EntityRecord entityRecord, boolean hasDpsOwner,
      boolean markedForDeletion) {
    return entityRecord != null && entityRecord.isDisabled() && hasDpsOwner && !markedForDeletion;
  }

}
