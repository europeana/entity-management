package eu.europeana.entitymanagement.batch.writer;

import eu.europeana.entitymanagement.batch.utils.BatchUtils;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import java.util.List;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/** ItemWriter for removing entities from Solr */
@Component
public class EntitySolrRemovalWriter implements ItemWriter<EntityRecord> {
  private final EntityRecordService entityRecordService;

  public EntitySolrRemovalWriter(EntityRecordService entityRecordService) {
    this.entityRecordService = entityRecordService;
  }

  @Override
  public void write(@NonNull List<? extends EntityRecord> entityRecords) throws Exception {
    String[] entityIds = BatchUtils.getEntityIds(entityRecords);
    entityRecordService.deleteFromSolr(List.of(entityIds));
  }
}
