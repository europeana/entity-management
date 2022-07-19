package eu.europeana.entitymanagement.batch.writer;

import java.util.List;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import eu.europeana.entitymanagement.batch.utils.BatchUtils;
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.solr.service.SolrService;

/** ItemWriter for removing entities from Solr */
@Component
public class EntitySolrRemovalWriter implements ItemWriter<BatchEntityRecord> {
  private final SolrService solrService;

  public EntitySolrRemovalWriter(SolrService solrService) {
    this.solrService = solrService;
  }

  @Override
  public void write(@NonNull List<? extends BatchEntityRecord> entityRecords) throws Exception {
    String[] entityIds = BatchUtils.getEntityIds((List<BatchEntityRecord>)BatchUtils.filterRecordsForWritters(this.getClass(), entityRecords));
    solrService.deleteById(List.of(entityIds), true);
  }
}
