package eu.europeana.entitymanagement.batch.service;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.UpdateResult;
import dev.morphia.query.experimental.filters.Filter;
import eu.europeana.entitymanagement.batch.model.BatchUpdateType;
import eu.europeana.entitymanagement.batch.model.ScheduledTask;
import eu.europeana.entitymanagement.batch.repository.ScheduledTaskRepository;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduledTaskService {

    private final ScheduledTaskRepository repository;
    private static final Logger logger = LogManager.getLogger(ScheduledTaskService.class);

    @Autowired
    public ScheduledTaskService(
            ScheduledTaskRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates {@link ScheduledTask} instances for entityIds, and then saves them to the
     * database
     *
     * @param entityIds list of entityIds
     */
    public void scheduleUpdateBulk(List<String> entityIds, BatchUpdateType updateType) {
        Instant now = Instant.now();

        List<ScheduledTask> tasks = entityIds.stream()
                .map(entityId ->
                        new ScheduledTask.Builder(entityId, updateType)
                                .modified(now)
                                .build()
                ).collect(Collectors.toList());

        BulkWriteResult writeResult = repository.upsertBulk(tasks);
        logger.info("Persisted scheduled tasks to db: matched={}, modified={}, inserted={}",
                writeResult.getMatchedCount(), writeResult.getModifiedCount(),
                writeResult.getInsertedCount());
    }

    /**
     * Removes entities from the ScheduledTasks collection if their entityId is contained within
     * the provided entityIds
     *
     * @param entityIds list of entityIds
     */
    public void removedTasks(List<String> entityIds) {
        long removeCount = repository.removeTasks(entityIds);
        if (removeCount > 0) {
            logger.info("Removed scheduled tasks from db: count={}", removeCount);
        }
    }

    public List<? extends EntityRecord> getEntityRecordsForTasks(int start, int count, Filter[] queryFilters) {
        return repository.getEntityRecordsForTasks(start, count, queryFilters);
    }


    /**
     * Removes entities from the ScheduledTasks collection if their entityId is contained within
     * the provided entityIds
     *
     * @param entityIds list of entityIds
     */
    public void removeTasks(List<String> entityIds) {
        long removeCount = repository.removeTasks(entityIds);
        if (removeCount > 0) {
            logger.info("Removed tasks from db: count={}", removeCount);
        }
    }


}
