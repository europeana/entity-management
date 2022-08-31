package eu.europeana.entitymanagement.batch.processor;

import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.batch.item.ItemProcessor;

/** Base type for ItemProcessors used during the Update Task */
public abstract class BaseEntityProcessor
    implements ItemProcessor<BatchEntityRecord, BatchEntityRecord> {

  private final Set<ScheduledTaskType> supportedScheduledTasks;

  /**
   * Instantiates the processor with a list of supported ScheduledTask types
   *
   * @param supportedScheduledTasks @{@link ScheduledTaskType} supported by processor
   */
  protected BaseEntityProcessor(@NotNull ScheduledTaskType... supportedScheduledTasks) {
    this.supportedScheduledTasks = Set.of(supportedScheduledTasks);
  }

  /*
   * Checks whether the processor should be executed for the ScheduledUpdateType
   *
   * @param updateType ScheduledTask update type
   * @return
   */
  private boolean shouldProcessUpdateType(ScheduledTaskType updateType) {
    return supportedScheduledTasks.contains(updateType);
  }

  abstract BatchEntityRecord doProcessing(BatchEntityRecord batchEntityRecord) throws Exception;

  @Override
  public BatchEntityRecord process(BatchEntityRecord item) throws Exception {
    if (!shouldProcessUpdateType(item.getScheduledTaskType())) {
      // pass unprocessed item to next stage
      return item;
    }

    return doProcessing(item);
  }
}
