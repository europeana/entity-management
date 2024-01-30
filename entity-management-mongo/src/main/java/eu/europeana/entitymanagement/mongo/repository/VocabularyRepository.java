package eu.europeana.entitymanagement.mongo.repository;

import static dev.morphia.query.experimental.filters.Filters.in;
import static eu.europeana.entitymanagement.definitions.VocabularyFields.VOCABULARY_URI;
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

  public List<Vocabulary> findByVocabularyUris(List<String> vocabularyUris) {
    List<Filter> filters = new ArrayList<>();
    filters.add(in(VOCABULARY_URI, vocabularyUris));
    return datastore.find(Vocabulary.class)
        .filter(filters.toArray(Filter[]::new))
        .iterator()
        .toList();
  }

  public Vocabulary save(Vocabulary vocab) {
    return datastore.save(vocab);
  }
  
  public List<Vocabulary> saveBulk(List<Vocabulary> vocabs) {
    return datastore.save(vocabs);
  }

  public void dropCollection() {
    datastore.getMapper().getCollection(Vocabulary.class).drop();
  }
}
