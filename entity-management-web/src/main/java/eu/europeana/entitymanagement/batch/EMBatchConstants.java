package eu.europeana.entitymanagement.batch;

import eu.europeana.entitymanagement.batch.model.FailedTask;
import eu.europeana.entitymanagement.batch.model.ScheduledTask;

public class EMBatchConstants {

    // Document ops
    public final static String DOC_SET = "$set";
    public final static String DOC_SET_ON_INSERT = "$setOnInsert";


    // Common field names
    public final static String ENTITY_ID = "entityId";
    public final static String CREATED = "created";
    public final static String MODIFIED = "modified";

    // Error handling field-specific fields
    public final static String ERROR_MSG = "errorMessage";
    public final static String STACKTRACE = "stackTrace";

    // ScheduledTask
    public final static String UPDATE_TYPE = "updateType";
    public final static String HAS_BEEN_PROCESSED = "hasBeenProcessed";

    public final static String SCHEDULED_TASK_CLASSNAME = ScheduledTask.class.getSimpleName();
    public final static String FAILED_TASK_CLASSNAME = FailedTask.class.getSimpleName();

    public final static String MORPHIA_DISCRIMINATOR = "_t";

}
