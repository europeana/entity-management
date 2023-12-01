package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.solr.SolrUtils.createSolrEntity;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.exception.ingestion.EntityUpdateException;
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;
import eu.europeana.entitymanagement.solr.exception.SolrServiceException;
import eu.europeana.entitymanagement.solr.service.SolrService;

@SpringBootTest
@Disabled("Excluded from automated runs")
public class UtilityTests {

  @Qualifier(AppConfig.BEAN_EM_SOLR_SERVICE)
  @Autowired
  private SolrService emSolrService;
  
  @Autowired
  private EntityRecordRepository entityRecordRepository;

  /**
   * This test can be used to reindex the local/other solr from mongo, when the schema fields change.
   * @throws EntityUpdateException
   */
//  @Test
  public void reindexSolrFromMongo() throws EntityUpdateException {
    List<EntityRecord> allRecords = entityRecordRepository.findAll(0, 100);
    for(EntityRecord er : allRecords) {
      try {
        emSolrService.storeEntity(createSolrEntity(er));
      } catch (SolrServiceException e) {
        throw new EntityUpdateException(
            "Cannot create solr record for entity with id: " + er, e);
      }
    }    
  }

}
