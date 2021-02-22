package eu.europeana.entitymanagement.batch.repository;

import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.ReturnDocument;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.experimental.updates.UpdateOperators;
import dev.morphia.query.internal.MorphiaCursor;
import eu.europeana.entitymanagement.batch.entity.JobExecutionEntity;
import eu.europeana.entitymanagement.batch.entity.JobInstanceEntity;
import eu.europeana.entitymanagement.batch.entity.SequenceGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static dev.morphia.query.experimental.filters.Filters.eq;
import static eu.europeana.entitymanagement.batch.BatchConstants.*;

public abstract class AbstractRepository {

    abstract Datastore getDataStore();

    /**
     * Generates an autoincrement value for entities, based on the Entity type
     *
     * @param internalType internal type for Entity
     * @return autoincrement value
     */
    protected long generateSequence(String internalType) {
        // Get the given key from the auto increment entity and try to increment it.
        SequenceGenerator nextId = getDataStore().find(SequenceGenerator.class).filter(
                eq("_id", internalType))
                .modify(UpdateOperators.inc("value"))
                .execute(new ModifyOptions()
                        .returnDocument(ReturnDocument.AFTER));


        // If none is found, we need to create one for the given key.
        if (nextId == null) {
            nextId = new SequenceGenerator(internalType, 1L);
            getDataStore().save(nextId);
        }
        return nextId.getValue();
    }

    /**
     * Get JobInstanceIds with the given job name
     *
     * @param jobName jobName
     * @return List of jobInstance Ids
     */
    protected List<Long> getJobInstanceIdsWithName(String jobName) {
        List<Long> results = new ArrayList<>();
        MorphiaCursor<JobInstanceEntity> cursor = getDataStore().find(JobInstanceEntity.class)
                .filter(eq(JOB_NAME_KEY, jobName))
                .iterator(new FindOptions()
                        .projection().include(JOB_INSTANCE_ID_KEY));

        while (cursor.hasNext()) {
            results.add(cursor.next().getJobInstanceId());
        }

        return results;
    }

    protected JobExecutionEntity getJobExecutionWithId(long jobExecutionId) {
        return getDataStore().find(JobExecutionEntity.class)
                .filter(eq(JOB_EXECUTION_ID_KEY, jobExecutionId))
                .first();
    }

    protected long getJobExecutionInstanceId(long jobExecutionId) {
        return
                getDataStore().find(JobExecutionEntity.class)
                        .filter(eq(JOB_EXECUTION_ID_KEY, jobExecutionId))
                        .iterator(new FindOptions()
                                .projection().include(JOB_INSTANCE_ID_KEY)
                                .limit(1))
                        .next().getJobInstanceId();
    }


    /**
     * Gets distinct values for a collection property
     *
     * @param clazz     entity class
     * @param fieldName name of field
     * @return List containing distinct values
     */
    protected List<String> queryDistinctStringValues(final Class<?> clazz, final String fieldName) {
        DistinctIterable<String> iterable = getDataStore().getMapper().getCollection(clazz).distinct(fieldName, String.class);

        MongoCursor<String> cursor = iterable.iterator();
        List<String> result = new ArrayList<>();
        while (cursor.hasNext()) {
            result.add(cursor.next());
        }
        return result;
    }
}
