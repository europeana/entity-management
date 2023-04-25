package eu.europeana.entitymanagement.mongo.repository;

import static dev.morphia.query.experimental.filters.Filters.eq;

import eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.ConceptSchemeFields;
import eu.europeana.entitymanagement.definitions.model.ConceptScheme;
import org.springframework.stereotype.Repository;

/** Repository for retrieving the EntityRecord objects. */
@Repository(AppConfigConstants.BEAN_CONCEPT_SCHEME_REPO)
public class ConceptSchemeRepository extends AbstractRepository {

  /** @return the total number of resources in the database */
  public long count() {
    return getDataStore().find(ConceptScheme.class).count();
  }

  /**
   * Deletes all EntityRecord objects that contain the given entityId
   *
   * @param the identifier of the ConceptScheme to be deleted
   * @return the number of deleted objects
   */
  public long deleteForGood(long identifier) {
    return getDataStore()
        .find(ConceptScheme.class)
        .filter(eq(ConceptSchemeFields.IDENTIFIER, identifier))
        .delete()
        .getDeletedCount();
  }

  /** Drops the ConceptScheme collection. */
  public void dropCollection() {
    getDataStore().getMapper().getCollection(ConceptScheme.class).drop();
  }

  public ConceptScheme findConceptScheme(long identifier) {
    return getDataStore()
        .find(ConceptScheme.class)
        .filter(eq(ConceptSchemeFields.IDENTIFIER, identifier))
        .first();
  }

  public ConceptScheme saveConceptScheme(ConceptScheme scheme) {
    return getDataStore().save(scheme);
  }
}
