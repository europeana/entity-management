package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.solr.SolrUtils.createSolrEntity;
import static eu.europeana.entitymanagement.testutils.UnitTestUtils.loadFile;
import java.util.List;
import javax.xml.bind.JAXBContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.Vocabulary;
import eu.europeana.entitymanagement.exception.ingestion.EntityUpdateException;
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;
import eu.europeana.entitymanagement.mongo.repository.VocabularyRepository;
import eu.europeana.entitymanagement.solr.exception.SolrServiceException;
import eu.europeana.entitymanagement.solr.service.SolrService;
import eu.europeana.entitymanagement.web.xml.model.XmlBaseEntityImpl;
import eu.europeana.entitymanagement.web.xml.model.XmlConceptImpl;

@SpringBootTest
@Disabled("Excluded from automated runs")
public class UtilityTests {

  @Qualifier(AppConfig.BEAN_EM_SOLR_SERVICE)
  @Autowired
  private SolrService emSolrService;
  
  @Autowired
  private EntityRecordRepository entityRecordRepository;

  @Autowired
  VocabularyRepository vocabularyRepo;
  
  @Autowired protected JAXBContext jaxbContext;

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
  
//  @Test
  public void saveVocabulariesToMongo() throws Exception {
    List<XmlBaseEntityImpl<?>> xmlEntities = MetisDereferenceUtils.parseMetisResponseMany(
        jaxbContext.createUnmarshaller(), loadFile("/metis-deref-unittest/roles.xml"));
    for(XmlBaseEntityImpl<?> xmlEntity : xmlEntities) {
      XmlConceptImpl xmlConcept = (XmlConceptImpl) xmlEntity;
      Concept concept = xmlConcept.toEntityModel();
      Vocabulary vocab = new Vocabulary();
      vocab.setVocabularyUri(concept.getEntityId());
      vocab.setInScheme(concept.getInScheme());
      vocab.setPrefLabel(concept.getPrefLabel());
      vocabularyRepo.save(vocab);
    }
  }

}
