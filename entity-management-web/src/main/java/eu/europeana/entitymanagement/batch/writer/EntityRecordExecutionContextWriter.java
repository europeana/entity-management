package eu.europeana.entitymanagement.batch.writer;

import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.ENTITY_RECORD_CTX_KEY;

/**
 * This {@link ItemWriter} saves an EntityRecord list to the Job ExecutionContext.
 * The list can be retrieved in subsequent Steps using {@link eu.europeana.entitymanagement.batch.reader.EntityRecordExecutionContextReader}
 */
@Component
public class EntityRecordExecutionContextWriter implements ItemWriter<EntityRecord> {
    private StepExecution stepExecution;

    public void write(@NonNull List<? extends EntityRecord> items) throws Exception {
        ExecutionContext stepContext = this.stepExecution.getExecutionContext();
        List<EntityRecord> e = new ArrayList<>(items);
        stepContext.put(ENTITY_RECORD_CTX_KEY, e);
    }

    @BeforeStep
    public void saveStepExecution(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }
}