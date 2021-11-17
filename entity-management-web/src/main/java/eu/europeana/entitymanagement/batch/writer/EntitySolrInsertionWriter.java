package eu.europeana.entitymanagement.batch.writer;

import static eu.europeana.entitymanagement.solr.SolrUtils.createSolrEntity;

import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.solr.model.SolrEntity;
import eu.europeana.entitymanagement.solr.service.SolrService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

/** Saves Entities to Solr */
@Component
public class EntitySolrInsertionWriter implements ItemWriter<EntityRecord> {
  private final SolrService solrService;

  public EntitySolrInsertionWriter(SolrService solrService) {
    this.solrService = solrService;
  }

  @Override
  public void write(List<? extends EntityRecord> entityRecords) throws Exception {
    List<SolrEntity<?>> solrEntities =
        entityRecords.stream()
            .map(entityRecord -> createSolrEntity(entityRecord))
            .collect(Collectors.toList());

    solrService.storeMultipleEntities(solrEntities);
  }
}
