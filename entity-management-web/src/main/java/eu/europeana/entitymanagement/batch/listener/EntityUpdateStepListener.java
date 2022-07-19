package eu.europeana.entitymanagement.batch.listener;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.lang.NonNull;
import eu.europeana.entitymanagement.batch.service.ScheduledTaskService;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;

/** Listens for Step execution lifecycle events. */
public class EntityUpdateStepListener implements StepExecutionListener {

  private static final Logger logger = LogManager.getLogger(EntityUpdateStepListener.class);

  private final ScheduledTaskService scheduledTaskService;
  private final List<? extends ScheduledTaskType> updateType;

  private final int maxFailedTaskRetries;
  private final boolean isSynchronous;

  public EntityUpdateStepListener(
      ScheduledTaskService scheduledTaskService,
      List<? extends ScheduledTaskType> updateType,
      boolean isSynchronous,
      int maxFailedTaskRetries) {
    this.scheduledTaskService = scheduledTaskService;
    this.updateType = updateType;
    this.isSynchronous = isSynchronous;

    this.maxFailedTaskRetries = maxFailedTaskRetries;
  }

  /**
   * Cleanup processed tasks before every update run, in case job was terminated before completion
   */
  @Override
  public void beforeStep(@NonNull StepExecution stepExecution) {
    // for now, we don't need any cleanup for synchronous steps
    if (!isSynchronous) {
      logger.debug("Cleaning up processed tasks before step execution. updateType={}", 
          updateType.stream().map(u -> u.getValue()).collect(Collectors.joining(",")));
      // remove processed tasks here, in case application restarted before step finished execution
      scheduledTaskService.removeProcessedTasks(updateType);
    }
  }

  @Override
  public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
    // no cleanup needed for synchronous steps
    if (isSynchronous) {
      return ExitStatus.NOOP;
    }

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
