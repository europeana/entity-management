package eu.europeana.entitymanagement.batch.errorhandling;

import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.filters.Filters.in;
import static eu.europeana.entitymanagement.mongo.utils.MorphiaUtils.MULTI_DELETE_OPTS;
import static eu.europeana.entitymanagement.mongo.utils.MorphiaUtils.UPSET_OPTS;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import com.mongodb.client.result.UpdateResult;
import dev.morphia.Datastore;
import dev.morphia.query.experimental.updates.UpdateOperators;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class EntityUpdateFailureRepository {

  private final Datastore datastore;

  // Field names
  private final static String ENTITY_ID = "entityId";
  private final static String TIMESTAMP = "timestamp";
  private final static String ERROR_MSG = "errorMessage";
  private final static String STACKTRACE = "stackTrace";

  @Autowired
  public EntityUpdateFailureRepository(
      @Qualifier(AppConfigConstants.BEAN_EM_DATA_STORE) Datastore datastore) {
    this.datastore = datastore;
  }

  /**
   * Upserts the provided {@link EntityUpdateFailure} into the database
   * @param failure update failure
   * @return updateResult
   */
  public UpdateResult upsert(EntityUpdateFailure failure) {
    //
    return datastore.find(EntityUpdateFailure.class)
        .filter(
            eq(ENTITY_ID, failure.getEntityId())
        )
        .update(
            UpdateOperators.set(ENTITY_ID, failure.getEntityId()),
            UpdateOperators.set(TIMESTAMP, failure.getTimestamp()),
            UpdateOperators.set(ERROR_MSG, failure.getErrorMessage()),
            UpdateOperators.set(STACKTRACE, failure.getStackTrace())
        ).execute(UPSET_OPTS);
  }

  /**
   * Upserts the {@link EntityUpdateFailure} list to the database
   * @param failures list of failures
   * @return BulkWriteResult of db query
   */
  public BulkWriteResult upsertBulk(List<EntityUpdateFailure> failures) {
    MongoCollection<EntityUpdateFailure> collection = datastore.getMapper()
        .getCollection(EntityUpdateFailure.class);

    List<WriteModel<EntityUpdateFailure>> updates = new ArrayList<>();

    for (EntityUpdateFailure failure : failures) {
      updates.add(new UpdateOneModel<>(
          // filter
          new Document(ENTITY_ID, failure.getEntityId()),
          // update
          new Document(Map.of(
              ENTITY_ID, failure.getEntityId(),
              TIMESTAMP, failure.getTimestamp(),
              ERROR_MSG, failure.getErrorMessage(),
              STACKTRACE, failure.getStackTrace()
          )),
          // update options
          UPSET_OPTS
      ));
    }

    return collection.bulkWrite(updates);
  }

  /**
   * Deletes {@link EntityUpdateFailure} entries in the db whose entityId is contained within
   * the provided list.
   * @param entityIds entityId list
   * @return number of deleted entries
   */
  public long removeFailures(List<String> entityIds) {
    return datastore.find(EntityUpdateFailure.class).filter(
        in(ENTITY_ID, entityIds))
        .delete(MULTI_DELETE_OPTS).getDeletedCount();
  }
}
