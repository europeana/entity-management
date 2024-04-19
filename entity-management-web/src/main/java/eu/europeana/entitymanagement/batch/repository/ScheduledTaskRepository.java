package eu.europeana.entitymanagement.batch.repository;

import static dev.morphia.query.Sort.ascending;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.filters.Filters.gte;
import static dev.morphia.query.filters.Filters.in;
import static dev.morphia.query.filters.Filters.or;
import static eu.europeana.entitymanagement.definitions.batch.EMBatchConstants.CREATED;
import static eu.europeana.entitymanagement.definitions.batch.EMBatchConstants.DOC_SET;
import static eu.europeana.entitymanagement.definitions.batch.EMBatchConstants.DOC_SET_ON_INSERT;
import static eu.europeana.entitymanagement.definitions.batch.EMBatchConstants.ENTITY_ID;
import static eu.europeana.entitymanagement.definitions.batch.EMBatchConstants.HAS_BEEN_PROCESSED;
import static eu.europeana.entitymanagement.definitions.batch.EMBatchConstants.MODIFIED;
import static eu.europeana.entitymanagement.definitions.batch.EMBatchConstants.MORPHIA_DISCRIMINATOR;
import static eu.europeana.entitymanagement.definitions.batch.EMBatchConstants.SCHEDULED_TASK_CLASSNAME;
import static eu.europeana.entitymanagement.definitions.batch.EMBatchConstants.UPDATE_TYPE;
import static eu.europeana.entitymanagement.mongo.utils.MorphiaUtils.MULTI_DELETE_OPTS;
import static eu.europeana.entitymanagement.mongo.utils.MorphiaUtils.UPSERT_OPTS;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bson.Document;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import dev.morphia.Datastore;
import dev.morphia.aggregation.stages.Lookup;
import dev.morphia.aggregation.stages.Projection;
import dev.morphia.aggregation.stages.Unwind;
import dev.morphia.query.FindOptions;
import dev.morphia.query.MorphiaCursor;
import dev.morphia.query.filters.Filter;
import eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.batch.model.FailedTask;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTask;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledUpdateType;

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
    //SG: TODO should be created by the datastore, need to check
    //datastore.ensureIndexes(ScheduledTask.class);
  }

  /**
   * Marks the given tasks as "processed". Update only occurs if a task matches the specified query
   * filter document.
   *
   * @param tasks list of scheduled tasks
   * @return BulkWriteResult of db query
   */
  public BulkWriteResult markAsProcessed(@NonNull List<ScheduledTask> tasks) {
    List<WriteModel<ScheduledTask>> updates = new ArrayList<>(tasks.size());
    for (ScheduledTask task : tasks) {
      updates.add(new UpdateOneModel<>(
          // query filters on updateType
          new Document(ENTITY_ID, task.getEntityId()).append(UPDATE_TYPE,
              task.getUpdateType().getValue()),
          new Document(DOC_SET, new Document(HAS_BEEN_PROCESSED, task.hasBeenProcessed())
              .append(MODIFIED, task.getModified()))));
    }
    return datastore.getCollection(ScheduledTask.class).bulkWrite(updates);
  }

  /**
   * Upserts the {@link ScheduledTask} list to the database
   *
   * @param tasks list of tasks
   * @return BulkWriteResult of db query
   */
  public BulkWriteResult upsertBulk(@NonNull List<ScheduledTask> tasks) {
    MongoCollection<ScheduledTask> collection =
        datastore.getCollection(ScheduledTask.class);

    List<WriteModel<ScheduledTask>> updates = new ArrayList<>(tasks.size());

    for (ScheduledTask task : tasks) {
      Document updateDoc = new Document(ENTITY_ID, task.getEntityId())
          .append(HAS_BEEN_PROCESSED, task.hasBeenProcessed()).append(MODIFIED, task.getModified());

      Document setOnInsertDoc = new Document(CREATED, task.getModified())
          // manually set Morphia discriminator as we're bypassing its API for this query
          .append(MORPHIA_DISCRIMINATOR, SCHEDULED_TASK_CLASSNAME);

      boolean shouldChangeUpdateType = task.getUpdateType() == ScheduledUpdateType.FULL_UPDATE;
      /*
       * If entity is being scheduled for a full update, this: - changes the current updateType from
       * METRICS to FULL; or - leaves current updateType as FULL (no change) otherwise
       */
      if (shouldChangeUpdateType) {
        updateDoc.append(UPDATE_TYPE, task.getUpdateType().getValue());
      } else {
        // only include updateType in $setOnInsert if it isn't added in $set
        setOnInsertDoc.append(UPDATE_TYPE, task.getUpdateType().getValue());
      }

      updates.add(new UpdateOneModel<>(new Document(ENTITY_ID, task.getEntityId()),
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
   * @param updateType update types to filter on
   * @return number of deleted entries
   */
  public long removeProcessedTasks(List<? extends ScheduledTaskType> updateType) {
    return datastore.find(ScheduledTask.class)
        .filter(eq(HAS_BEEN_PROCESSED, Boolean.TRUE),
            or(updateType.stream().map(u -> eq(UPDATE_TYPE, u.getValue())).toArray(Filter[]::new)))
        .delete(MULTI_DELETE_OPTS).getDeletedCount();
  }
  
  /**
   * Delete scheduled tasks associated to the given entity
   * @param entityId the ID of the entity for which to delete the scheduled tasks
   */
  public void deleteScheduledTasks(String entityId) {
    datastore.find(ScheduledTask.class).filter(eq(ENTITY_ID, entityId)).delete();
  }

  /**
   * Queries the ScheduledTasks collection to retrieve a page of results
   *
   * @param start - the start index for the results page
   * @param limit - the size of the current results page
   * @param filters Query filters to match the results
   * @return List with results
   */
  public List<ScheduledTask> getTasks(int start, int limit, Filter[] filters) {
    return datastore.find(ScheduledTask.class).filter(filters).iterator(new FindOptions()
        // we only care about the EntityID and update type
        .projection().include(ENTITY_ID, UPDATE_TYPE).skip(start)
        // matches the index sort order defined in ScheduledTask
        // sort with _id in case of multiple matching created values
        .sort(ascending(CREATED), ascending(ENTITY_ID)).limit(limit)).toList();

  }

  /**
   * Retrieve the first scheduled task for the given entity
   * @param entityId the id of the entity
   * @return the first task found in the database
   */
  public ScheduledTask getTask(String entityId) {
    return datastore.find(ScheduledTask.class).filter(eq(ENTITY_ID, entityId)).first();
  }

  /**
   * searches the database for scheduled task for which the processing is not complete
   * 
   * @return The number of scheduled task which are not marked as processed
   */
  public long getRuningTasksCount() {
    return datastore.find(ScheduledTask.class).filter(eq(HAS_BEEN_PROCESSED, Boolean.FALSE))
        .count();
  }

  public List<ScheduledTask> getTasks(List<String> entityIds) {
    return datastore.find(ScheduledTask.class).filter(in(ENTITY_ID, entityIds)).iterator().toList();
  }

  public void dropCollection() {
    datastore.getCollection(ScheduledTask.class).drop();
  }

  /**
   * Gets all ScheduledTasks with failures, whose failureCount is above the maxFailedTaskRetries
   * value. Note: this method returns a cursor, which callers are responsible for closing
   * @param maxFailedTaskRetries maximum number of failed tasks to be used as filter
   * @param updateType a list of scheduled tasks types to be used for filtering
   * @return the database cursor to access scheduled tasks
   */
  public MorphiaCursor<ScheduledTask> getTasksWithFailures(int maxFailedTaskRetries,
      List<? extends ScheduledTaskType> updateType) {
    return datastore.aggregate(ScheduledTask.class)
        .match(eq(HAS_BEEN_PROCESSED, Boolean.FALSE),
            in(UPDATE_TYPE,
                updateType.stream().map(ScheduledTaskType::getValue).collect(Collectors.toList())))
        // both collections use the same entityId field name
        .lookup(Lookup.from(FailedTask.class).localField(ENTITY_ID).foreignField(ENTITY_ID)
            .as("failed_tasks_lookup"))
        .unwind(Unwind.on("failed_tasks_lookup"))
        .match(gte("failed_tasks_lookup.failureCount", maxFailedTaskRetries))
        // we only care about the entityId for this query
        .project(Projection.of().include(ENTITY_ID)).execute(ScheduledTask.class);
  }
}
