package eu.europeana.entitymanagement.web.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;
import eu.europeana.entitymanagement.solr.exception.SolrServiceException;
import eu.europeana.entitymanagement.solr.service.SolrService;
import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.web.model.BatchOperations;
import eu.europeana.entitymanagement.web.model.Operation;
import eu.europeana.entitymanagement.web.model.ZohoSyncReport;
import eu.europeana.entitymanagement.zoho.organization.ZohoAccessConfiguration;
import eu.europeana.entitymanagement.zoho.organization.ZohoOrganizationConverter;
import eu.europeana.entitymanagement.zoho.utils.ZohoConstants;
import eu.europeana.entitymanagement.zoho.utils.ZohoException;

@Service(AppConfig.BEAN_ZOHO_SYNC_SERVICE)
public class ZohoSyncService {

  private final EntityRecordService entityRecordService;

  private final EntityRecordRepository entityRecordRepository;

  private final EntityUpdateService entityUpdateService;

  final EntityManagementConfiguration emConfiguration;

  private final DataSources datasources;

  private DataSource zohoDataSource;

  private final ZohoAccessConfiguration zohoAccessConfiguration;

  private final SolrService solrService;

  private static final Logger logger = LogManager.getLogger(ZohoSyncService.class);

  @Autowired
  public ZohoSyncService(EntityRecordService entityRecordService,
      EntityUpdateService entityUpdateService, EntityRecordRepository entityRecordRepository,
      EntityManagementConfiguration emConfiguration, DataSources datasources,
      ZohoAccessConfiguration zohoAccessConfiguration, SolrService solrService) {
    this.entityRecordService = entityRecordService;
    this.entityUpdateService = entityUpdateService;
    this.entityRecordRepository = entityRecordRepository;
    this.emConfiguration = emConfiguration;
    this.datasources = datasources;
    this.zohoAccessConfiguration = zohoAccessConfiguration;
    this.solrService = solrService;

  }

  public ZohoSyncReport synchronizeZohoOrganizations(@NotNull OffsetDateTime modifiedSince) {
    ZohoSyncReport zohoSyncReport = new ZohoSyncReport(new Date());
    // synchronize updated
    synchronizeZohoOrganizations(modifiedSince, zohoSyncReport);

    // synchronize deleted only if execution is not intrerrupted
    if (!zohoSyncReport.isExecutionIntrerrupted()) {
      synchronizeDeletedZohoOrganizations(modifiedSince, zohoSyncReport);
    }
    return zohoSyncReport;
  }

