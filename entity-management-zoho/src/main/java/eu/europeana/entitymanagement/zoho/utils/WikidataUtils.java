package eu.europeana.entitymanagement.zoho.utils;

import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import java.util.List;
import java.util.Optional;

public class WikidataUtils {

  /**
   * Checks if the entity with the given id and type is a wikidata organization
   *
   * @param id external entity id
   * @param entityType entity type
   * @return true if given entity is a wikidata organization, false otherwise
   */
  public static boolean isWikidataOrganization(String id, String entityType) {
    return EntityTypes.Organization.getEntityType().equals(entityType)
        && id.contains(DataSource.WIKIDATA_HOST);
  }

  /**
   * Gets the first wikidata uri from a list of uri values
   *
   * @param uriList list of uri values
   * @return Optional containing found wikidata uri, or empty Optional if none found
   */
  public static Optional<String> getWikidataId(List<String> uriList) {
    if (uriList != null) {
      return uriList.stream().filter(s -> s.contains(DataSource.WIKIDATA_HOST)).findFirst();
    }
    return Optional.empty();
  }
}
