package eu.europeana.entitymanagement.batch.service;


import eu.europeana.entitymanagement.batch.config.EntityUpdateJobConfig;
import eu.europeana.entitymanagement.batch.BatchUtils;
import eu.europeana.entitymanagement.batch.model.BatchUpdateType;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.solr.SolrSearchPaginatingIterator;
import eu.europeana.entitymanagement.solr.exception.SolrServiceException;
import eu.europeana.entitymanagement.solr.model.SolrEntity;
import eu.europeana.entitymanagement.solr.service.SolrService;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;
import eu.europeana.entitymanagement.web.model.SearchRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.SYNC_JOB_LAUNCHER;

@Service
public class EntityUpdateService {
    private static final Logger logger = LogManager.getLogger(EntityUpdateService.class);

    private final EntityUpdateJobConfig entityUpdateJobConfig;
    private final JobLauncher synchronousJobLauncher;

    private final ScheduledTaskService scheduledTaskService;
    private final SolrService solrService;

    @Autowired
    public EntityUpdateService(EntityUpdateJobConfig entityUpdateJobConfig,
                               @Qualifier(SYNC_JOB_LAUNCHER) JobLauncher synchronousJobLauncher,
                               ScheduledTaskService scheduledTaskService, SolrService solrService) {
        this.entityUpdateJobConfig = entityUpdateJobConfig;
        this.scheduledTaskService = scheduledTaskService;
        this.synchronousJobLauncher = synchronousJobLauncher;
        this.solrService = solrService;
    }

    /**
     * Synchronously updates the entity with the given entityId
     *
     * @param entityId entityId
     * @throws Exception on exception
     */
    public void runSynchronousUpdate(String entityId) throws Exception {
        logger.info("Triggering synchronous update for entityId={}", entityId);
        synchronousJobLauncher.run(entityUpdateJobConfig.updateSingleEntity(),
                BatchUtils.createJobParameters(entityId, Date.from(Instant.now())));
    }


    /**
     * Schedules entities with the given entityIds for an update.
     *
     * @param entityIds  list of entity ids
     * @param updateType type of update to schedule
     */
    public void scheduleUpdates(List<String> entityIds, BatchUpdateType updateType) {
        if (CollectionUtils.isEmpty(entityIds)) {
            return;
        }
        logger.info("Scheduling async updates for entityIds={}, count={}", Arrays.toString(entityIds.toArray()),
                entityIds.size());
        scheduledTaskService.scheduleUpdateBulk(entityIds, updateType);
    }

    /**
     * Schedules entity updates using a search query
     *
     * @param searchRequest contains search query
     * @param updateType    update type to schedule
     * @throws SolrServiceException if error occurs during search
     */
    public void scheduleUpdatesWithSearch(SearchRequest searchRequest, BatchUpdateType updateType) throws SolrServiceException {
        SolrSearchPaginatingIterator iterator = solrService.getSearchIterator(searchRequest.getQuery(),
                List.of(EntitySolrFields.TYPE, EntitySolrFields.ID));

        while (iterator.hasNext()) {
            List<SolrEntity<Entity>> solrEntities = iterator.next();
            scheduleUpdates(solrEntities.stream()
                            .map(SolrEntity::getEntityId)
                            .collect(Collectors.toList()),
                    updateType);
        }
    }
}
