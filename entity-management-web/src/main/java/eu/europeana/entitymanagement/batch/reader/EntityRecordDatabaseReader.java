package eu.europeana.entitymanagement.batch.reader;

import dev.morphia.query.experimental.filters.Filter;
import eu.europeana.entitymanagement.batch.processor.EntityDereferenceProcessor;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.web.service.impl.EntityRecordService;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.AbstractPaginatedDataItemReader;
import org.springframework.lang.NonNull;

import java.util.Iterator;

/**
 * Restartable {@link ItemReader} that reads documents from MongoDB via a paging technique.
 */
public class EntityRecordDatabaseReader extends AbstractPaginatedDataItemReader<EntityRecord> {

  private static final Logger logger = LogManager.getLogger(EntityRecordDatabaseReader.class);
  private final EntityRecordService entityRecordService;
  private final Filter[] queryFilters;
  private final int pageSize;

  public EntityRecordDatabaseReader(EntityRecordService entityRecordService, int pageSize,
      Filter... queryFilters) {
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
    List<? extends EntityRecord> result = entityRecordService
        .findEntitiesWithFilter(page, pageSize, queryFilters);

    logger.debug("Retrieved {} EntityRecords from database. skip={} limit={}", result.size(), page,
        pageSize);
    return (Iterator<EntityRecord>) result.iterator();
  }
}
