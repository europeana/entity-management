package eu.europeana.entitymanagement.batch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;

public class BatchUtils {


  /**
   * Creates JobParameters for triggering the Spring Batch update job for specific entities
   *
   * @param entityIds string array containing entity ids
   * @param runTime   trigger time for job
   * @param mapper    JSON object mapper
   * @return JobParameters with trigger time and entityIds
   * @throws JsonProcessingException
   */
  public static JobParameters createJobParameters(String[] entityIds, Date runTime,
      ObjectMapper mapper) throws JsonProcessingException {
    JobParametersBuilder paramBuilder = new JobParametersBuilder()
        .addDate(JobParameter.RUN_TIME.key(), runTime);

    if (entityIds != null) {

      paramBuilder.addString(JobParameter.ENTITY_ID.key(),
          mapper.writeValueAsString(entityIds));
    }
    return paramBuilder.toJobParameters();
  }

}
