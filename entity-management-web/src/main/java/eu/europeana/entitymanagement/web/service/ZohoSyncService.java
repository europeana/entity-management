package eu.europeana.entitymanagement.web.service;

import com.zoho.crm.api.record.DeletedRecord;
import com.zoho.crm.api.record.Record;
import dev.morphia.query.experimental.filters.Filter;
import dev.morphia.query.experimental.filters.Filters;
import eu.europeana.api.commons.web.model.vocabulary.Operations;
import eu.europeana.entitymanagement.batch.service.EntityUpdateService;
import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.config.DataSources;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledUpdateType;
import eu.europeana.entitymanagement.definitions.exceptions.EntityCreationException;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.exception.FunctionalRuntimeException;
import eu.europeana.entitymanagement.exception.ingestion.EntityUpdateException;
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;
import eu.europeana.entitymanagement.solr.service.SolrService;
import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.web.model.BatchOperations;
import eu.europeana.entitymanagement.web.model.Operation;
import eu.europeana.entitymanagement.web.model.ZohoSyncReport;
import eu.europeana.entitymanagement.web.model.ZohoSyncReportFields;
import eu.europeana.entitymanagement.zoho.organization.ZohoAccessConfiguration;
import eu.europeana.entitymanagement.zoho.organization.ZohoOrganizationConverter;
import eu.europeana.entitymanagement.zoho.utils.ZohoException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service(AppConfig.BEAN_ZOHO_SYNC_SERVICE)
public class ZohoSyncService {

  private final EntityRecordService entityRecordService;

  private final EntityRecordRepository entityRecordRepository;

  private final EntityUpdateService entityUpdateService;

  final EntityManagementConfiguration emConfiguration;

  private final DataSources datasources;

  private final DataSource zohoDataSource;

  private final ZohoAccessConfiguration zohoAccessConfiguration;

  private static final Logger logger = LogManager.getLogger(ZohoSyncService.class);

  @Autowired
  public ZohoSyncService(
      EntityRecordService entityRecordService,
      EntityUpdateService entityUpdateService,
      EntityRecordRepository entityRecordRepository,
      EntityManagementConfiguration emConfiguration,
      DataSources datasources,
      ZohoAccessConfiguration zohoAccessConfiguration,
      SolrService solrService) {
    this.entityRecordService = entityRecordService;
    this.entityUpdateService = entityUpdateService;
    this.entityRecordRepository = entityRecordRepository;
    this.emConfiguration = emConfiguration;
    this.datasources = datasources;
    this.zohoAccessConfiguration = zohoAccessConfiguration;
    this.zohoDataSource = initZohoDataSource();
  }

  private DataSource initZohoDataSource() {
    Optional<DataSource> zohoDatasource = datasources.getDatasourceById(DataSource.ZOHO_ID);
    if (zohoDatasource.isEmpty()) {
      throw new FunctionalRuntimeException(
          "No zoho data source found! Zoho data source must be present in configurations with id: "
              + DataSource.ZOHO_ID);
    }
    return zohoDatasource.get();
  }

  public ZohoSyncReport synchronizeZohoOrganizations(@NotNull OffsetDateTime modifiedSince) {
    ZohoSyncReport zohoSyncReport = new ZohoSyncReport(new Date());
    // synchronize updated
    synchronizeZohoOrganizations(modifiedSince, zohoSyncReport);

    // synchronize deleted
    synchronizeDeletedZohoOrganizations(modifiedSince, zohoSyncReport);
    return zohoSyncReport;
  }

  void synchronizeZohoOrganizations(
      @NotNull OffsetDateTime modifiedSince, ZohoSyncReport zohoSyncReport) {
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
        orgList =
            zohoAccessConfiguration
                .getZohoAccessClient()
                .getZcrmRecordOrganizations(page, pageSize, modifiedSince);

        logExecutionProgress(orgList, page, pageSize);
      } catch (ZohoException e) {
        // break if Zoho access failures occurs
        // sets also execution status
        zohoSyncReport.addFailedOperation(
            null,
            ZohoSyncReportFields.ZOHO_ACCESS_ERROR,
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
        hasNext = false;
      } else {
        // go to next page
        page++;
      }
    }

