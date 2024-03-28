package eu.europeana.entitymanagement.web.service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import com.zoho.crm.api.record.DeletedRecord;
import com.zoho.crm.api.record.Record;
import dev.morphia.query.experimental.filters.Filter;
import dev.morphia.query.experimental.filters.Filters;
import eu.europeana.api.commons.definitions.utils.DateUtils;
import eu.europeana.api.commons.web.model.vocabulary.Operations;
import eu.europeana.entitymanagement.batch.service.EntityUpdateService;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.config.AppAutoconfig;
import eu.europeana.entitymanagement.config.DataSources;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.exception.ingestion.EntityUpdateException;
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;
import eu.europeana.entitymanagement.mongo.repository.ZohoSyncRepository;
import eu.europeana.entitymanagement.solr.exception.SolrServiceException;
import eu.europeana.entitymanagement.solr.service.SolrService;
import eu.europeana.entitymanagement.web.model.BatchOperations;
import eu.europeana.entitymanagement.web.model.Operation;
import eu.europeana.entitymanagement.web.model.ZohoSyncReport;
import eu.europeana.entitymanagement.web.model.ZohoSyncReportFields;
import eu.europeana.entitymanagement.zoho.organization.ZohoConfiguration;
import eu.europeana.entitymanagement.zoho.organization.ZohoOrganizationConverter;
import eu.europeana.entitymanagement.zoho.utils.ZohoConstants;
import eu.europeana.entitymanagement.zoho.utils.ZohoException;

@Service(AppAutoconfig.BEAN_ZOHO_SYNC_SERVICE)
public class ZohoSyncService extends BaseZohoAccess {

  @Autowired
  public ZohoSyncService(EntityRecordService entityRecordService,
      EntityUpdateService entityUpdateService, EntityRecordRepository entityRecordRepository,
      EntityManagementConfiguration emConfiguration, DataSources datasources,
      ZohoConfiguration zohoConfiguration,
      SolrService solrService,
      ZohoSyncRepository zohoSyncRepo) {

    super(entityRecordService, entityUpdateService, entityRecordRepository, emConfiguration,
        datasources, zohoConfiguration, solrService, zohoSyncRepo);
  }

  /**
   * method to run the zoho synchronization for organizations updated since last successfully
   * scheduled synchronization (updates are run asynchronously)
   *
   * @return the report on performed operations
   * @throws EntityUpdateException
   */
  public ZohoSyncReport synchronizeModifiedZohoOrganizations() throws EntityUpdateException {

    ZohoSyncReport previousSync = zohoSyncRepo.findLastZohoSyncReport();
    OffsetDateTime modifiedSince;
    if (previousSync != null && previousSync.getStartDate() != null) {
      modifiedSince = DateUtils.toOffsetDateTime(previousSync.getStartDate());
    } else {
      modifiedSince = OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
    }

    // for development debugging purposes use modifiedSince = generateFixDate();
    // modifiedSince = generateFixDate();

    return synchronizeZohoOrganizations(modifiedSince);
  }

