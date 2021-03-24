package eu.europeana.entitymanagement.batch.reader;

import dev.morphia.query.experimental.filters.Filter;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.web.service.impl.EntityRecordService;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.AbstractPaginatedDataItemReader;
import org.springframework.lang.NonNull;

import java.util.Iterator;

/**
 * Restartable {@link ItemReader} that reads documents from MongoDB
 * via a paging technique.
 */
public class EntityRecordDatabaseReader extends AbstractPaginatedDataItemReader<EntityRecord> {
    private final EntityRecordService entityRecordService;
    private final Filter[] queryFilters;
    private final int pageSize;

    public EntityRecordDatabaseReader(EntityRecordService entityRecordService, int pageSize, Filter... queryFilters) {
        this.entityRecordService = entityRecordService;
        this.queryFilters = queryFilters;
        this.pageSize = pageSize;
    }

    @Override
    protected void doOpen() throws Exception {
        super.doOpen();
        setName(EntityRecordDatabaseReader.class.getSimpleName());
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    protected Iterator<EntityRecord> doPageRead() {
        return (Iterator<EntityRecord>) entityRecordService
                .findEntitiesWithFilter(page, pageSize, queryFilters).iterator();
    }
}
