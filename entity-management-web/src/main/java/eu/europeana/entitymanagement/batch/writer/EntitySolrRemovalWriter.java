package eu.europeana.entitymanagement.batch.writer;

import eu.europeana.entitymanagement.batch.utils.BatchUtils;
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledRemovalType;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import eu.europeana.entitymanagement.solr.service.SolrService;
import java.util.List;
import java.util.Set;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/** ItemWriter for removing entities from Solr */
@Component
public class EntitySolrRemovalWriter implements ItemWriter<BatchEntityRecord> {

  private static final Set<ScheduledTaskType> supportedScheduledTasks =
      Set.of(ScheduledRemovalType.DEPRECATION, ScheduledRemovalType.PERMANENT_DELETION);

  private final SolrService solrService;

  public EntitySolrRemovalWriter(SolrService solrService) {
    this.solrService = solrService;
  }

  @Override
  public void write(@NonNull List<? extends BatchEntityRecord> entityRecords) throws Exception {
    String[] entityIds =
        BatchUtils.getEntityIds(
            (List<BatchEntityRecord>)
                BatchUtils.filterRecordsForWritters(supportedScheduledTasks, entityRecords));
    solrService.deleteById(List.of(entityIds), true);
  }
}