  /**
   * main method to run the zoho synchronization
   *
   * @param modifiedSince the start date from which the Zoho modifications must be synchronized
   * @return the report on performed operations
   * @throws EntityUpdateException
   */
  public ZohoSyncReport synchronizeZohoOrganizations(@NonNull OffsetDateTime modifiedSince)
      throws EntityUpdateException {

    if (logger.isInfoEnabled()) {
      logger.info("Synchronizing organizations updated after date: {}", modifiedSince);
    }

    ZohoSyncReport zohoSyncReport = new ZohoSyncReport(new Date());
    // synchronize updated
    synchronizeZohoOrganizations(modifiedSince, zohoSyncReport);
    // synchronize deleted in zoho
    synchronizeDeletedZohoOrganizations(modifiedSince, zohoSyncReport);

    logger.info("Zoho update operations completed successfully:\n {}", zohoSyncReport);

    return zohoSyncRepo.save(zohoSyncReport);
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
      // OffsetDateTime offsetDateTime = modifiedSince.toInstant()
      // .atOffset(ZoneOffset.UTC);
      try {
        orgList = zohoConfiguration.getZohoAccessClient().getZcrmRecordOrganizations(page,
            pageSize, modifiedSince);

        logExecutionProgress(orgList, page, pageSize);
      } catch (ZohoException e) {
        // break if Zoho access failures occurs
        // sets also execution status
        zohoSyncReport.addFailedOperation(null, ZohoSyncReportFields.ZOHO_ACCESS_ERROR,
            "Zoho synchronization exception occured when handling organizations modified in Zoho, the execution was interupted without updating all organizations",
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
              "Processing of deleted records is complete on page on page {}, pageSize {}, items on last page {}",
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
   * @return the number of deleted from Enrichment database organizations
   * @throws EntityUpdateException
   * @throws ZohoException
   * @throws OrganizationImportException
   */
  void synchronizeDeletedZohoOrganizations(OffsetDateTime modifiedSince,
      ZohoSyncReport zohoSyncReport) throws EntityUpdateException {

    // do not delete organizations for individual entity importer
    // in case of full import the database should be manually cleaned. No need to delete
    // organizations
    List<DeletedRecord> deletedRecordsInZoho;
    int startPage = 1;
    int pageSize = emConfiguration.getZohoSyncBatchSize();
    boolean hasNext = true;
    int currentPageSize = 0;
    List<String> entitiesDeletedInZoho = null;
    // Zoho doesn't return the total results
    while (hasNext) {
      try {
        // list of (europeana) organizations ids
        deletedRecordsInZoho = zohoConfiguration.getZohoAccessClient()
            .getZohoDeletedRecordOrganizations(modifiedSince, startPage, pageSize);

        currentPageSize = deletedRecordsInZoho.size();
        // check exists in EM (Note: zoho doesn't support filtering by lastModified for deleted
        // entities)
        entitiesDeletedInZoho = getDeletedEntityIds(deletedRecordsInZoho);

        // build delete operations set
        runPermanentDelete(entitiesDeletedInZoho, zohoSyncReport);
      } catch (ZohoException e) {
        logger.error(
            "Zoho synchronization exception occured when handling organizations deleted in Zoho",
            e);
        zohoSyncReport.addFailedOperation(null, ZohoSyncReportFields.ZOHO_ACCESS_ERROR, e);
      } catch (SolrServiceException | RuntimeException e) {
        logger.error(
            "Zoho synchronization exception occured when handling organizations deleted in Zoho",
            e);
        String message =
            buildErrorMessage("Unexpected error occured when deleting organizations with ids: ",
                entitiesDeletedInZoho);
        zohoSyncReport.addFailedOperation(null, ZohoSyncReportFields.ENTITY_DELETION_ERROR, message,
            e);
      }

      if (isLastPage(currentPageSize, pageSize)) {
        // last page: if no more organizations exist in Zoho
        if (logger.isDebugEnabled()) {
          logger.debug(
              "Processing of deleted records is complete on page on page {}, pageSize {}, items on last page {}",
              startPage, pageSize, currentPageSize);
        }
        hasNext = false;
      } else {
        // go to next page
        startPage++;
      }
    }
  }

  boolean isLastPage(int currentPageSize, int maxItemsPerPage) {
    // END LOOP: if no more organizations exist in Zoho
    return currentPageSize < maxItemsPerPage;
    // return currentPageSize == 0;
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
    // List<EntityRecord> existingRecords = findEntityRecordsBySameAs(modifiedZohoUrls);
    List<EntityRecord> existingEntityRecords = findEntityRecordsByProxyId(modifiedZohoUrls);

    Long zohoId;
    for (Record zohoOrg : orgList) {
      // if full import then always update no deletion required
      zohoId = zohoOrg.getId();
      // organizationId = EntityRecordUtils.buildEntityIdUri(EntityTypes.Organization, zohoId);
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
    return entityRecordRepository.findWithCount(0, modifiedInZoho.size(),
        new Filter[] {proxyIdsFilter});
  }

  private void addOperation(BatchOperations operations, Long zohoId, Record zohoOrg,
      EntityRecord entityRecord) {

    // String zohoBasedEntityId =
    // EntityRecordUtils.buildEntityIdUri(EntityTypes.Organization, zohoId.toString());
    String zohoRecordEuropeanaID = ZohoOrganizationConverter.getEuropeanaIdFieldValue(zohoOrg);

    boolean hasDpsOwner = hasRequiredOwnership(zohoOrg);
    boolean markedForDeletion = ZohoOrganizationConverter.isMarkedForDeletion(zohoOrg);

    String emOperation = identifyOperationType(zohoId, zohoRecordEuropeanaID, entityRecord,
        hasDpsOwner, markedForDeletion, zohoOrg);

    if (emOperation != null) {
      // only if there is an operation to perform in EM
      // zohoRecordEuropeanaID might be null at this stage
      Operation operation =
          new Operation(zohoRecordEuropeanaID, emOperation, zohoOrg, entityRecord);
      operations.addOperation(operation);
    } else {
      // skip
      if (logger.isDebugEnabled()) {
        logger.debug(
            "Organization has changed in zoho, but there is no operation to perform on entity database, "
                + "probably becasue the zoho organization doesn't have the required role, it was marked for deletion, "
                + "or the generation of Organizations is not allowed for this job instance. Zoho id: {}, "
                + "hasDpsOwner: {}, markedForDeletion: {}, entityRecord: {}",
            zohoId, hasDpsOwner, markedForDeletion,
            entityRecord != null ? entityRecord.getEntityId() : "null");
      }
    }
  }

  String identifyOperationType(Long zohoId, String zohoRecordEuropeanaID, EntityRecord entityRecord,
      boolean hasDpsOwner, boolean markedForDeletion, Record zohoOrg) {

    if (entityRecord == null) {
      return shouldCreate(zohoId, zohoRecordEuropeanaID, hasDpsOwner, markedForDeletion)
          ? Operations.CREATE
          : null;
    } else if(emConfiguration.isGenerateOrganizationEuropeanaId() && isModifiedByApi(zohoOrg)) {
      if(logger.isInfoEnabled()) {
      logger.info(
          "Skip Organization organization, it was last modified by the API by the  {}",
          zohoId);
      }
      return null;
    } else if (shouldDisable(hasDpsOwner, markedForDeletion)) {
      // if needsToBeDisabled
      return Operations.DELETE;
      // NOTE: the perform deletion needs to be updated to schedule updates
    } else if (shouldEnable(entityRecord, hasDpsOwner, markedForDeletion)) {
      // enable and update
      // if needsToBeDidabled
      return Operations.ENABLE;
    } else {
      if (StringUtils.isBlank(zohoRecordEuropeanaID)) {
        logger.warn(
            "Zoho Organization doesn't have a Europeana ID, it should be set in Zoho before running the update workflow {}",
            zohoId);
      }
      return Operations.UPDATE;
    }
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
          "Organization has changed in zoho, but it is marked for deletion or doesn't have DPS as Owner. Skipped creation for Zoho id: {}, hasDpsOwner: {}, markedForDeletion: {}",
          zohoId, hasDpsOwner, markedForDeletion);
      // skipped
      return false;
    }

    if (skipNoZohoEuropeanaId(zohoRecordEuropeanaID,
        emConfiguration.isGenerateOrganizationEuropeanaId())) {
      logger.debug(
          "Organization has changed in zoho, but the job instance is not allowed to generate entity ids for Organizations. Skipped entity registration for Zoho id: {}",
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
    //skip deprecated if not explicitly enabled in configs
    //skip if owner is not DPS Team
    return (markedForDeletion && !emConfiguration.isRegisterDeprecated())
        || !hasDpsOwner;
  }

  boolean needsToBeEnabled(EntityRecord entityRecord, boolean hasDpsOwner,
      boolean markedForDeletion) {
    return entityRecord != null && entityRecord.isDisabled() && hasDpsOwner && !markedForDeletion;
  }

}
