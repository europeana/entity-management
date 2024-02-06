package eu.europeana.entitymanagement.mongo.repository;

import static dev.morphia.query.experimental.filters.Filters.in;
import static eu.europeana.entitymanagement.definitions.VocabularyFields.URI;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Repository;
import dev.morphia.Datastore;
import dev.morphia.query.experimental.filters.Filter;
import eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.model.Vocabulary;

/** Repository for retrieving the Vocabulary objects. */
@Repository(AppConfigConstants.BEAN_VOCABULARY_REPO)
public class VocabularyRepository  {
  
  @Resource(name = AppConfigConstants.BEAN_EM_DATA_STORE)
  Datastore datastore;

  /**
   * retrieve records by their URI/id
   * @param vocabularyUris
   * @return
   */
  public List<Vocabulary> findByUri(List<String> vocabularyUris) {
    List<Filter> filters = new ArrayList<>();
    filters.add(in(URI, vocabularyUris));
    return datastore.find(Vocabulary.class)
        .filter(filters.toArray(Filter[]::new))
        .iterator()
        .toList();
  }

  /**
   * save to database 
   * @param vocab record to save
   * @return saved record
   */
  public Vocabulary save(Vocabulary vocab) {
    return datastore.save(vocab);
  }
  
  /**
   * save list of records to database
   * @param vocabs list of records to save
   * @return saved records
   */
  public List<Vocabulary> saveBulk(List<Vocabulary> vocabs) {
    return datastore.save(vocabs);
  }

  /**
   * clear database collection
   */
  public void dropCollection() {
    datastore.getMapper().getCollection(Vocabulary.class).drop();
  }

  /**
   * count the records available in the database
   * @return number of database records
   */
  public long countRecords() {
    return datastore.find(Vocabulary.class).count();
  }
}