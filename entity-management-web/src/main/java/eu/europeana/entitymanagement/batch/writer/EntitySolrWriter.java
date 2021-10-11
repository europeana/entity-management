package eu.europeana.entitymanagement.batch.writer;

import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.solr.SolrUtils;
import eu.europeana.entitymanagement.solr.model.SolrEntity;
import eu.europeana.entitymanagement.solr.service.SolrService;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static eu.europeana.entitymanagement.solr.SolrUtils.createSolrEntity;

/**
 * Saves Entities to Solr
 */
@Component
public class EntitySolrWriter implements ItemWriter<EntityRecord> {
    private final SolrService solrService;

    public EntitySolrWriter(SolrService solrService) {
        this.solrService = solrService;
    }

    @Override
    public void write(List<? extends EntityRecord> entityRecords) throws Exception {
        List<SolrEntity<?>> solrEntities = entityRecords.stream()
                .map(entityRecord -> createSolrEntity(entityRecord.getEntity()))
                .collect(Collectors.toList());

        solrService.storeMultipleEntities(solrEntities);
    }
}
