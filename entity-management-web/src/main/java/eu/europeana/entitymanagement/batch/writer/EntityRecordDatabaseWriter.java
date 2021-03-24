package eu.europeana.entitymanagement.batch.writer;

import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.web.service.impl.EntityRecordService;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * This {@link ItemWriter} saves EntityRecords to the database.
 */
@Component
public class EntityRecordDatabaseWriter implements ItemWriter<EntityRecord> {

    private final EntityRecordService entityRecordService;

    public EntityRecordDatabaseWriter(EntityRecordService entityRecordService) {
        this.entityRecordService = entityRecordService;
    }


    @Override
    public void write(@NonNull List<? extends EntityRecord> list) throws Exception {
        entityRecordService.saveBulkEntityRecords(list);
    }
}
