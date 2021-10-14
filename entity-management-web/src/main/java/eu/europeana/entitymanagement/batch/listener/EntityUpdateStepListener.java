package eu.europeana.entitymanagement.batch.listener;

import eu.europeana.entitymanagement.batch.model.ScheduledTaskType;
import eu.europeana.entitymanagement.batch.service.ScheduledTaskService;
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

  public EntityUpdateStepListener(
      ScheduledTaskService scheduledTaskService, ScheduledTaskType updateType) {
    this.scheduledTaskService = scheduledTaskService;
    this.updateType = updateType;
  }

  /**
   * Cleanup processed tasks before every update run, in case job was terminated before completion
   */
  @Override
  public void beforeStep(@NonNull StepExecution stepExecution) {
    logger.debug("Cleaning up processed tasks before step execution. updateType={}", updateType);
    cleanupProcessedTasks();
  }

  @Override
  public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
    logger.debug("Cleaning up processed tasks after step execution. updateType={}", updateType);
    cleanupProcessedTasks();
    return ExitStatus.COMPLETED;
  }

  private void cleanupProcessedTasks() {
    scheduledTaskService.removeProcessedTasks(updateType);
  }
}
