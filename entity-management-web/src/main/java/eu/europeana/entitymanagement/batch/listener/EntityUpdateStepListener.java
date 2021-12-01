package eu.europeana.entitymanagement.batch.listener;

import eu.europeana.entitymanagement.batch.service.ScheduledTaskService;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.lang.NonNull;

/** Listens for Step execution lifecycle events. */
public class EntityUpdateStepListener implements StepExecutionListener {

  private static final Logger logger = LogManager.getLogger(EntityUpdateStepListener.class);

  private final ScheduledTaskService scheduledTaskService;
  private final ScheduledTaskType updateType;

  private final int maxFailedTaskRetries;

  public EntityUpdateStepListener(
      ScheduledTaskService scheduledTaskService,
      ScheduledTaskType updateType,
      int maxFailedTaskRetries) {
    this.scheduledTaskService = scheduledTaskService;
    this.updateType = updateType;

    this.maxFailedTaskRetries = maxFailedTaskRetries;
  }

  /**
   * Cleanup processed tasks before every update run, in case job was terminated before completion
   */
  @Override
  public void beforeStep(@NonNull StepExecution stepExecution) {
    logger.debug("Cleaning up processed tasks before step execution. updateType={}", updateType);
    // remove processed tasks here, in case application restarted before step finished execution
    scheduledTaskService.removeProcessedTasks(updateType);
  }

  @Override
  public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
    /*
     * By default, we always retry ScheduledTasks with failures (hasBeenProcessed=false)
     *
     * Tasks with failuresCount >= maxFailedTaskRetries are removed here, instead of the beforeStep, as we still want to process
     * them if they're manually scheduled again.
     */

    scheduledTaskService.removeScheduledTasksWithFailures(maxFailedTaskRetries, updateType);
    return ExitStatus.COMPLETED;
  }
}
