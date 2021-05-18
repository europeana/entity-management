package eu.europeana.entitymanagement.batch.reader;

import dev.morphia.query.experimental.filters.Filter;
import eu.europeana.entitymanagement.batch.BatchUtils;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.AbstractPaginatedDataItemReader;
import org.springframework.lang.NonNull;

import java.util.Iterator;

/**
 * {@link ItemReader} that reads documents from MongoDB via a paging technique.
 */
public class EntityRecordDatabaseReader extends BaseDatabaseReader<EntityRecord> {

  private static final Logger logger = LogManager.getLogger(EntityRecordDatabaseReader.class);
  private final EntityRecordService entityRecordService;
  private final Filter[] queryFilters;

  public EntityRecordDatabaseReader(EntityRecordService entityRecordService, int pageSize,
      Filter... queryFilters) {
    super(pageSize);
    this.entityRecordService = entityRecordService;
    this.queryFilters = queryFilters;
  }
  

  @Override
  @NonNull
  @SuppressWarnings("unchecked")
  protected Iterator<EntityRecord> doPageRead() {
    // number of items to skip when reading. pageSize is incremented in parent class every time
    // this method is invoked
    int start = page * pageSize;
    List<? extends EntityRecord> result = entityRecordService
        .findEntitiesWithFilter(start, pageSize, queryFilters);

    logger.debug("Retrieved {} EntityRecords from database. skip={}, limit={}, entityIds={}", result.size(), start,
        pageSize, Arrays.toString(BatchUtils.getEntityIds(result)));
    return (Iterator<EntityRecord>) result.iterator();
  }

  @Override
  String getClassName() {
    return EntityRecordDatabaseReader.class.getSimpleName();
  }
}
