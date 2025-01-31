package eu.europeana.entitymanagement.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.MapUtils;

/**
 * Utils for converting the representation of data to solr fields
 */
public class SolrGeneralUtils {

  /**
   * Hide default constructor
   */
  private SolrGeneralUtils() {

  }

  /**
   * This method adds prefixes to the fields in format Map<String, List<String>> languageMap e.g.
   * "skos_prefLabel"
   *
   * @param fieldNamePrefix e.g. ConceptSolrFields.PREF_LABEL
   * @param languageMap e.g. prefLabel
   * @return normalized content in format Map<String, List<String>>
   */
  public static Map<String, List<String>> normalizeStringListMapByAddingPrefix(
      String fieldNamePrefix, Map<String, List<String>> languageMap) {
    if (MapUtils.isEmpty(languageMap)) {
      return new HashMap<>();
    }
    Map<String, List<String>> res;
    if (languageMap.keySet().iterator().next().contains(fieldNamePrefix)) {
      res = languageMap;
    } else {
      res = languageMap.entrySet().stream().collect(
          Collectors.toMap(entry -> fieldNamePrefix + entry.getKey(), Map.Entry::getValue));
    }
    return res;
  }

  /**
   * This method adds prefixes to the fields in format Map<String, String> languageMap e.g.
   * "skos_prefLabel"
   *
   * @param fieldNamePrefix e.g. ConceptSolrFields.PREF_LABEL
   * @param languageMap e.g. prefLabel
   * @return normalized content in format Map<String, String>
   */
  public static Map<String, String> normalizeStringMapByAddingPrefix(String fieldNamePrefix,
      Map<String, String> languageMap) {

    if (MapUtils.isEmpty(languageMap)) {
      return new HashMap<>();
    }

    Map<String, String> res;
    if (languageMap.keySet().iterator().next().contains(fieldNamePrefix)) {
      res = languageMap;
    } else {
      res = languageMap.entrySet().stream().collect(
          Collectors.toMap(entry -> fieldNamePrefix + entry.getKey(), Map.Entry::getValue));
    }
    return res;
  }

}
