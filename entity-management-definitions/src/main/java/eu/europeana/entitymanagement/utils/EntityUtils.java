package eu.europeana.entitymanagement.utils;

import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EntityUtils {

  public static String createWikimediaResourceString(String wikimediaCommonsId) {
    assert wikimediaCommonsId.contains("Special:FilePath/");
    return wikimediaCommonsId.replace("Special:FilePath/", "File:");
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
