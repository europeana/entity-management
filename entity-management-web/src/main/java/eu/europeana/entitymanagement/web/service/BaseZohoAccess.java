package eu.europeana.entitymanagement.web.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.zoho.crm.api.record.DeletedRecord;
import com.zoho.crm.api.record.Record;
import eu.europeana.api.commons.definitions.utils.DateUtils;
import eu.europeana.entitymanagement.batch.service.EntityUpdateService;
import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.config.DataSources;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledUpdateType;
import eu.europeana.entitymanagement.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.exception.EntityCreationException;
import eu.europeana.entitymanagement.exception.FunctionalRuntimeException;
import eu.europeana.entitymanagement.exception.ingestion.EntityUpdateException;
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;
import eu.europeana.entitymanagement.mongo.repository.ZohoSyncRepository;
import eu.europeana.entitymanagement.solr.exception.SolrServiceException;
import eu.europeana.entitymanagement.solr.service.SolrService;
import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.web.model.BatchOperations;
import eu.europeana.entitymanagement.web.model.Operation;
import eu.europeana.entitymanagement.web.model.ZohoSyncReport;
import eu.europeana.entitymanagement.web.model.ZohoSyncReportFields;
import eu.europeana.entitymanagement.zoho.organization.ZohoAccessConfiguration;
import eu.europeana.entitymanagement.zoho.organization.ZohoOrganizationConverter;
import eu.europeana.entitymanagement.zoho.utils.ZohoUtils;

public class BaseZohoAccess {

  static final Logger logger = LogManager.getLogger(BaseZohoAccess.class);
  
  final EntityRecordService entityRecordService;

  final EntityRecordRepository entityRecordRepository;

  final EntityUpdateService entityUpdateService;

  final EntityManagementConfiguration emConfiguration;

  final DataSources datasources;

  final DataSource zohoDataSource;
  
  final ZohoAccessConfiguration zohoAccessConfiguration;

  final ZohoSyncRepository zohoSyncRepo;

  
  public BaseZohoAccess(
      EntityRecordService entityRecordService,
      EntityUpdateService entityUpdateService,
      EntityRecordRepository entityRecordRepository,
      EntityManagementConfiguration emConfiguration,
      DataSources datasources,
      ZohoAccessConfiguration zohoAccessConfiguration,
      SolrService solrService,
      ZohoSyncRepository zohoSyncRepo) {
    this.entityRecordService = entityRecordService;
    this.entityUpdateService = entityUpdateService;
    this.entityRecordRepository = entityRecordRepository;
    this.emConfiguration = emConfiguration;
    this.datasources = datasources;
    this.zohoAccessConfiguration = zohoAccessConfiguration;
    this.zohoDataSource = initZohoDataSource();
    this.zohoSyncRepo = zohoSyncRepo;
  }
  
  protected DataSource initZohoDataSource() {
    Optional<DataSource> zohoDatasource = datasources.getDatasourceById(DataSource.ZOHO_ID);
    if (zohoDatasource.isEmpty()) {
      throw new FunctionalRuntimeException(
          "No zoho data source found! Zoho data source must be present in configurations with id: "
              + DataSource.ZOHO_ID);
    }
    return zohoDatasource.get();
  }

