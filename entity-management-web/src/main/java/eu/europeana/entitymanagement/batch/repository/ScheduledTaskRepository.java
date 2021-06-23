package eu.europeana.entitymanagement.batch.repository;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import com.mongodb.client.result.UpdateResult;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;
import dev.morphia.query.experimental.filters.Filter;
import dev.morphia.query.experimental.updates.UpdateOperators;
import eu.europeana.entitymanagement.batch.model.ScheduledTask;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import org.bson.Document;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static dev.morphia.query.Sort.ascending;
import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.filters.Filters.in;
import static eu.europeana.entitymanagement.batch.EMBatchConstants.*;
import static eu.europeana.entitymanagement.mongo.utils.MorphiaUtils.MULTI_DELETE_OPTS;
import static eu.europeana.entitymanagement.mongo.utils.MorphiaUtils.UPSERT_OPTS;

@Repository
public class ScheduledTaskRepository implements InitializingBean {

    private final Datastore datastore;

    @Autowired
    public ScheduledTaskRepository(
            @Qualifier(AppConfigConstants.BEAN_EM_DATA_STORE) Datastore datastore) {
        this.datastore = datastore;
    }


    @Override
    public void afterPropertiesSet() {
        datastore.ensureIndexes(ScheduledTask.class);
    }

    /**
     * Upserts the provided {@link ScheduledTask} into the database
     *
     * @param task scheduled task
     * @return updateResult
     */
    public UpdateResult upsert(ScheduledTask task) {
        return datastore.find(ScheduledTask.class)
                .filter(
                        eq(ENTITY_ID, task.getEntityId())
                )
                .update(
                        UpdateOperators.set(ENTITY_ID, task.getEntityId()),
                        UpdateOperators.set(MODIFIED, task.getLastModified()),
                        // set "created" value if this a new doc
                        UpdateOperators.setOnInsert(Map.of(CREATED, task.getLastModified()))
                ).execute(UPSERT_OPTS);
    }

    /**
     * Upserts the {@link ScheduledTask} list to the database
     *
     * @param tasks list of tasks
     * @return BulkWriteResult of db query
     */
    public BulkWriteResult upsertBulk(List<ScheduledTask> tasks) {
        MongoCollection<ScheduledTask> collection = datastore.getMapper()
                .getCollection(ScheduledTask.class);

        List<WriteModel<ScheduledTask>> updates = new ArrayList<>();

        for (ScheduledTask task : tasks) {
            updates.add(new UpdateOneModel<>(
                    new Document(ENTITY_ID, task.getEntityId()),
                    new Document(DOC_SET,
                            new Document(ENTITY_ID, task.getEntityId())
                                    .append(MODIFIED, task.getLastModified())
                    )
                            // set "created" if this is a new document
                            .append(DOC_SET_ON_INSERT, new Document(CREATED, task.getLastModified())),
                    UPSERT_OPTS
            ));
        }

        return collection.bulkWrite(updates);
    }

    /**
     * Deletes {@link ScheduledTask} entries in the db whose entityId is contained within
     * the provided list.
     *
     * @param entityIds entityId list
     * @return number of deleted entries
     */
    public long removeTasks(List<String> entityIds) {
        return datastore.find(ScheduledTask.class).filter(
                in(ENTITY_ID, entityIds))
                .delete(MULTI_DELETE_OPTS).getDeletedCount();
    }

    /**
     * Queries the ScheduledTasks collection for records that match the given filter(s).
     * Then retrieves the matching EntityRecords
     * Results are sorted in ascending order of created
     *
     * @param filters Query filters
     * @return List with results
     */
    public List<? extends EntityRecord> getEntityRecordsForTasks(int start, int count, Filter[] filters) {
        List<ScheduledTask> scheduledTasks = datastore.find(ScheduledTask.class)
                .filter(filters)
                .iterator(new FindOptions()
                        // we only care about the EntityID
                        .projection().include(ENTITY_ID)
                        .skip(start)
                        .sort(ascending(CREATED))
                        .limit(count)).toList();

        List<String> matchingIds = scheduledTasks.stream().map(ScheduledTask::getEntityId).collect(
                Collectors.toList());

        return datastore.find(EntityRecord.class)
                .filter(in(ENTITY_ID, matchingIds)).iterator().toList();
    }
}