  void synchronizeZohoOrganizations(@NotNull OffsetDateTime modifiedSince,
      ZohoSyncReport zohoSyncReport) {
    List<Record> orgList;
    BatchOperations operations;

    int page = 1;
    int pageSize = emConfiguration.getZohoSyncBatchSize();
    boolean hasNext = true;
    // Zoho doesn't return the number of organizations in get response.
    try {
      while (hasNext) {
        // retrieve modified organizations
        // OffsetDateTime offsetDateTime = modifiedSince.toInstant()
        // .atOffset(ZoneOffset.UTC);
        orgList = zohoAccessConfiguration.getZohoAccessClient().getZcrmRecordOrganizations(page, pageSize, modifiedSince);
        logExecutionProgress(orgList, page, pageSize);

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
      zohoSyncReport.setExecutionStatus(ZohoSyncReport.STATUS_COMPLETED);
    } catch (ZohoException | EntityCreationException | SolrServiceException e) {
      // break if failures occur
      logger.error(
          "Zoho synchronization exception occured when handling organizations modified in Zoho, the execution was interupted without updating all organizations",
          e);
      // sets also execution status
      zohoSyncReport.updateExecutionStatus(e);
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
  void synchronizeDeletedZohoOrganizations(OffsetDateTime modifiedSince,
      ZohoSyncReport zohoSyncReport) {

    // do not delete organizations for individual entity importer
    // in case of full import the database should be manually cleaned. No need to delete
    // organizations
    List<DeletedRecord> deletedRecordsInZoho;
    int startPage = 1;
    int pageSize = emConfiguration.getZohoSyncBatchSize();
    boolean hasNext = true;
    List<String> entitiesDeletedInZoho;
    try {
      // Zoho doesn't return the total results
      while (hasNext) {
        // list of (europeana) organizations ids
        deletedRecordsInZoho =
            zohoAccessConfiguration.getZohoAccessClient().getZohoDeletedRecordOrganizations(startPage, pageSize);

        // check exists in Metis (Note: zoho doesn't support filtering by lastModified for deleted
        // entities)
        entitiesDeletedInZoho = getDeletedEntityIds(deletedRecordsInZoho);

        // build delete operations set
        runPermanentDelete(entitiesDeletedInZoho, zohoSyncReport);

        if (isLastPage(deletedRecordsInZoho.size(), pageSize)) {
          // last page: if no more organizations exist in Zoho
          hasNext = false;
        } else {
          // go to next page
          startPage++;
        }
      }
    } catch (ZohoException e) {
      logger.error(
          "Zoho synchronization exception occured when handling organizations deleted in Zoho, the execution was interupted without updating all organizations",
          e);
      zohoSyncReport.updateExecutionStatus(e);
    }
  }

  void logExecutionProgress(List<Record> orgList, int page, final int pageSize) {
    int start = (page - 1) * pageSize;
    int end = start + orgList.size();
    logger.info("Processing organizations set: {} - {}", start, end);
  }

  private void performOperations(BatchOperations operations, ZohoSyncReport zohoSyncReport)
      throws EntityCreationException, SolrServiceException {
    // process first the create operations
    SortedSet<Operation> createOperations = operations.getCreateOperations();
    performCreateOperation(createOperations, zohoSyncReport);

    // schedule updates
    performUpdateOperations(operations.getUpdateOperations(), zohoSyncReport);
    performDeprecationOperations(operations.getDeleteOperations(), zohoSyncReport);

    // per deletion
    performPermanentDeleteOperations(operations.getPermanentDeleteOperations(), zohoSyncReport);
  }

  void performPermanentDeleteOperations(SortedSet<Operation> permanentDeleteOperations, ZohoSyncReport zohoSyncReport) {
    if (permanentDeleteOperations == null || permanentDeleteOperations.isEmpty()) {
      return;
    }
    List<String> entitiesToDelete = permanentDeleteOperations.stream()
        .map(permDelete -> permDelete.getOrganizationId()).collect(Collectors.toList());
    runPermanentDelete(entitiesToDelete, zohoSyncReport);
  }

  void performDeprecationOperations(SortedSet<Operation> deprecateOperations,
      ZohoSyncReport zohoSyncReport) throws SolrServiceException {
    if (deprecateOperations == null || deprecateOperations.isEmpty()) {
      return;
    }
    EntityRecord record;
    Organization zohoOrganization;
    for (Operation operation : deprecateOperations) {
      if(operation.getEntityRecord().isEmpty()) {
        //the record must not be empty for deprecate operations
        logger.info("Deprecation operation skipped, no entity record available: {}", operation);
        continue;
      }
      record = operation.getEntityRecord().get();
      zohoOrganization =
          ZohoOrganizationConverter.convertToOrganizationEntity(operation.getZohoRecord());
      // update sameAs
      entityRecordService.addSameReferenceLinks(record.getEntity(),
          zohoOrganization.getSameReferenceLinks());

      // deprecate
      solrService.deleteById(List.of(record.getEntityId()));
      entityRecordService.disableEntityRecord(record);
      zohoSyncReport.increaseDeprecated(1);
    }
  }



  void performUpdateOperations(SortedSet<Operation> updateOperations,
      ZohoSyncReport zohoSyncReport) {
    if (updateOperations == null || updateOperations.isEmpty()) {
      return;
    }
    List<String> organizationIds = updateOperations.stream()
        .map(operation -> operation.getOrganizationId()).collect(Collectors.toList());
    entityUpdateService.scheduleTasks(organizationIds, ScheduledUpdateType.FULL_UPDATE);
    zohoSyncReport.increaseUpdated(updateOperations.size());
  }

  private void performCreateOperation(SortedSet<Operation> createOperations,
      ZohoSyncReport zohoSyncReport) throws EntityCreationException {

    if(createOperations == null || createOperations.isEmpty()) {
      return;
    }
    List<String> entitiesToUpdate = performEntityRegistration(createOperations, zohoSyncReport);
    // schedule updates
    entityUpdateService.scheduleTasks(entitiesToUpdate, ScheduledUpdateType.FULL_UPDATE);
    zohoSyncReport.increaseCreated(entitiesToUpdate.size());
  }

  List<String> performEntityRegistration(SortedSet<Operation> createOperations,
      ZohoSyncReport zohoSyncReport) throws EntityCreationException {
    List<String> entitiesToUpdate = new ArrayList<String>(createOperations.size());
    List<String> allCorefs;
    // register new entitities
    for (Operation operation : createOperations) {
      Organization zohoOrganization =
          ZohoOrganizationConverter.convertToOrganizationEntity(operation.getZohoRecord());
      allCorefs = new ArrayList<String>();
      allCorefs.add(operation.getOrganizationId());
      allCorefs.add(zohoOrganization.getAbout());
      allCorefs.addAll(zohoOrganization.getSameReferenceLinks());

      Optional<EntityRecord> existingEntity =
          entityRecordService.findMatchingCoreference(allCorefs);
      if (existingEntity.isPresent()) {
        // skipp processing
        zohoSyncReport.addSkippedDupplicate(zohoOrganization.getAbout(),
            existingEntity.get().getEntityId());
        continue;
      } else {
        // create shell
        Organization europeanaProxyEntity = new Organization();
        // set zoho URL
        europeanaProxyEntity.setAbout(zohoOrganization.getAbout());

        EntityRecord savedEntityRecord = entityRecordService
            .createEntityFromRequest(europeanaProxyEntity, zohoOrganization, getZohoDataSource());
        entitiesToUpdate.add(savedEntityRecord.getEntityId());
        logger.info("Created Entity record for externalId={}; entityId={}", zohoOrganization.getAbout(),
            savedEntityRecord.getEntityId());
      }
    }
    return entitiesToUpdate;
  }
 

  /**
   * This method performs synchronization of organizations between Zoho and Entity API database
   * addressing deleted and unwanted (defined by owner criteria) organizations. We separate to
   * update from to delete types
   *
   * If full import -> only update/create else Update operation -> 1) search criteria is empty,
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
      addOperation(operations, zohoId, zohoOrg, entityRecordOptional);
    }
    return operations;
  }

  List<EntityRecord> findEntityRecordsById(Set<String> modifiedInZoho) {
    Filter entityIdsFilter = Filters.in(WebEntityFields.ENTITY_ID, modifiedInZoho);
    return 
        entityRecordRepository.findWithFilters(0, modifiedInZoho.size(), entityIdsFilter);
  }

  private void addOperation(BatchOperations operations, String zohoId, Record zohoOrg,
      Optional<EntityRecord> entityRecordOptional) {

    String entityId = EntityRecordUtils.buildEntityIdUri(EntityTypes.Organization, zohoId);

    boolean hasDpsOwner = hasRequiredOwnership(zohoOrg);
    String emOperation = null;
    if (entityRecordOptional.isEmpty() && hasDpsOwner) {
      // entity not in entity management database,
      // create operation if ownership is correct
      emOperation = Operations.CREATE;
    } else if (entityRecordOptional.isPresent() && hasDpsOwner) {
      // Zoho entry has changed
      emOperation = Operations.UPDATE;
    } else if (entityRecordOptional.isPresent() && !hasDpsOwner) {
      // Zoho entry has changed, but DPS ownership lost
      //TODO: Add handling for deprecated in Zoho 
      emOperation = Operations.DELETE;
    }

    if (emOperation != null) {
      // only if there is an operation to perform in EM
      Operation operation =
          new Operation(entityId, emOperation, zohoOrg, entityRecordOptional);
      operations.addOperation(operation);
    } else {
      logger.info(
          "Organization has changed in zoho, but there is no operation to perform on entity database, probably becasue the zoho organization doesn't have the required role. Zoho id: {}",
          zohoId);
    }
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
      orgList.forEach(updatedRecord -> modifiedInZoho.add(EntityRecordUtils
          .buildEntityIdUri(EntityTypes.Organization, updatedRecord.getId().toString())));
    }
    return modifiedInZoho;
  }


  List<String> getDeletedEntityIds(final List<DeletedRecord> deletedInZoho) {

    List<String> deletedEntityIds = new ArrayList<String>();
    // get the id list from Zoho deleted Record
    if (!deletedEntityIds.isEmpty()) {
      deletedInZoho.forEach(deletedRecord -> deletedEntityIds.add(EntityRecordUtils
          .buildEntityIdUri(EntityTypes.Organization, deletedRecord.getId().toString())));
    }
    return deletedEntityIds;
  }

  private Optional<EntityRecord> findRecordInList(String zohoId,
      List<EntityRecord> existingRecords) {
    if (existingRecords == null || existingRecords.isEmpty()) {
      return Optional.ofNullable(null);
    }
    String identifier = "/" + zohoId;
    return existingRecords.stream().filter(er -> er.getEntityId().endsWith(identifier)).findFirst();
  }



  private void runPermanentDelete(List<String> entitiesDeletedInZoho,
      ZohoSyncReport zohoSyncReport) {
    long deleted = entityRecordService.deleteBulk(entitiesDeletedInZoho);
    zohoSyncReport.increaseDeleted(deleted);
  }

  boolean isLastPage(int currentPageSize, int maxItemsPerPage) {
    // END LOOP: if no more organizations exist in Zoho
    return currentPageSize < maxItemsPerPage;
  }

  public DataSource getZohoDataSource() {
    if (zohoDataSource == null) {
      synchronized(this) {
        Optional<DataSource> zohoDatasource = datasources.getDatasourceById(DataSource.ZOHO_ID);
        if(zohoDatasource.isEmpty()) {
          throw new FunctionalRuntimeException("No zoho data source found! Zoho data source must be present in configurations with id: " + DataSource.ZOHO_ID); 
        }
        zohoDataSource = zohoDatasource.get();
      }
    }

    return zohoDataSource;
  }

}