  OffsetDateTime generateFixDate() {
    //hardcoded date, just for manual testing
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.ENGLISH);
    String dateInString = "23-Oct-2023 14:38:00"; 
    try {
      Date date = formatter.parse(dateInString);
      return DateUtils.toOffsetDateTime(date);
    } catch (ParseException e) {
      logger.warn("Invalid date format!", e);
    }
    return null;
  }

  protected String buildErrorMessage(String message, List<String> ids) {
    if (ids != null) {
      StringBuilder builder = new StringBuilder(message);
      builder.append(ids);
      message = builder.toString();
    }
    return message;
  }

  protected void logExecutionProgress(List<Record> orgList, int page, final int pageSize) {
    int start = (page - 1) * pageSize;
    int end = start + orgList.size();
    logger.debug("Processing organizations set: {} - {}", start, end);
  }

  protected void performOperations(BatchOperations operations, ZohoSyncReport zohoSyncReport) {
    // do not throw exceptions but add them to failed operations of the zohoSyncReport
    // process first the create operations
    performCreateOperations(operations.getCreateOperations(), zohoSyncReport);
  
    //deprecation
    performDeprecationOperations(operations.getDeleteOperations(), zohoSyncReport);
    
    //enabling
    performEnablingOperations(operations.getDeleteOperations(), zohoSyncReport);
    
    
    // scheduled updates at the end, otherwise the other operations may overwrite the record in the db with the old captured in the operation
    performUpdateOperations(operations.getUpdateOperations(), zohoSyncReport);
  
  }

  void performPermanentDeleteOperations(SortedSet<Operation> permanentDeleteOperations, ZohoSyncReport zohoSyncReport) {
    if (permanentDeleteOperations == null || permanentDeleteOperations.isEmpty()) {
      return;
    }
  
    List<String> entitiesToDelete =
        permanentDeleteOperations.stream()
            .map(permDelete -> permDelete.getZohoEuropeanaId())
            .collect(Collectors.toList());
    try {
      runPermanentDelete(entitiesToDelete, zohoSyncReport);
    } catch (SolrServiceException | RuntimeException e) {
      String message =
          "Cannot perform permanent delete operations for organizations with ids:"
              + entitiesToDelete.toArray();
      zohoSyncReport.addFailedOperation(
          null, ZohoSyncReportFields.ENTITY_DELETION_ERROR, message, e);
    }
  }
  
  void runPermanentDelete(List<String> entitiesDeletedInZoho, ZohoSyncReport zohoSyncReport)
      throws SolrServiceException {
    long deleted = entityRecordService.deleteBulk(entitiesDeletedInZoho, true);
    zohoSyncReport.increaseDeleted(deleted);
  }


  void performDeprecationOperations(SortedSet<Operation> deprecateOperations, ZohoSyncReport zohoSyncReport) {
      if (deprecateOperations == null || deprecateOperations.isEmpty()) {
        return;
      }
      
      //first update the EuropeanaId in Zoho and disable organizations
      for (Operation operation : deprecateOperations) {
        // deprecate if not already deprecated
        if(operation.getEntityRecord().isDisabled()) {
          logger.info(
              "Organization was marked for deletion, but it is already disabled. Skipping disable for id: {}",
              operation.getZohoEuropeanaId());        
        } else {
          //registers also the failed operations
          performDeprecation(zohoSyncReport, operation);
        }
        
        /*
         * SG: note, the implementation was changed to sync execution, the following comments might be outdated, but probably still a concern
         * CAUTION: this update is a scheduled update, which will modify the record from the db,
         * and therefore must be execute after the previous deprecation step which operates on the
         * record which is already taken from the db. If this does not hold, the deprecation would overwrite
         * the modified record from update with the old record which is incorrect.
         */
        //through this update, the sameAs field from zoho should also end up in the sameAs field of the entity
        try {
          //SG: run update synchronously as we don't have many entities disabled and we can report failures 
          entityUpdateService.runSynchronousUpdate(operation.getEntityRecord().getEntityId());
        } catch (Exception e) {
          zohoSyncReport.addFailedOperation(
              operation.getZohoEuropeanaId(), ZohoSyncReportFields.ENTITY_SYNCHRONOUS_UPDATE_ERROR, e);
        }
      }
    }

  String generateZohoOrganizationUrl(Long zohoRecordId) {
    return ZohoUtils.buildZohoOrganizationId(zohoAccessConfiguration.getZohoBaseUrl(), zohoRecordId);
  }

  private void performDeprecation(ZohoSyncReport zohoSyncReport, Operation operation) {
    try {
      entityRecordService.disableEntityRecord(operation.getEntityRecord(), false);
      zohoSyncReport.increaseDeprecated(1);
    } catch (EntityUpdateException e) {
      zohoSyncReport.addFailedOperation(
          operation.getZohoEuropeanaId(), ZohoSyncReportFields.SOLR_DELETION_ERROR, e);
    } catch (RuntimeException e) {
      zohoSyncReport.addFailedOperation(
          operation.getZohoEuropeanaId(), ZohoSyncReportFields.ENTITY_DEPRECATION_ERROR, e);
    }
  }
  
  void performEnablingOperations(SortedSet<Operation> enablingOperations,
      ZohoSyncReport zohoSyncReport) {
    if (enablingOperations == null || enablingOperations.isEmpty()) {
      return;
    }
    
    //first update the EuropeanaID and enable the records in the database
    for (Operation operation : enablingOperations) {
      
      //first enable the records in the db if disabled (synchronous)
      if(operation.getEntityRecord().isDisabled()) {
        //SG: actually the isDisabled check is redundant, but it is ok to keep the check here as well
        try {
          entityRecordService.enableEntityRecord(operation.getEntityRecord());
          zohoSyncReport.increaseEnabled(1);
        } catch (RuntimeException | EntityUpdateException e) {
          zohoSyncReport.addFailedOperation(
              operation.getEntityRecord().getEntityId(), ZohoSyncReportFields.ENABLE_ERROR, e);
        }
      } else {
        logger.info(
            "The enable operation was not performed as the entity is already enabled in the database. Skipping enable for id: {}",
            operation.getZohoEuropeanaId()); 
      }
    }
    
    //update the records (async through the scheduled tasks)   
    List<String> entityIds =
        enablingOperations.stream()
            .map(operation -> operation.getEntityRecord().getEntityId())
            .collect(Collectors.toList());
    try {
      entityUpdateService.scheduleTasks(entityIds, ScheduledUpdateType.FULL_UPDATE);
      //not needed to update to updated field in the report, as the enabled counter was already updated   
    } catch (RuntimeException e) {
      String message =
          "Cannot schedule update operations for organizations with ids:"
              + entityIds.toArray();
      zohoSyncReport.addFailedOperation(null, ZohoSyncReportFields.CREATION_ERROR, message, e);
    }
    
  }

  void performUpdateOperations(SortedSet<Operation> updateOperations, ZohoSyncReport zohoSyncReport) {
    if (updateOperations == null || updateOperations.isEmpty()) {
      return;
    }
    
    //update the records (async through the scheduled tasks)   
    List<String> entityIds =
        updateOperations.stream()
            .map(operation -> operation.getEntityRecord().getEntityId())
            .collect(Collectors.toList());
    try {
      entityUpdateService.scheduleTasks(entityIds, ScheduledUpdateType.FULL_UPDATE);
      zohoSyncReport.increaseUpdated(updateOperations.size());
    } catch (RuntimeException e) {
      String message =
          "Cannot schedule update operations for organizations with ids:"
              + entityIds.toArray();
      zohoSyncReport.addFailedOperation(null, ZohoSyncReportFields.CREATION_ERROR, message, e);
    }
  }

  void performCreateOperations(SortedSet<Operation> createOperations, ZohoSyncReport zohoSyncReport) {
  
    if (createOperations == null || createOperations.isEmpty()) {
      return;
    }
  
    //also collects failed operations
    List<String> entitiesToUpdate = performEntityRegistration(createOperations, zohoSyncReport);
    
    // schedule updates
    try {
      entityUpdateService.scheduleTasks(entitiesToUpdate, ScheduledUpdateType.FULL_UPDATE);
      //note: the zoho report was allready during the entity registration
    } catch (RuntimeException e) {
      String message =
          "Cannot schedule update operations for newly created organizations with ids:"
              + entitiesToUpdate.toArray();
      zohoSyncReport.addFailedOperation(null, ZohoSyncReportFields.UPDATE_ERROR, message, e);
    }
  }

  /**
   * Registers new organizations and returns their IDs
   * @param createOperations a list of create operations
   * @param zohoSyncReport the report collecting the results of the performed operations
   * @return list of registered entity ids 
   */
  List<String> performEntityRegistration(SortedSet<Operation> createOperations, ZohoSyncReport zohoSyncReport) {
    List<String> entitiesToUpdate = new ArrayList<String>(createOperations.size());
  
    // register new entitities
    for (Operation operation : createOperations) {
      performEntityRegistration(operation, zohoSyncReport, entitiesToUpdate);
      //entity registration submits Europeana ID to zoho
      zohoSyncReport.increaseSubmittedZohoEuropeanaId();
    }
    return entitiesToUpdate;
  }

  /**
   * registers the new organization and sets the generated organizationID in the operation
   * @param operation the create operation object
   * @param zohoSyncReport the report collecting executed operations
   * @param entitiesToUpdate the ids of the newly created organizations
   */
  private void performEntityRegistration(Operation operation, ZohoSyncReport zohoSyncReport, List<String> entitiesToUpdate) {
    Organization zohoOrganization =
        ZohoOrganizationConverter.convertToOrganizationEntity(operation.getZohoRecord(), zohoAccessConfiguration.getZohoBaseUrl());
    
    try {
      Optional<EntityRecord> existingEntity =
          findDupplicateOrganization(operation, zohoOrganization);
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
                europeanaProxyEntity, zohoOrganization, getZohoDataSource(), operation.getZohoEuropeanaId());
        
        
        entitiesToUpdate.set(entitiesToUpdate.size()-1, savedEntityRecord.getEntityId());
        
        //update organization ID into the operation, generated ids are available only at this stage
        operation.setZohoEuropeanaId(savedEntityRecord.getEntityId());
        entitiesToUpdate.add(savedEntityRecord.getEntityId());
        
        zohoSyncReport.increaseCreated(1);
        
        if (logger.isDebugEnabled()) {
          logger.debug(
              "Created Entity record for externalId={}; entityId={}",
              zohoOrganization.getAbout(),
              savedEntityRecord.getEntityId());
        }
      }
    } catch (EntityCreationException | UnsupportedEntityTypeException e) {
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

  Optional<EntityRecord> findDupplicateOrganization(Operation operation,
      Organization zohoOrganization) {
    List<String> allCorefs = new ArrayList<>();
    if(operation.getZohoEuropeanaId() != null) {
      allCorefs.add(operation.getZohoEuropeanaId());
    }
    allCorefs.add(zohoOrganization.getAbout());
    String Europeana_ID = ZohoOrganizationConverter.getEuropeanaIdFieldValue(operation.getZohoRecord());
    if(Europeana_ID != null) {
      allCorefs.add(Europeana_ID);
    }
    if(zohoOrganization.getSameReferenceLinks() != null && !zohoOrganization.getSameReferenceLinks().isEmpty()) {
      allCorefs.addAll(zohoOrganization.getSameReferenceLinks());
    }
    
    if(logger.isDebugEnabled()) {
      logger.debug("Searching existing organizations by corefs: {}", allCorefs);
    }
      
    Optional<EntityRecord> existingEntity =
        entityRecordService.findEntityDupplicationByCoreference(allCorefs, null);
    
    if(logger.isDebugEnabled() && existingEntity.isPresent()) {
      logger.debug("Found existing dupplicated organization with id: {} ", existingEntity.get().getEntityId());
    }
    return existingEntity;
  }

  /**
   * This method validates that organization ownership match to the filter criteria.
   *
   * @param zohoRecord
   * @return
   */
  protected boolean hasRequiredOwnership(Record zohoRecord) {
    String ownerName = ZohoOrganizationConverter.getOwnerName(zohoRecord);
    return ownerName.equals(emConfiguration.getZohoSyncOwnerFilter());
  }

  protected Set<String> getZohoUrl(final List<Record> orgList) {
    Set<String> modifiedInZoho = new HashSet<String>();
    // get the id list from Zoho deleted Record
    if (!orgList.isEmpty()) {
      orgList.forEach(
          updatedRecord ->
              modifiedInZoho.add(generateZohoOrganizationUrl(updatedRecord.getId())));
    }
    return modifiedInZoho;
  }


  protected List<String> getDeletedEntityIds(final List<DeletedRecord> deletedInZoho) {
  
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

  protected Optional<EntityRecord> findRecordInList(Long zohoId, List<EntityRecord> existingRecords) {
    if (existingRecords == null || existingRecords.isEmpty()) {
      return Optional.ofNullable(null);
    }
    String zohoUrl = generateZohoOrganizationUrl(zohoId);
    //find the record with the zohoId in the proxies    
    return existingRecords.stream().filter(er -> er.getExternalProxyIds().contains(zohoUrl)).findFirst();
  }
  
  /** @return the Zoho DataSource object */
  public DataSource getZohoDataSource() {
    return zohoDataSource;
  }

}
