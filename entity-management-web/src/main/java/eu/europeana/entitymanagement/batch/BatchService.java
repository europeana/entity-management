package eu.europeana.entitymanagement.batch;

import static eu.europeana.entitymanagement.batch.BatchUtils.JOB_UPDATE_ALL_ENTITIES;
import static eu.europeana.entitymanagement.batch.BatchUtils.JOB_UPDATE_SPECIFIC_ENTITIES;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.BEAN_JSON_MAPPER;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.DEFAULT_JOB_LAUNCHER;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.SYNC_JOB_LAUNCHER;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
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
  private final ObjectMapper mapper;

  private final JobLauncher defaultJobLauncher;
  private final JobLauncher synchronousJobLauncher;

  private final JobExplorer jobExplorer;


  @Autowired
  public BatchService(BatchEntityUpdateConfig batchUpdateConfig,
      @Qualifier(BEAN_JSON_MAPPER) ObjectMapper mapper,
      @Qualifier(DEFAULT_JOB_LAUNCHER) JobLauncher defaultJobLauncher,
      @Qualifier(SYNC_JOB_LAUNCHER) JobLauncher synchronousJobLauncher,
      JobExplorer jobExplorer) {
    this.batchUpdateConfig = batchUpdateConfig;
    this.mapper = mapper;
    this.defaultJobLauncher = defaultJobLauncher;
    this.synchronousJobLauncher = synchronousJobLauncher;
    this.jobExplorer = jobExplorer;
  }


  /**
   * Launches the update job for a single entity.
   *
   * @param entityId          entityId to be used in job
   * @param runAsynchronously indicates whether this job should be run asynchronously or not
   * @throws Exception on error
   */
  public void launchSingleEntityUpdate(String entityId, boolean runAsynchronously)
      throws Exception {
    JobLauncher launcher = runAsynchronously ? defaultJobLauncher : synchronousJobLauncher;

    JobParameters jobParameters = BatchUtils
        .createJobParameters(new String[]{entityId}, new Date(), mapper);
    launcher.run(batchUpdateConfig.updateSpecificEntities(), jobParameters);
  }

  /**
   * Launches the update job for multiple entities. Runs asynchronously.
   *
   * @throws Exception on error
   */
  public void launchMultiEntityUpdate() throws Exception {
    // first check if any failed or stopped executions exist for job

    JobParameters jobParameters;
    JobExecution jobExecution = getLastExecution(JOB_UPDATE_ALL_ENTITIES);
    if (jobExecution != null) {
      logger
          .debug("Found {} execution for jobId={}; jobExecutionId={}", jobExecution.getStatus(),
              JOB_UPDATE_ALL_ENTITIES, jobExecution.getId());

      if (jobExecution.getStatus().isGreaterThan(BatchStatus.STOPPING)) {
        jobParameters = jobExecution.getJobParameters();
        logger
            .info("Restarting {} job instance for jobName={}", jobExecution.getStatus(),
                JOB_UPDATE_ALL_ENTITIES);

        defaultJobLauncher.run(batchUpdateConfig.updateAllEntities(), jobParameters);
        return;
      }
    }

    logger.info("No failed or stopped execution found for jobName={}. Creating a new instance",
        JOB_UPDATE_SPECIFIC_ENTITIES);

    defaultJobLauncher.run(batchUpdateConfig.updateAllEntities(),
        BatchUtils.createJobParameters(null, new Date(), mapper));
  }


  private JobExecution getLastExecution(String jobIdentifier) {
    JobInstance lastJobInstance = jobExplorer.getLastJobInstance(jobIdentifier);

    if (lastJobInstance == null) {
      logger.debug("No jobInstance found for jobId={}", jobIdentifier);
      return null;
    }

    // get the last execution of the latest instance (results already sorted by most-recent-first)
    return jobExplorer.getLastJobExecution(lastJobInstance);
  }
}
