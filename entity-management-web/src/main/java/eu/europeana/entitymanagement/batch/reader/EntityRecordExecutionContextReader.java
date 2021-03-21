package eu.europeana.entitymanagement.batch.reader;

import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.ENTITY_RECORD_CTX_KEY;

/**
 * This {@link ItemReader} retrieves an EntityRecord list from the Job execution context.
 * The EntityRecord is expected to have been saved in a previous step via {@link eu.europeana.entitymanagement.batch.writer.EntityRecordExecutionContextWriter}
 */
@Component
public class EntityRecordExecutionContextReader implements ItemReader<EntityRecord> {
    private Iterator<EntityRecord> entityRecordList;

    @BeforeStep
    @SuppressWarnings("unchecked")
    public void retrieveEntityRecords(StepExecution stepExecution) {
        JobExecution jobExecution = stepExecution.getJobExecution();
        ExecutionContext jobContext = jobExecution.getExecutionContext();
        List<EntityRecord> savedRecords = (List<EntityRecord>) jobContext.get(ENTITY_RECORD_CTX_KEY);

        if (savedRecords != null) {
            this.entityRecordList = savedRecords.iterator();
        }
    }

    @Override
    @Nullable
    public EntityRecord read() throws Exception {
        if (entityRecordList != null && entityRecordList.hasNext()) {
            return entityRecordList.next();
        }
        return null;
    }
}
