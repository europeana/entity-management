package eu.europeana.entitymanagement.zoho.utils;

import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WikidataUtils {

  /**
   * Checks if the entity with the given id and type is a wikidata organization
   *
   * @param id external entity id
   * @param entityType entity type
   * @return true if given entity is a wikidata organization, false otherwise
   */
  public static boolean isWikidataOrganization(String id, String entityType) {
    return EntityTypes.Organization.getEntityType().equals(entityType) && isWikidataEntity(id);
  }

  /**
   * Checks if the entity with the given id is a wikidata entity
   *
   * @param id external entity id
   * @return true if given entity is a wikidata entity, false otherwise
   */
  public static boolean isWikidataEntity(String id) {
    return id.contains(WebEntityFields.WIKIDATA_HOST);
  }

  /**
   * Gets the first wikidata uri from a list of uri values
   *
   * @param uriList list of uri values
   * @return Optional containing found wikidata uri, or empty Optional if none found
   */
  public static Optional<String> getWikidataId(List<String> uriList) {
    if (uriList != null) {
      return uriList.stream().filter(WikidataUtils::isWikidataEntity).findFirst();
    }
    return Optional.empty();
  }

  /**
   * extract wikidata uris
   *
   * @param uriList list of references
   * @return list of wikidata uris found in the input
   */
  public static List<String> getAllWikidataIds(List<String> uriList) {
    if (uriList == null || uriList.isEmpty()) {
      return Collections.emptyList();
    }

    return uriList.stream().filter(WikidataUtils::isWikidataEntity).collect(Collectors.toList());
  }
}
