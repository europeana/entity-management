package eu.europeana.entitymanagement.batch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import java.util.Date;
import java.util.List;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.lang.Nullable;

public class BatchUtils {

  // Batch jobs and steps
  public static String JOB_UPDATE_ALL_ENTITIES = "update-all-entities-job";
  public static String JOB_UPDATE_SPECIFIC_ENTITIES = "update-specific-entities-job";
  public static String JOB_UPDATE_METRICS_SPECIFIC_ENTITIES = "update-metrics-specific-entities-job";
  public static String JOB_RETRY_FAILED_ENTITIES = "retry-failed-entities-job";
  public static String STEP_UPDATE_ENTITY = "update-entity-step";
  public static String STEP_RETRY_FAILED_ENTITIES = "retry-failed-entities-step";


  /**
   * Creates JobParameters for triggering the Spring Batch update job for specific entities
   *
   * @param entityId string array containing entity ids
   * @param runTime   trigger time for job
   * @return JobParameters with trigger time and entityId
   */
  public static JobParameters createJobParameters(String entityId, Date runTime) {
    return new JobParametersBuilder()
        .addDate(JobParameter.CURRENT_START_TIME.key(), runTime).addString(JobParameter.ENTITY_ID.key(),
          entityId).toJobParameters();
  }


  public static String[] getEntityIds(List<? extends EntityRecord> entityRecords) {
    return entityRecords.stream().map(EntityRecord::getEntityId)
        .toArray(String[]::new);
  }
}