    logger.info("Zoho update operations completed successfully:\n {}", zohoSyncReport);
  }

  /**
   * Retrieve deleted in Zoho organizations and removed from the Enrichment database
   *
   * @return the number of deleted from Enrichment database organizations
   * @throws ZohoException
   * @throws OrganizationImportException
   */
  void synchronizeDeletedZohoOrganizations(
      OffsetDateTime modifiedSince, ZohoSyncReport zohoSyncReport) {

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
        deletedRecordsInZoho =
            zohoAccessConfiguration
                .getZohoAccessClient()
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
      } catch (RuntimeException e) {
        logger.error(
            "Zoho synchronization exception occured when handling organizations deleted in Zoho",
            e);
        String message =
            buildErrorMessage(
                "Unexpected error occured when deleting organizations with ids: ",
                entitiesDeletedInZoho);
        zohoSyncReport.addFailedOperation(
            null, ZohoSyncReportFields.ENTITY_DELETION_ERROR, message, e);
      }

      if (isLastPage(currentPageSize, pageSize)) {
        // last page: if no more organizations exist in Zoho
        hasNext = false;
      } else {
        // go to next page
        startPage++;
      }
    }
  }

  String buildErrorMessage(String message, List<String> ids) {
    if (ids != null) {
      StringBuilder builder = new StringBuilder(message);
      builder.append(ids);
      message = builder.toString();
    }
    return message;
  }

  void logExecutionProgress(List<Record> orgList, int page, final int pageSize) {
    int start = (page - 1) * pageSize;
    int end = start + orgList.size();
    logger.info("Processing organizations set: {} - {}", start, end);
  }

  private void performOperations(BatchOperations operations, ZohoSyncReport zohoSyncReport) {
    // do not throw exceptions but add them to failed operations of the zohoSyncReport
    // process first the create operations
    SortedSet<Operation> createOperations = operations.getCreateOperations();
    performCreateOperations(createOperations, zohoSyncReport);

    SortedSet<Operation> enableOperations = operations.getEnableOperations();
    performEnableOperations(enableOperations, zohoSyncReport);

    // schedule updates
    performUpdateOperations(operations.getUpdateOperations(), zohoSyncReport);
    performDeprecationOperations(operations.getDeleteOperations(), zohoSyncReport);

    // per deletion
    performPermanentDeleteOperations(operations.getPermanentDeleteOperations(), zohoSyncReport);
  }

  void performPermanentDeleteOperations(
      SortedSet<Operation> permanentDeleteOperations, ZohoSyncReport zohoSyncReport) {
    if (permanentDeleteOperations == null || permanentDeleteOperations.isEmpty()) {
      return;
    }

    List<String> entitiesToDelete =
        permanentDeleteOperations.stream()
            .map(permDelete -> permDelete.getOrganizationId())
            .collect(Collectors.toList());
    try {
      runPermanentDelete(entitiesToDelete, zohoSyncReport);
    } catch (RuntimeException e) {
      String message =
          "Cannot perfomr permanet delete operations for organizations with ids:"
              + entitiesToDelete.toArray();
      zohoSyncReport.addFailedOperation(
          null, ZohoSyncReportFields.ENTITY_DELETION_ERROR, message, e);
    }
  }

  void performDeprecationOperations(
      SortedSet<Operation> deprecateOperations, ZohoSyncReport zohoSyncReport) {
    if (deprecateOperations == null || deprecateOperations.isEmpty()) {
      return;
    }
    Organization zohoOrganization;
    EntityRecord entityRecord;
    for (Operation operation : deprecateOperations) {
      zohoOrganization =
          ZohoOrganizationConverter.convertToOrganizationEntity(operation.getZohoRecord());
      entityRecord = operation.getEntityRecord();

      // update sameAs from zohoOrganization to the consolidated version
      entityRecordService.addSameReferenceLinks(
          entityRecord.getEntity(), zohoOrganization.getSameReferenceLinks());

      // deprecate
      performDeprecation(zohoSyncReport, operation);
    }
  }

  void performDeprecation(ZohoSyncReport zohoSyncReport, Operation operation) {
    try {
      entityRecordService.disableEntityRecord(operation.getEntityRecord());
      zohoSyncReport.increaseDeprecated(1);
    } catch (EntityUpdateException e) {
      zohoSyncReport.addFailedOperation(
          operation.getOrganizationId(), ZohoSyncReportFields.SOLR_DELETION_ERROR, e);
    } catch (RuntimeException e) {
      zohoSyncReport.addFailedOperation(
          operation.getOrganizationId(), ZohoSyncReportFields.ENTITY_DEPRECATION_ERROR, e);
    }
  }

  void performUpdateOperations(
      SortedSet<Operation> updateOperations, ZohoSyncReport zohoSyncReport) {
    if (updateOperations == null || updateOperations.isEmpty()) {
      return;
    }
    List<String> organizationIds =
        updateOperations.stream()
            .map(operation -> operation.getOrganizationId())
            .collect(Collectors.toList());
    try {
      entityUpdateService.scheduleTasks(organizationIds, ScheduledUpdateType.FULL_UPDATE);
      zohoSyncReport.increaseUpdated(updateOperations.size());
    } catch (RuntimeException e) {
      String message =
          "Cannot schedule updae operations for organizations with ids:"
              + organizationIds.toArray();
      zohoSyncReport.addFailedOperation(null, ZohoSyncReportFields.CREATION_ERROR, message, e);
    }
  }

  private void performCreateOperations(
      SortedSet<Operation> createOperations, ZohoSyncReport zohoSyncReport) {

    if (createOperations == null || createOperations.isEmpty()) {
      return;
    }

    List<String> entitiesToUpdate = performEntityRegistration(createOperations, zohoSyncReport);
    // schedule updates
    try {
      entityUpdateService.scheduleTasks(entitiesToUpdate, ScheduledUpdateType.FULL_UPDATE);
      zohoSyncReport.increaseCreated(entitiesToUpdate.size());
    } catch (RuntimeException e) {
      String message =
          "Cannot schedule updae operations for organizations with ids:"
              + entitiesToUpdate.toArray();
      zohoSyncReport.addFailedOperation(null, ZohoSyncReportFields.CREATION_ERROR, message, e);
    }
  }

  List<String> performEntityRegistration(
      SortedSet<Operation> createOperations, ZohoSyncReport zohoSyncReport) {
    List<String> entitiesToUpdate = new ArrayList<String>(createOperations.size());

    // register new entitities
    for (Operation operation : createOperations) {
      performEntityRegistration(operation, zohoSyncReport, entitiesToUpdate);
    }
    return entitiesToUpdate;
  }

  void performEntityRegistration(
      Operation operation, ZohoSyncReport zohoSyncReport, List<String> entitiesToUpdate) {
    Organization zohoOrganization =
        ZohoOrganizationConverter.convertToOrganizationEntity(operation.getZohoRecord());
    List<String> allCorefs = new ArrayList<>();
    allCorefs.add(operation.getOrganizationId());
    allCorefs.add(zohoOrganization.getAbout());
    allCorefs.addAll(zohoOrganization.getSameReferenceLinks());

    try {

      Optional<EntityRecord> existingEntity =
          entityRecordService.findEntityDupplicationByCoreference(allCorefs, null);
      if (existingEntity.isPresent()) {
        // skipp processing
        zohoSyncReport.addFailedOperation(
            zohoOrganization.getAbout(),
            "Dupplicate entity error",
            "Dupplicate of :" + existingEntity.get().getEntityId(),
            null);
      } else {
        // create shell
        Organization europeanaProxyEntity = new Organization();
        // set zoho URL
        europeanaProxyEntity.setAbout(zohoOrganization.getAbout());

        EntityRecord savedEntityRecord =
            entityRecordService.createEntityFromRequest(
                europeanaProxyEntity, zohoOrganization, getZohoDataSource());
        entitiesToUpdate.add(savedEntityRecord.getEntityId());
        if (logger.isDebugEnabled()) {
          logger.debug(
              "Created Entity record for externalId={}; entityId={}",
              zohoOrganization.getAbout(),
              savedEntityRecord.getEntityId());
        }
      }
    } catch (EntityCreationException e) {
      zohoSyncReport.addFailedOperation(
          zohoOrganization.getAbout(),
          ZohoSyncReportFields.CREATION_ERROR,
          "Entity registration failed.",
          e);
    } catch (RuntimeException e) {
      zohoSyncReport.addFailedOperation(
          zohoOrganization.getAbout(), ZohoSyncReportFields.CREATION_ERROR, e);
    }
  }

  private void performEnableOperations(
      SortedSet<Operation> enableOperations, ZohoSyncReport zohoSyncReport) {

    if (enableOperations == null || enableOperations.isEmpty()) {
      return;
    }

    for (Operation operation : enableOperations) {
      try {
        entityRecordService.enableEntityRecord(operation.getEntityRecord());
        zohoSyncReport.increaseEnabled(1);
      } catch (RuntimeException | EntityUpdateException e) {
        zohoSyncReport.addFailedOperation(
            operation.getEntityRecord().getEntityId(), ZohoSyncReportFields.ENABLE_ERROR, e);
      }
    }
  }

  /**
   * This method performs synchronization of organizations between Zoho and Entity API database
   * addressing deleted and unwanted (defined by owner criteria) organizations. We separate to
   * update from to delete types
   *
   * <p>If full import -> only update/create else Update operation -> 1) search criteria is empty,
   * check if the owner is present in the zoho record 2) search criteria present, then zoho record
   * owner should match with the search filter ZOHO_OWNER_CRITERIA Delete the record if none of the
   * above matches.
   *
   * @param orgList The list of retrieved Zoho objects
   */
  BatchOperations fillOperations(final List<Record> orgList) {
    BatchOperations operations = new BatchOperations();

    Set<String> modifiedInZoho = getEntityIds(orgList);
    List<EntityRecord> existingRecords = findEntityRecordsById(modifiedInZoho);

    String zohoId;
    for (Record zohoOrg : orgList) {
      // if full import then always update no deletion required
      zohoId = zohoOrg.getId().toString();
      // organizationId = EntityRecordUtils.buildEntityIdUri(EntityTypes.Organization, zohoId);
      Optional<EntityRecord> entityRecordOptional = findRecordInList(zohoId, existingRecords);
      EntityRecord entityRecord = null;
      if (entityRecordOptional.isPresent()) {
        entityRecord = entityRecordOptional.get();
      }

      addOperation(operations, zohoId, zohoOrg, entityRecord);
    }
    return operations;
  }

  List<EntityRecord> findEntityRecordsById(Set<String> modifiedInZoho) {
    Filter entityIdsFilter = Filters.in(WebEntityFields.ENTITY_ID, modifiedInZoho);
    return entityRecordRepository.findWithFilters(
        0, modifiedInZoho.size(), new Filter[] {entityIdsFilter});
  }

  private void addOperation(
      BatchOperations operations, String zohoId, Record zohoOrg, EntityRecord entityRecord) {

    String entityId = EntityRecordUtils.buildEntityIdUri(EntityTypes.Organization, zohoId);

    boolean hasDpsOwner = hasRequiredOwnership(zohoOrg);
    boolean markedForDeletion = ZohoOrganizationConverter.isMarkedForDeletion(zohoOrg);
    String emOperation = null;

    if (entityRecord == null) {
      if (skipNonExisting(hasDpsOwner, markedForDeletion)) {
        // skipped
        logger.debug(
            "Organization has changed in zoho, but it is marked for deletion or doesn't have DPS as Owner. Skipped update for Zoho id: {}",
            zohoId);
      } else {
        // entity not in entity management database,
        // create operation if ownership is correct
        emOperation = Operations.CREATE;
      }
    } else {
      // entity record not null, update or deletion
      if (!hasDpsOwner || markedForDeletion) {
        // lost ownership, or marked for deletion - > disable
        emOperation = Operations.DELETE;
      } else if (skipExisting(entityRecord, markedForDeletion)) {
        // skipped
        logger.debug(
            "Organization was marked for deletion, but it is already disabled. Skipped update for Zoho id: {}",
            zohoId);
      } else {
        if (needsToBeEnabled(entityRecord, hasDpsOwner, markedForDeletion)) {
          // check if need to enable
          Operation operation = new Operation(entityId, Operations.ENABLE, null, entityRecord);
          // add enable operation
          operations.addOperation(operation);
          // perform update for enabled operations
        }
        // Zoho entry has changed
        emOperation = Operations.UPDATE;
      }
    }

    if (emOperation != null) {
      // only if there is an operation to perform in EM
      Operation operation = new Operation(entityId, emOperation, zohoOrg, entityRecord);
      operations.addOperation(operation);
    } else {
      // skip
      if (logger.isInfoEnabled()) {
        logger.info(
            "Organization has changed in zoho, but there is no operation to perform on entity database, "
                + "probably becasue the zoho organization doesn't have the required role or it was marked for deletion. Zoho id: {}",
            zohoId);
      }
    }
  }

  boolean skipNonExisting(boolean hasDpsOwner, boolean markedForDeletion) {
    return markedForDeletion || !hasDpsOwner;
  }

  boolean skipExisting(EntityRecord entityRecord, boolean markedForDeletion) {
    return markedForDeletion && entityRecord.isDisabled();
  }

  boolean needsToBeEnabled(
      EntityRecord entityRecord, boolean hasDpsOwner, boolean markedForDeletion) {
    return entityRecord != null && entityRecord.isDisabled() && hasDpsOwner && !markedForDeletion;
  }

  /**
   * This method validates that organization ownership match to the filter criteria.
   *
   * @param zohoRecord
   * @return
   */
  boolean hasRequiredOwnership(Record zohoRecord) {
    String ownerName = ZohoOrganizationConverter.getOwnerName(zohoRecord);
    return ownerName.equals(emConfiguration.getZohoSyncOwnerFilter());
  }

  Set<String> getEntityIds(final List<Record> orgList) {

    Set<String> modifiedInZoho = new HashSet<String>();
    // get the id list from Zoho deleted Record
    if (!orgList.isEmpty()) {
      orgList.forEach(
          updatedRecord ->
              modifiedInZoho.add(
                  EntityRecordUtils.buildEntityIdUri(
                      EntityTypes.Organization, updatedRecord.getId().toString())));
    }
    return modifiedInZoho;
  }

  List<String> getDeletedEntityIds(final List<DeletedRecord> deletedInZoho) {

    List<String> deletedEntityIds = new ArrayList<String>();
    // get the id list from Zoho deleted Record
    if (!deletedInZoho.isEmpty()) {
      deletedInZoho.forEach(
          deletedRecord ->
              deletedEntityIds.add(
                  EntityRecordUtils.buildEntityIdUri(
                      EntityTypes.Organization, deletedRecord.getId().toString())));
    }
    return deletedEntityIds;
  }

  private Optional<EntityRecord> findRecordInList(
      String zohoId, List<EntityRecord> existingRecords) {
    if (existingRecords == null || existingRecords.isEmpty()) {
      return Optional.ofNullable(null);
    }
    String identifier = "/" + zohoId;
    return existingRecords.stream().filter(er -> er.getEntityId().endsWith(identifier)).findFirst();
  }

  private void runPermanentDelete(
      List<String> entitiesDeletedInZoho, ZohoSyncReport zohoSyncReport) {
    long deleted = entityRecordService.deleteBulk(entitiesDeletedInZoho);
    zohoSyncReport.increaseDeleted(deleted);
  }

  boolean isLastPage(int currentPageSize, int maxItemsPerPage) {
    // END LOOP: if no more organizations exist in Zoho
    return currentPageSize < maxItemsPerPage;
  }

  public DataSource getZohoDataSource() {
    return zohoDataSource;
  }
}
