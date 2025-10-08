package eu.europeana.entitymanagement.batch.service;

import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.BEAN_EM_SOLR_SERVICE;
import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.BEAN_ENTITY_RECORD_SERVICE;
import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.SYNC_WEB_REQUEST_JOB_LAUNCHER;
import static java.util.stream.Collectors.groupingBy;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import eu.europeana.entitymanagement.batch.config.EntityUpdateJobFactory;
import eu.europeana.entitymanagement.batch.model.JobDescription;
import eu.europeana.entitymanagement.batch.utils.BatchUtils;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.batch.model.TaskType;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.web.EntityIdDisabledStatus;
import eu.europeana.entitymanagement.definitions.web.EntityIdResponse;
import eu.europeana.entitymanagement.solr.SolrSearchCursorIterator;
import eu.europeana.entitymanagement.solr.exception.SolrServiceException;
import eu.europeana.entitymanagement.solr.model.SolrEntity;
import eu.europeana.entitymanagement.solr.service.SolrService;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;
import eu.europeana.entitymanagement.web.service.EntityRecordService;

@Service
public class EntityUpdateService {
  private static final Logger logger = LogManager.getLogger(EntityUpdateService.class);

  private final EntityUpdateJobFactory entityUpdateJobFactory;
  private final JobLauncher syncWebRequestLauncher;
  @Resource(name=BEAN_ENTITY_RECORD_SERVICE)
  private EntityRecordService entityRecordService;
  @Resource(name=BEAN_EM_SOLR_SERVICE)
  private SolrService solrService;
  @Resource
  private EntityManagementConfiguration emConfiguration;
  
  private final ScheduledTaskService scheduledTaskService;

  @Autowired
  public EntityUpdateService(
          EntityUpdateJobFactory entityUpdateJobFactory,
          @Qualifier(SYNC_WEB_REQUEST_JOB_LAUNCHER) JobLauncher syncWebRequestLauncher,
          ScheduledTaskService scheduledTaskService) {
    this.entityUpdateJobFactory = entityUpdateJobFactory;
    this.scheduledTaskService = scheduledTaskService;
    this.syncWebRequestLauncher = syncWebRequestLauncher;
  }

  /**
   * Synchronously updates the entity with the given entityId
   *
   * @param entityId entityId
   * @param jobDescription jobs to be run ( processors and writers)
   * @throws Exception on exception
   */
  public void runSynchronousUpdate(String entityId, JobDescription jobDescription) throws Exception {
    logger.debug("Triggering synchronous update for entityId={} with processors={}, writers={}",
            entityId, jobDescription.getProcessors() , jobDescription.getWriters());
    syncWebRequestLauncher.run(
        entityUpdateJobFactory.createJob(jobDescription),
        BatchUtils.createJobParameters(
            entityId, Date.from(Instant.now()), jobDescription.getTaskType(), true));
  }

  /**
   * Schedules entities with the given entityIds for an update.
   *
   * @param entityIds list of entity ids
   * @param updateType type of update to schedule
   */
  public void scheduleTasks(List<String> entityIds, TaskType updateType) {
    if (CollectionUtils.isEmpty(entityIds)) {
      return;
    }
    logger.info(
        "Scheduling async task for entityIds={}, count={} updateType={}",
        Arrays.toString(entityIds.toArray()),
        entityIds.size(),
        updateType);
    Map<String, TaskType> mapEntityIdScheduledTaskType =
        new HashMap<>(entityIds.size());
    for (String id : entityIds) {
      mapEntityIdScheduledTaskType.put(id, updateType);
    }
    scheduledTaskService.scheduleTasksForEntities(mapEntityIdScheduledTaskType);
  }
  
  /**
   * Method to schedule a metrics update using a solr search query
   * @param query solr search query
   * @param updateType the type of the Task to be scheduled
   * @return the results of the scheduling 
   * @throws SolrServiceException if the query is malformed or the solr is not accessible
   */
  public EntityIdResponse scheduleUpdatesWithSearch(String query, TaskType updateType)
      throws SolrServiceException {
    SolrSearchCursorIterator iterator =
        solrService.getSearchIterator(query, List.of(EntitySolrFields.TYPE, EntitySolrFields.ID));

    EntityIdResponse entityIdResponse = new EntityIdResponse();

    while (iterator.hasNext()) {
      List<SolrEntity<Entity>> solrEntities = iterator.next();

      List<String> entityIds =
          solrEntities.stream().map(SolrEntity::getEntityId).collect(Collectors.toList());

      // get the entities to be scheduled, failed and skipped for update
      List<String> entityIdsToSchedule = updateEntityIdResponse(entityIdResponse, entityIds);
      System.out.println("Entities to schedule: " + entityIdsToSchedule);

      scheduleTasks(entityIdsToSchedule, updateType);
    }
    return entityIdResponse;
  }
  
  /**
   * Generate the EntityIdResponse based on entity Ids to be processed for update
   *
   * @param entityIdResponse object to be updated
   * @param entityIds the ids of the entities to schedule updates
   * @return the list of active entities for which the update was successfully performed  
   */
  public List<String> updateEntityIdResponse(EntityIdResponse entityIdResponse,
      List<String> entityIds) {
    // Get all existing EntityIds and their disabled status
    List<EntityIdDisabledStatus> statusList =
        entityRecordService.retrieveEntityDeprecationStatus(entityIds, false);

    // extract only the entityIds for easy comparison
    List<String> existingEntityIds =
        statusList.stream().map(EntityIdDisabledStatus::getEntityId).collect(Collectors.toList());

    // failures are entityIds that weren't retrieved with status
    List<String> failures =
        entityIds.stream().filter(e -> !existingEntityIds.contains(e)).collect(Collectors.toList());

    Map<Boolean, List<EntityIdDisabledStatus>> entityIdsByDisabled =
        statusList.stream().collect(groupingBy(EntityIdDisabledStatus::isDisabled));

    // get entityIds that can be scheduled (they are not disabled)
    List<EntityIdDisabledStatus> nonDisabledEntities = entityIdsByDisabled.get(Boolean.FALSE);
    List<String> activeEntities =
        CollectionUtils.isEmpty(nonDisabledEntities) ? Collections.emptyList()
            : nonDisabledEntities.stream().map(EntityIdDisabledStatus::getEntityId)
                .collect(Collectors.toList());

    // updates skipped if EntityIdDisabledStatus.disabled=true
    List<EntityIdDisabledStatus> disabledEntities = entityIdsByDisabled.get(Boolean.TRUE);
    List<String> skipped = CollectionUtils.isEmpty(disabledEntities) ? Collections.emptyList()
        : disabledEntities.stream().map(EntityIdDisabledStatus::getEntityId)
            .collect(Collectors.toList());

    entityIdResponse.updateValues(entityIds.size(), activeEntities, failures, skipped,
        emConfiguration.getEntityIdResponseMaxSize());
    return activeEntities;
  }
}
