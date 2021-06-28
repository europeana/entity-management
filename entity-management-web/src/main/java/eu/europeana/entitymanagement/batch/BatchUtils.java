package eu.europeana.entitymanagement.batch;

import eu.europeana.entitymanagement.batch.model.BatchUpdateType;
import eu.europeana.entitymanagement.batch.model.JobParameter;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;

import java.util.Date;
import java.util.List;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;

public class BatchUtils {

  // Batch jobs and steps
  public static String JOB_UPDATE_SINGLE_ENTITY = "update-single-entity-job";
  public static String STEP_UPDATE_ENTITY = "update-single-entity-step";
  public static String JOB_UPDATE_SCHEDULED_ENTITIES = "update-scheduled-entities-job";
  public static String STEP_UPDATE_SCHEDULED_ENTITIES = "update-scheduled-entities-step";


  /**
   * Creates JobParameters for triggering the Spring Batch update job for specific entities
   *
   * @param entityId entity id
   * @param runTime   trigger time for job
   * @return JobParameters with trigger time and entityId
   */
  public static JobParameters createJobParameters(String entityId, Date runTime) {
    return new JobParametersBuilder()
        .addDate(JobParameter.CURRENT_START_TIME.key(), runTime).addString(JobParameter.ENTITY_ID.key(),
          entityId).toJobParameters();
  }

  public static JobParameters createJobParameters(BatchUpdateType updateType, Date runTime) {
    return new JobParametersBuilder()
            .addDate(JobParameter.CURRENT_START_TIME.key(), runTime)
            .addString(JobParameter.UPDATE_TYPE.key(), updateType.name())
            .toJobParameters();
  }


  public static String[] getEntityIds(List<? extends EntityRecord> entityRecords) {
    return entityRecords.stream().map(EntityRecord::getEntityId)
        .toArray(String[]::new);
  }
}
