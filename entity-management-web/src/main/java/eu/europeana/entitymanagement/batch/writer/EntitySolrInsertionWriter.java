package eu.europeana.entitymanagement.batch.writer;

import static eu.europeana.entitymanagement.solr.SolrUtils.createSolrEntity;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.solr.model.SolrEntity;
import eu.europeana.entitymanagement.solr.service.SolrService;

/** Saves Entities to Solr */
@Component
public class EntitySolrInsertionWriter implements ItemWriter<BatchEntityRecord> {
  private final SolrService solrService;

  public EntitySolrInsertionWriter(SolrService solrService) {
    this.solrService = solrService;
  }

  @Override
  public void write(List<? extends BatchEntityRecord> entityRecords) throws Exception {
    List<SolrEntity<?>> solrEntities =
        entityRecords.stream()
            .map(entityRecord -> createSolrEntity(entityRecord.getEntityRecord()))
            .collect(Collectors.toList());

    solrService.storeMultipleEntities(solrEntities);
  }
}
