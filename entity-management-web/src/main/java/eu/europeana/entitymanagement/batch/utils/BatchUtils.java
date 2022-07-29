package eu.europeana.entitymanagement.batch.utils;

import eu.europeana.entitymanagement.batch.model.JobParameter;
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

public class BatchUtils {

  // Batch jobs and steps
  public static String JOB_UPDATE_SINGLE_ENTITY = "update-single-entity-job";
  public static String STEP_UPDATE_ENTITY = "update-entity-step";
  public static String STEP_REMOVE_ENTITY = "remove-entity-step";
  public static String JOB_UPDATE_SCHEDULED_ENTITIES = "update-scheduled-entities-job";
  public static String JOB_REMOVE_SCHEDULED_ENTITIES = "remove-scheduled-entities-job";

  /**
   * Creates JobParameters for triggering the Spring Batch update job for specific entities
   *
   * @param entityId entity id
   * @param runTime trigger time for job
   * @param updateType update tpe for job
   * @return JobParameters with trigger time and entityId
   */
  public static JobParameters createJobParameters(
      @Nullable String entityId,
      Date runTime,
      List<ScheduledTaskType> updateType,
      boolean isSynchronous) {
    JobParametersBuilder jobParametersBuilder =
        new JobParametersBuilder()
            .addDate(JobParameter.CURRENT_START_TIME.key(), runTime)
            .addString(
                JobParameter.UPDATE_TYPE.key(),
                updateType.stream().map(p -> p.getValue()).collect(Collectors.joining(",")))
            // boolean parameters not supported
            .addString(JobParameter.IS_SYNCHRONOUS.key(), String.valueOf(isSynchronous));

    if (StringUtils.hasLength(entityId)) {
      jobParametersBuilder.addString(JobParameter.ENTITY_ID.key(), entityId);
    }

    return jobParametersBuilder.toJobParameters();
  }

  public static String[] getEntityIds(List<BatchEntityRecord> batchEntityRecords) {
    return batchEntityRecords.stream()
        .map(p -> p.getEntityRecord().getEntityId())
        .toArray(String[]::new);
  }

  public static List<? extends BatchEntityRecord> filterRecordsForWritters(
      Set<ScheduledTaskType> supportedScheduledTasks, List<? extends BatchEntityRecord> records) {
    return records.stream()
        .filter(p -> supportedScheduledTasks.contains(p.getScheduledTaskType()))
        .collect(Collectors.toList());
  }
}
