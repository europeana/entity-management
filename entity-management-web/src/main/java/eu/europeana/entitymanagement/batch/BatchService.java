package eu.europeana.entitymanagement.batch;

import static eu.europeana.entitymanagement.batch.BatchUtils.JOB_UPDATE_ALL_ENTITIES;
import static eu.europeana.entitymanagement.batch.BatchUtils.JOB_UPDATE_SPECIFIC_ENTITIES;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.BEAN_JSON_MAPPER;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import javax.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class BatchService {

  private static final Logger logger = LogManager.getLogger(BatchService.class);

  private final BatchEntityUpdateConfig batchUpdateConfig;
  private final BatchConfigurer batchConfigurer;
  private final ObjectMapper mapper;
  private JobLauncher jobLauncher;

  private JobExplorer jobExplorer;


  @Autowired
  public BatchService(BatchEntityUpdateConfig batchUpdateConfig,
      BatchConfigurer batchConfigurer, @Qualifier(BEAN_JSON_MAPPER) ObjectMapper mapper,
      JobLauncher jobLauncher, JobExplorer jobExplorer) {
    this.batchUpdateConfig = batchUpdateConfig;
    this.batchConfigurer = batchConfigurer;
    this.mapper = mapper;
    this.jobLauncher = jobLauncher;
    this.jobExplorer = jobExplorer;
  }


  @PostConstruct
  void setup() throws Exception {
    // launcher is async, so this is non-blocking
    jobLauncher = batchConfigurer.getJobLauncher();
  }

  /**
   * Launches the update job for a single entity.
   *
   * @param entityId entityId to be used in job
   * @throws Exception on error
   */
  public void launchSingleEntityUpdate(String entityId) throws Exception {
    JobParameters jobParameters = BatchUtils
        .createJobParameters(new String[]{entityId}, new Date(), mapper);
    jobLauncher.run(batchUpdateConfig.updateSpecificEntities(), jobParameters);
  }

  /**
   * Launches the update job for multiple entities
   *
   * @throws Exception on error
   */
  public void launchMultiEntityUpdate() throws Exception {
    // first check if any failed or stopped executions exist for job

    JobParameters jobParameters;
    JobExecution jobExecution = getLastExecution(JOB_UPDATE_ALL_ENTITIES);
    if (jobExecution != null) {
      logger
          .debug("Found {} execution for job={}; jobExecutionId={}", jobExecution.getStatus(),
              JOB_UPDATE_ALL_ENTITIES, jobExecution.getId());

      if (jobExecution.getStatus().isGreaterThan(BatchStatus.STOPPING)) {
        jobParameters = jobExecution.getJobParameters();
        logger
            .info("Restarting {} job instance for job={}", jobExecution.getStatus(),
                JOB_UPDATE_ALL_ENTITIES);

        jobLauncher.run(batchUpdateConfig.updateAllEntities(), jobParameters);
        return;
      }
    }

    logger.info("No failed or stopped execution found for job={}. Creating a new instance",
        JOB_UPDATE_SPECIFIC_ENTITIES);

    jobLauncher.run(batchUpdateConfig.updateAllEntities(),
        BatchUtils.createJobParameters(null, new Date(), mapper));
  }


  private JobExecution getLastExecution(String jobIdentifier) {
    JobInstance lastJobInstance = jobExplorer.getLastJobInstance(jobIdentifier);

    if(lastJobInstance == null){
      logger.debug("No jobInstance found for job={}", jobIdentifier);
      return null;
    }

    // get the last execution of the latest instance (results already sorted by most-recent-first)
    return jobExplorer.getLastJobExecution(lastJobInstance);
  }
}
