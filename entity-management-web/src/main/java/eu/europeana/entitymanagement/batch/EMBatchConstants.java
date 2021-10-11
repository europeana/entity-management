package eu.europeana.entitymanagement.batch;

import eu.europeana.entitymanagement.batch.model.FailedTask;
import eu.europeana.entitymanagement.batch.model.ScheduledTask;

public class EMBatchConstants {

  // Document ops
  public static final String DOC_SET = "$set";
  public static final String DOC_SET_ON_INSERT = "$setOnInsert";

  // Common field names
  public static final String ENTITY_ID = "entityId";
  public static final String CREATED = "created";
  public static final String MODIFIED = "modified";

  // Error handling field-specific fields
  public static final String ERROR_MSG = "errorMessage";
  public static final String STACKTRACE = "stackTrace";

  // ScheduledTask
  public static final String UPDATE_TYPE = "updateType";
  public static final String HAS_BEEN_PROCESSED = "hasBeenProcessed";

  public static final String SCHEDULED_TASK_CLASSNAME = ScheduledTask.class.getSimpleName();
  public static final String FAILED_TASK_CLASSNAME = FailedTask.class.getSimpleName();

  public static final String MORPHIA_DISCRIMINATOR = "_t";
}
