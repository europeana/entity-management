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

  public static String JOB_UPDATE_ALL_ENTITIES = "allEntityUpdateJob";
  public static String JOB_UPDATE_SPECIFIC_ENTITIES = "specificEntityUpdateJob";

  public static String STEP_UPDATE_ENTITY = "updateEntityStep";

  /**
   * Creates JobParameters for triggering the Spring Batch update job for specific entities
   *
   * @param entityIds string array containing entity ids
   * @param runTime   trigger time for job
   * @param mapper    JSON object mapper
   * @return JobParameters with trigger time and entityIds
   * @throws JsonProcessingException
   */
  public static JobParameters createJobParameters(@Nullable String[] entityIds, Date runTime,
      ObjectMapper mapper) throws JsonProcessingException {
    JobParametersBuilder paramBuilder = new JobParametersBuilder()
        .addDate(JobParameter.CURRENT_START_TIME.key(), runTime);

    if (entityIds != null) {

      paramBuilder.addString(JobParameter.ENTITY_ID.key(),
          mapper.writeValueAsString(entityIds));
    }
    return paramBuilder.toJobParameters();
  }


  public static String[] getEntityIds(List<? extends EntityRecord> entityRecords) {
    return entityRecords.stream().map(EntityRecord::getEntityId)
        .toArray(String[]::new);
  }
}
