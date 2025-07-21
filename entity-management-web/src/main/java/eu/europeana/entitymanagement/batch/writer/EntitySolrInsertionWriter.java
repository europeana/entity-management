package eu.europeana.entitymanagement.batch.writer;

import static eu.europeana.entitymanagement.solr.SolrEntityUtils.createSolrEntity;

import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.solr.model.SolrEntity;
import eu.europeana.entitymanagement.solr.service.SolrService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.BEAN_ENTITY_SOLR_INSERTION_WRITER;

/** Saves Entities to Solr */
@Component(BEAN_ENTITY_SOLR_INSERTION_WRITER)
public class EntitySolrInsertionWriter implements ItemWriter<BatchEntityRecord> {
  private final SolrService solrService;

  public EntitySolrInsertionWriter(SolrService solrService) {
    this.solrService = solrService;
  }

  @Override
  public void write(List<? extends BatchEntityRecord> entityRecords) throws Exception {
    //filter out disabled entities and convert to solr docs
    List<SolrEntity<?>> solrEntities =
        entityRecords.stream().filter(record -> !record.getEntityRecord().isDisabled())
            .map(entityRecord -> createSolrEntity(entityRecord.getEntityRecord()))
            .collect(Collectors.toList());

    if (!solrEntities.isEmpty()) {
      solrService.storeMultipleEntities(solrEntities);
    }
  }
}
