package eu.europeana.entitymanagement.batch.writer;

import eu.europeana.entitymanagement.batch.utils.BatchUtils;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.solr.service.SolrService;
import java.util.List;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/** ItemWriter for removing entities from Solr */
@Component
public class EntitySolrRemovalWriter implements ItemWriter<EntityRecord> {
  private final SolrService solrService;

  public EntitySolrRemovalWriter(SolrService solrService) {
    this.solrService = solrService;
  }

  @Override
  public void write(@NonNull List<? extends EntityRecord> entityRecords) throws Exception {
    String[] entityIds = BatchUtils.getEntityIds(entityRecords);
    solrService.deleteById(List.of(entityIds), true);
  }
}
