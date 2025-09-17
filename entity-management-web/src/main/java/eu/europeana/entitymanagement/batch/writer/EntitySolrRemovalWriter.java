package eu.europeana.entitymanagement.batch.writer;

import eu.europeana.entitymanagement.batch.utils.BatchUtils;
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.solr.service.SolrService;
import java.util.List;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.ENTITY_SOLR_REMOVAL_WRITER;

/** ItemWriter for removing entities from Solr */
@Component(ENTITY_SOLR_REMOVAL_WRITER)
public class EntitySolrRemovalWriter implements ItemWriter<BatchEntityRecord> {

  private final SolrService solrService;

  public EntitySolrRemovalWriter(SolrService solrService) {
    this.solrService = solrService;
  }

  @Override
  public void write(@NonNull List<? extends BatchEntityRecord> entityRecords) throws Exception {
    List<String> entityIds =
        BatchUtils.filterRecordsForWriters(entityRecords);

    if (!entityIds.isEmpty()) {
      solrService.deleteById(entityIds, true);
    }
  }
}
