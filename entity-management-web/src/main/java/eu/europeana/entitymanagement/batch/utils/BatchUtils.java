package eu.europeana.entitymanagement.batch.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import eu.europeana.entitymanagement.batch.model.EntityUpdateStats;
import eu.europeana.entitymanagement.batch.model.JobParameter;
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.definitions.batch.model.TaskType;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

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
   * @param taskType of task for the job
   * @return JobParameters with trigger time and entityId
   */
  public static JobParameters createJobParameters(
          @Nullable String entityId,
          Date runTime,
          TaskType taskType,
          boolean isSynchronous) {
    JobParametersBuilder jobParametersBuilder =
            new JobParametersBuilder()
                    .addDate(JobParameter.CURRENT_START_TIME.key(), runTime)
                    .addString(
                            JobParameter.UPDATE_TYPE.key(),
                            taskType.getValue())
                    // boolean parameters not supported
                    .addString(JobParameter.IS_SYNCHRONOUS.key(), String.valueOf(isSynchronous));

    if (StringUtils.hasLength(entityId)) {
      jobParametersBuilder.addString(JobParameter.ENTITY_ID.key(), entityId);
    }

    return jobParametersBuilder.toJobParameters();
  }

  public static List<String> getEntityIds(List<BatchEntityRecord> batchEntityRecords, @NonNull TaskType taskType) {
    return batchEntityRecords.stream()
        .filter(br -> (taskType == null || taskType == br.getScheduledTaskType()))
        .map(br -> br.getEntityRecord().getEntityId())
        .toList();
  }
    
  public static List<String> getZohoUrls(List<? extends BatchEntityRecord> batchEntityRecords, String zohoBaseUrl) {
    //only organizations
    List<Entity> orgs =  batchEntityRecords.stream()
        .filter(p -> EntityTypes.isOrganizationType(p.getEntityRecord().getEntity().getType()))
        .map(p -> p.getEntityRecord().getEntity()).toList();
    
    if(orgs.isEmpty()) {
      return Collections.emptyList();
    }
    
    List<String> zohoUrls = new ArrayList<>();
    for (Entity org : orgs) {
      zohoUrls.addAll(
          org.getSameReferenceLinks().stream().filter(sa -> sa.startsWith(zohoBaseUrl)).toList());
    }
    return zohoUrls;
  }

  public static List<String> filterRecordsForWriters(List<? extends BatchEntityRecord> records) {
    return records.stream()
//        .filter(p -> supportedScheduledTasks.contains(p.getScheduledTaskType()))
        .map(r -> r.getEntityRecord().getEntityId())
        .collect(Collectors.toList());
  }
  
  /**
   * Select the stats for the given taskType
   * @param taskType the type assigned to the current job execution
   * @param entityUpdateStats the stats for entity updates execution
   * @param metricsUpdateStats the stats for metrics update execution
   * @return the selected stats object
   */
  public static EntityUpdateStats selectStats(TaskType taskType, EntityUpdateStats entityUpdateStats, EntityUpdateStats metricsUpdateStats) {
    switch (taskType) {
      case full_update: 
        return entityUpdateStats;
      case metrics_update:
        return metricsUpdateStats;
      default:
        throw new IllegalArgumentException("Unexpected value: " + taskType);
    }
  }
}
