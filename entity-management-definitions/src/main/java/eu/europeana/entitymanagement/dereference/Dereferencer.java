package eu.europeana.entitymanagement.dereference;

import eu.europeana.entitymanagement.definitions.model.Entity;
import java.util.Optional;

public interface Dereferencer {

  /**
   * Dereferences the entity with the given id from the external data source.
   *
   * @param id         external ID for entity
   * @return An optional containing the de-referenced entity, or an empty optional if no match
   * found.
   **/
  Optional<Entity> dereferenceEntityById(String id) throws Exception;

}