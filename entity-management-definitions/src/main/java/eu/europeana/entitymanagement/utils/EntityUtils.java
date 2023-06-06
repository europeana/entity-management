package eu.europeana.entitymanagement.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;

public class EntityUtils {

  public static String createWikimediaResourceString(String wikimediaCommonsId) {
    if (wikimediaCommonsId != null && wikimediaCommonsId.contains("/Special:FilePath/")) {
      return wikimediaCommonsId.replace("/Special:FilePath/", "/File:");
    }

    return null;
  }

  public static String buildConceptSchemeId(String baseUrl, Long identifier) {
    StringBuilder builder = new StringBuilder();
    builder.append(baseUrl);
    if (baseUrl != null && !baseUrl.endsWith("/")) {
      builder.append('/');
    }
    return builder.append(identifier).toString();
  }

  public static String toGeoUri(String latLon) {
    if (latLon == null) {
      return null;
    }
    if (latLon.startsWith(WebEntityConstants.PROTOCOL_GEO)) {
      return latLon;
    }
    return WebEntityConstants.PROTOCOL_GEO + latLon;
  }

  public static String toGeoUri(String lat, String lon) {
    if (lat == null || lon == null) {
      return null;
    }

    return toGeoUri(lat + "," + lon);
  }

  public static String toLatLongValue(String geoUri) {
    if (geoUri == null) {
      return null;
    }
    return geoUri.replaceFirst(WebEntityConstants.PROTOCOL_GEO, "");
  }

  public static boolean compareLists(List<String> l1, List<String> l2) {
    if (CollectionUtils.isEmpty(l1) && CollectionUtils.isEmpty(l2)) {
      return true;
    } else if (CollectionUtils.isEmpty(l1) || CollectionUtils.isEmpty(l2)) {
      return false;
    }
    if (l1.size() != l2.size()) {
      return false;
    }
    return l1.containsAll(l2);
  }
  
  public static boolean compareStringStringMaps(Map<String, String> m1, Map<String, String> m2) {
    if (MapUtils.isEmpty(m1) && MapUtils.isEmpty(m2)) {
      return true;
    } else if (MapUtils.isEmpty(m1) || MapUtils.isEmpty(m2)) {
      return false;
    }

    if(m1.size() != m2.size()) {
      return false;
    }

    for (Map.Entry<String, String> m1Entry : m1.entrySet()) {
      if(! Objects.equals(m1Entry.getValue(), m2.get(m1Entry.getKey()))) {
        return false;
      }
    }
    
    return true;
  }

  public static boolean compareStringListStringMaps(Map<String, List<String>> m1, Map<String, List<String>> m2) {
    if (MapUtils.isEmpty(m1) && MapUtils.isEmpty(m2)) {
      return true;
    } else if (MapUtils.isEmpty(m1) || MapUtils.isEmpty(m2)) {
      return false;
    }

    if(m1.size() != m2.size()) {
      return false;
    }

    for (Map.Entry<String, List<String>> m1Entry : m1.entrySet()) {
      if(! compareLists(m1Entry.getValue(), m2.get(m1Entry.getKey()))) {
        return false;
      }
    }
    
    return true;
  }

  /*
   * getting all fields of the class including the ones from the parent classes using Java reflection
   */
  public static List<Field> getAllFields(Class<?> type) {
    List<Field> entityFields = new ArrayList<Field>();
    getAllFieldsRecursively(entityFields, type);
    return entityFields;
  }

  private static void getAllFieldsRecursively(List<Field> fields, Class<?> type) {
    fields.addAll(Arrays.asList(type.getDeclaredFields()));
    if (type.getSuperclass() != null) {
      getAllFieldsRecursively(fields, type.getSuperclass());
    }
  }
}
