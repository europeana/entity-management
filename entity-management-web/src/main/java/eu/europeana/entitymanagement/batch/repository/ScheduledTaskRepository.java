package eu.europeana.entitymanagement.batch.repository;

import static dev.morphia.query.Sort.ascending;
import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.filters.Filters.gte;
import static dev.morphia.query.experimental.filters.Filters.in;
import static eu.europeana.entitymanagement.definitions.batch.EMBatchConstants.*;
import static eu.europeana.entitymanagement.mongo.utils.MorphiaUtils.MULTI_DELETE_OPTS;
import static eu.europeana.entitymanagement.mongo.utils.MorphiaUtils.UPSERT_OPTS;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import dev.morphia.Datastore;
import dev.morphia.aggregation.experimental.stages.Lookup;
import dev.morphia.aggregation.experimental.stages.Projection;
import dev.morphia.aggregation.experimental.stages.Unwind;
import dev.morphia.query.FindOptions;
import dev.morphia.query.experimental.filters.Filter;
import dev.morphia.query.internal.MorphiaCursor;
import eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.batch.model.FailedTask;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTask;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledUpdateType;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bson.Document;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

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
   * Marks the given tasks as "processed". Update only occurs if current updateType for a task
   * matches the specified updateType.
   *
   * @param updateType batch update type
   * @param tasks list of scheduled tasks
   * @return BulkWriteResult of db query
   */
  public BulkWriteResult markAsProcessed(ScheduledTaskType updateType, List<ScheduledTask> tasks) {
    List<WriteModel<ScheduledTask>> updates = new ArrayList<>();
    for (ScheduledTask task : tasks) {
      updates.add(
          new UpdateOneModel<>(
              // query filters on updateType
              new Document(ENTITY_ID, task.getEntityId())
                  .append(UPDATE_TYPE, updateType.getValue()),
              new Document(
                  DOC_SET,
                  new Document(HAS_BEEN_PROCESSED, task.hasBeenProcessed())
                      .append(MODIFIED, task.getModified()))));
    }
    return datastore.getMapper().getCollection(ScheduledTask.class).bulkWrite(updates);
  }

  /**
   * Upserts the {@link ScheduledTask} list to the database
   *
   * @param tasks list of tasks
   * @return BulkWriteResult of db query
   */
  public BulkWriteResult upsertBulk(List<ScheduledTask> tasks) {
    MongoCollection<ScheduledTask> collection =
        datastore.getMapper().getCollection(ScheduledTask.class);

    List<WriteModel<ScheduledTask>> updates = new ArrayList<>();

    for (ScheduledTask task : tasks) {
      Document updateDoc =
          new Document(ENTITY_ID, task.getEntityId())
              .append(HAS_BEEN_PROCESSED, task.hasBeenProcessed())
              .append(MODIFIED, task.getModified());

      Document setOnInsertDoc =
          new Document(CREATED, task.getModified())
              // manually set Morphia discriminator as we're bypassing its API for this query
              .append(MORPHIA_DISCRIMINATOR, SCHEDULED_TASK_CLASSNAME);

      boolean shouldChangeUpdateType = task.getUpdateType() == ScheduledUpdateType.FULL_UPDATE;
      /*
       * If entity is being scheduled for a full update, this:
       *  - changes the current updateType from METRICS to FULL; or
       *  - leaves current updateType as FULL (no change) otherwise
       */
      if (shouldChangeUpdateType) {
        updateDoc.append(UPDATE_TYPE, task.getUpdateType().getValue());
      } else {
        // only include updateType in $setOnInsert if it isn't added in $set
        setOnInsertDoc.append(UPDATE_TYPE, task.getUpdateType().getValue());
      }

      updates.add(
          new UpdateOneModel<>(
              new Document(ENTITY_ID, task.getEntityId()),
              new Document(DOC_SET, updateDoc)
                  // set "created" and updateType if this is a new document
                  .append(DOC_SET_ON_INSERT, setOnInsertDoc),
              UPSERT_OPTS));
    }

    return collection.bulkWrite(updates);
  }

  /**
   * Deletes {@link ScheduledTask} entries in the db that have been processed
   *
   * @param updateType updateType to filter on
   * @return number of deleted entries
   */
  public long removeProcessedTasks(ScheduledTaskType updateType) {
    return datastore
        .find(ScheduledTask.class)
        .filter(eq(HAS_BEEN_PROCESSED, true), eq(UPDATE_TYPE, updateType.getValue()))
        .delete(MULTI_DELETE_OPTS)
        .getDeletedCount();
  }

  public void deleteScheduledTask(String entityId) {
    datastore.find(ScheduledTask.class).filter(eq(ENTITY_ID, entityId)).delete();
  }

  /**
   * Queries the ScheduledTasks collection for records that match the given filter(s). Then
   * retrieves the matching EntityRecords. Results are sorted in ascending order of entityId.
   *
   * @param filters Query filters
   * @return List with results
   */
  public List<? extends EntityRecord> getEntityRecordsForTasks(
      int start, int count, Filter[] filters) {
    List<ScheduledTask> scheduledTasks =
        datastore
            .find(ScheduledTask.class)
            .filter(filters)
            .iterator(
                new FindOptions()
                    // we only care about the EntityID
                    .projection()
                    .include(ENTITY_ID)
                    .skip(start)
                    // matches the index sort order defined in ScheduledTask
                    .sort(ascending(CREATED))
                    .limit(count))
            .toList();

    List<String> matchingIds =
        scheduledTasks.stream().map(ScheduledTask::getEntityId).collect(Collectors.toList());

    return datastore
        .find(EntityRecord.class)
        .filter(in(ENTITY_ID, matchingIds))
        .iterator()
        .toList();
  }

  public ScheduledTask getTask(String entityId) {
    return datastore.find(ScheduledTask.class).filter(eq(ENTITY_ID, entityId)).first();
  }

  public List<ScheduledTask> getTasks(List<String> entityIds) {
    return datastore.find(ScheduledTask.class).filter(in(ENTITY_ID, entityIds)).iterator().toList();
  }

  public void dropCollection() {
    datastore.getMapper().getCollection(ScheduledTask.class).drop();
  }

  /**
   * Gets all ScheduledTasks with failures, whose failureCount is above the maxFailedTaskRetries
   * value. Note: this method returns a cursor, which callers are responsible for closing
   */
  public MorphiaCursor<ScheduledTask> getTasksWithFailures(
      int maxFailedTaskRetries, ScheduledTaskType updateType) {

    return datastore
        .aggregate(ScheduledTask.class)
        .match(eq(HAS_BEEN_PROCESSED, false), eq(UPDATE_TYPE, updateType.getValue()))
        // both collections use the same entityId field name
        .lookup(
            Lookup.from(FailedTask.class)
                .localField(ENTITY_ID)
                .foreignField(ENTITY_ID)
                .as("failed_tasks_lookup"))
        .unwind(Unwind.on("failed_tasks_lookup"))
        .match(gte("failed_tasks_lookup.failureCount", maxFailedTaskRetries))
        // we only care about the entityId for this query
        .project(Projection.of().include(ENTITY_ID))
        .execute(ScheduledTask.class);
  }
}
