package eu.europeana.entitymanagement.batch.repository;

import static dev.morphia.query.Sort.ascending;
import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.filters.Filters.in;
import static eu.europeana.entitymanagement.definitions.batch.EMBatchConstants.*;
import static eu.europeana.entitymanagement.mongo.utils.MorphiaUtils.MULTI_DELETE_OPTS;
import static eu.europeana.entitymanagement.mongo.utils.MorphiaUtils.UPSERT_OPTS;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import com.mongodb.client.result.UpdateResult;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;
import dev.morphia.query.experimental.updates.UpdateOperators;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.batch.model.FailedTask;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bson.Document;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class FailedTaskRepository implements InitializingBean {

  private final Datastore datastore;

  @Autowired
  public FailedTaskRepository(
      @Qualifier(AppConfigConstants.BEAN_EM_DATA_STORE) Datastore datastore) {
    this.datastore = datastore;
  }

  /** Morphia doesn't map the indexes on {@link FailedTask}. Explicitly set this up here */
  @Override
  public void afterPropertiesSet() {
    datastore.ensureIndexes(FailedTask.class);
  }

  /**
   * Upserts the provided {@link FailedTask} into the database
   *
   * @param failure update failure
   * @return updateResult
   */
  public UpdateResult upsert(FailedTask failure) {
    //
    return datastore
        .find(FailedTask.class)
        .filter(eq(ENTITY_ID, failure.getEntityId()))
        .update(
            UpdateOperators.set(ENTITY_ID, failure.getEntityId()),
            UpdateOperators.set(STACKTRACE, failure.getStackTrace()),
            UpdateOperators.set(ERROR_MSG, failure.getErrorMessage()),
            UpdateOperators.set(MODIFIED, failure.getModified()),
            UpdateOperators.set(UPDATE_TYPE, failure.getUpdateType().getValue()),
            // increment failureCount
            UpdateOperators.inc(FAILURE_COUNT, 1),
            // set "created" value if this a new doc. Also make failureCount=0
            UpdateOperators.setOnInsert(Map.of(CREATED, failure.getModified())))
        .execute(UPSERT_OPTS);
  }

  /**
   * Upserts the {@link FailedTask} list to the database
   *
   * @param failures list of failures
   * @return BulkWriteResult of db query
   */
  public BulkWriteResult upsertBulk(List<FailedTask> failures) {
    MongoCollection<FailedTask> collection = datastore.getMapper().getCollection(FailedTask.class);

    List<WriteModel<FailedTask>> updates = new ArrayList<>();

    for (FailedTask failure : failures) {
      updates.add(
          new UpdateOneModel<>(
              new Document(ENTITY_ID, failure.getEntityId()),
              new Document(
                      DOC_SET,
                      new Document(ENTITY_ID, failure.getEntityId())
                          .append(MODIFIED, failure.getModified())
                          .append(ERROR_MSG, failure.getErrorMessage())
                          .append(STACKTRACE, failure.getStackTrace()))
                  // increment failureCount by 1
                  .append(DOC_INCREMENT, new Document(FAILURE_COUNT, 1))
                  // set "created" if this is a new document
                  .append(
                      DOC_SET_ON_INSERT,
                      new Document(
                          Map.of(
                              CREATED,
                              failure.getModified(),
                              // add discriminator manually as we're bypassing Morphia for this
                              // query
                              MORPHIA_DISCRIMINATOR,
                              FAILED_TASK_CLASSNAME))),
              UPSERT_OPTS));
    }

    return collection.bulkWrite(updates);
  }

  /**
   * Deletes {@link FailedTask} entries in the db whose entityId is contained within the provided
   * list.
   *
   * @param entityIds entityId list
   * @return number of deleted entries
   */
  public long removeFailures(List<String> entityIds) {
    return datastore
        .find(FailedTask.class)
        .filter(in(ENTITY_ID, entityIds))
        .delete(MULTI_DELETE_OPTS)
        .getDeletedCount();
  }

  /**
   * Queries the FailedTasks collection and retrieves the matching entity ids that still exist in
   * the EntityRecord db, sorted in the ascending order of created.
   *
   * @param filters Query filters
   * @return List with results
   */
  public List<String> getEntityRecordsIdsForFailures(int start, int count) {
    List<FailedTask> failedTasks =
        datastore
            .find(FailedTask.class)
            .iterator(
                new FindOptions()
                    // we only care about the EntityID
                    .projection()
                    .include(ENTITY_ID)
                    .skip(start)
                    .sort(ascending(CREATED))
                    .limit(count))
            .toList();
    List<String> matchingIds =
        failedTasks.stream().map(FailedTask::getEntityId).collect(Collectors.toList());

    // then fetch matching EntityRecordsIds from the EntityRecord db
    // TODO: see if this can be done with a single query
    List<EntityRecord> entityRecordsForFailedTasks =
        datastore
            .find(EntityRecord.class)
            .filter(in(ENTITY_ID, matchingIds))
            .iterator(
                new FindOptions()
                    // we only care about the EntityID
                    .projection()
                    .include(ENTITY_ID))
            .toList();
    return entityRecordsForFailedTasks.stream()
        .map(EntityRecord::getEntityId)
        .collect(Collectors.toList());
  }

  public FailedTask getFailure(String entityId) {
    return datastore.find(FailedTask.class).filter(eq(ENTITY_ID, entityId)).first();
  }

  public List<FailedTask> getFailures(List<String> entityIds) {
    return datastore.find(FailedTask.class).filter(in(ENTITY_ID, entityIds)).iterator().toList();
  }

  /** Drops the FailedTask collection */
  public void dropCollection() {
    datastore.getMapper().getCollection(FailedTask.class).drop();
  }
}
