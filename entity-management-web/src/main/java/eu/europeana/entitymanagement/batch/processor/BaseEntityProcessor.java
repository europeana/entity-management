package eu.europeana.entitymanagement.batch.processor;

import org.springframework.batch.item.ItemProcessor;
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;

/** Base type for ItemProcessors used during the Update Task */
public abstract class BaseEntityProcessor
    implements ItemProcessor<BatchEntityRecord, BatchEntityRecord> {

  protected BaseEntityProcessor() {
  }

  abstract BatchEntityRecord doProcessing(BatchEntityRecord batchEntityRecord) throws Exception;

  @Override
  public BatchEntityRecord process(BatchEntityRecord item) throws Exception {
    return doProcessing(item);
  }
}
