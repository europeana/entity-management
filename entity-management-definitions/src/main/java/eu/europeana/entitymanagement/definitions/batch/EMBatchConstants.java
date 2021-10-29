package eu.europeana.entitymanagement.definitions.batch;

import eu.europeana.entitymanagement.definitions.batch.model.FailedTask;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTask;

public class EMBatchConstants {

  // Document ops
  public static final String DOC_SET = "$set";
  public static final String DOC_SET_ON_INSERT = "$setOnInsert";
  public static final String DOC_INCREMENT = "$inc";

  // Common field names
  public static final String ENTITY_ID = "entityId";
  public static final String CREATED = "created";
  public static final String MODIFIED = "modified";

  // Error handling field-specific fields
  public static final String ERROR_MSG = "errorMessage";
  public static final String STACKTRACE = "stackTrace";
  public static final String FAILURE_COUNT = "failureCount";

  // ScheduledTask
  public static final String UPDATE_TYPE = "updateType";
  public static final String HAS_BEEN_PROCESSED = "hasBeenProcessed";

  public static final String SCHEDULED_TASK_CLASSNAME = ScheduledTask.class.getSimpleName();
  public static final String FAILED_TASK_CLASSNAME = FailedTask.class.getSimpleName();

  public static final String MORPHIA_DISCRIMINATOR = "_t";
}
