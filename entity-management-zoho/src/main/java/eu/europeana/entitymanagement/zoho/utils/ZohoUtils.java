package eu.europeana.entitymanagement.zoho.utils;

import com.zoho.crm.api.util.Choice;

import eu.europeana.entitymanagement.common.config.DataSources;
import eu.europeana.entitymanagement.definitions.model.WebResource;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public final class ZohoUtils {

  /**
   * Method that would check if the object provided is of type {@link JSONObject.Null} and will
   * return a correct representation of {@link String} or null.
   *
   * @param object the object to be checked
   * @return the string representation of the object or null
   */
  public static String stringFieldSupplier(Object object) {
    if (!JSONObject.NULL.equals(object)) {
      if (object instanceof Choice<?>) {
        return ((Choice<?>) object).getValue().toString();
      } else {
        return object.toString();
      }
    }
    return null;
  }

  /**
   * Method that would check if the object provided is of type {@link JSONObject.Null} and will
   * return a correct representation of {@link List} with {@link String} items or empty list.
   *
   * @param object the object to be checked
   * @return the List of strings representation of the object or empty list
   */
  public static List<String> stringListSupplier(Object object) {
    if (!JSONObject.NULL.equals(object) && object instanceof List<?> && CollectionUtils
        .isNotEmpty((List<?>) object) && ((List<?>) object).get(0) instanceof Choice<?>) {
      return ((List<Choice<?>>) object).stream().map(Choice::getValue).map(String.class::cast)
          .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }
   
  /**
   * creates a Map with list of key and value
   * @param key
   * @param value
   * @return
   */
  public static Map<String, List<String>> createMapWithLists(String key, String value) {
      return value == null ? null : Collections.singletonMap(key, Collections.singletonList(value));
  }

  /**
   * Creates a map of key and value
   * @param key
   * @param value
   * @return
   */
  public static Map<String, String> createMap(String key, String value) {
      return value == null ? null : Collections.singletonMap(key, value);
  }

  /**
   * Creates a List with value
   * @param value
   * @return
   */
  public static List<String> createList(String value) {
      return value == null ? null : Collections.singletonList(value);
  }

  /**
   * Creates a String array for List < ? extends Part>
   * @param resources
   * @return
   */
  public static String[] createStringArrayFromPartList(List<? extends WebResource> resources) {
      return resources == null ? null : resources.stream().map(WebResource::getId).toArray((x$0) -> {
          return new String[x$0];
      });
  }

  /**
   * create language map of List
   * @param language
   * @param value
   * @return
   */
  public static Map<String, List<String>> createLanguageMapOfStringList(String language, String value) {
      return value == null ? null : Collections.singletonMap(toIsoLanguage(language), createList(value));
  }

  /**
   * create language map of list from list of values
   * @param language
   * @param value
   * @return
   */
  public static Map<String, List<String>> createLanguageMapOfStringList(String language, List<String> value) {
      return value == null ? null : Collections.singletonMap(toIsoLanguage(language), value);
  }

  /**
   * converts language into ISO language.
   * Defaults it to "def" if empty
   *
   * @param language
   * @return
   */
  public static String toIsoLanguage(String language) {
      return StringUtils.isBlank(language) ? ZohoConstants.UNDEFINED_LANGUAGE_KEY : language.substring(0, 2).toLowerCase(Locale.US);
  }

  /**
   * Merges maps with list of values
   *
   * @param baseMap
   * @param addMap
   * @return
   */
  public static Map<String, List<String>> mergeMapsWithLists(Map<String, List<String>> baseMap, Map<String, List<String>> addMap) {
      if (baseMap == null && addMap == null) {
          return null;
      } else {
          Map<String, List<String>> result = new HashMap();
          if (baseMap != null) {
              result.putAll(baseMap);
          }
          if (addMap != null) {
              Iterator var4 = addMap.entrySet().iterator();

              while(var4.hasNext()) {
                  Entry<String, List<String>> entry = (Entry)var4.next();
                  result.merge(entry.getKey(), new ArrayList((Collection)entry.getValue()), ZohoUtils::mergeStringLists);
              }
          }
          return result.isEmpty() ? null : result;
      }
  }

  /**
   * merges maps with list of values
   * @param baseMap
   * @param addMap
   * @param notMergedMap
   * @return
   */
  public static Map<String, List<String>> mergeMapsWithSingletonLists(Map<String, List<String>> baseMap, Map<String, List<String>> addMap, Map<String, List<String>> notMergedMap) {
      Map<String, List<String>> result = new HashMap(baseMap);
      Iterator var5 = addMap.entrySet().iterator();

      while(var5.hasNext()) {
          Entry<String, List<String>> entry = (Entry)var5.next();
          String key = entry.getKey();
          if (result.containsKey(key)) {
              List<String> unmergedValues = (List)((List)entry.getValue()).stream().distinct().filter(value -> {
                  return !((List)result.get(key)).contains(value);
              }).collect(Collectors.toList());
              if (!unmergedValues.isEmpty()) {
                  notMergedMap.merge(key, unmergedValues, ZohoUtils::mergeStringLists);
              }
          } else {
              result.put(key, new ArrayList(entry.getValue()));
          }
      }

      return result;
  }

  /**
   * merges lists of String
   *
   * @param baseList
   * @param addList
   * @return
   */
  public static List<String> mergeStringLists(List<String> baseList, List<String> addList) {
      Set<String> result = new HashSet();
      if (baseList != null) {
          result.addAll(baseList);
      }
      if (addList != null) {
          result.addAll(addList);
      }
      return result.isEmpty() ? null : new ArrayList(result);
  }

  /**
   * Creates a map with List of list of keys and values
   * @param keys
   * @param values
   * @return
   */
  public static Map<String, List<String>> createMapWithLists(List<String> keys, List<String> values) {
      if (keys != null && !keys.isEmpty()) {
          Map<String, List<String>> resMap = new HashMap(keys.size());
          for(int i = 0; i < keys.size(); ++i) {
              resMap.put(toIsoLanguage(keys.get(i)), createList(values.get(i)));
          }
          return resMap;
      } else {
          return null;
      }
  }

  /**
   * merges String arrays
   * @param base
   * @param add
   * @return
   */
  public static String[] mergeStringArrays(String[] base, String[] add) {
      List<String> baseList = base == null ? Collections.emptyList() : Arrays.asList(base);
      List<String> addList = add == null ? Collections.emptyList() : Arrays.asList(add);
      List<String> mergedList = mergeStringLists(baseList, addList);
      return mergedList == null ? null : mergedList.toArray(new String[0]);
  }
  
  /**
   * get the zohoId as the last part of the id (the part after the last /)
   * @param id
   */
  public static String getZohoId (String id) {
      if(!id.contains("/")) {
          return id;
      }else {
          String[] uriParts = id.split("/");
          return uriParts[uriParts.length -1];     
      }
  }
  
  /**
   * check if the entity is the Zoho organization
   * @param id
   * @param entityType
   * @return
   */
  public static boolean isZohoOrganization(String id, String entityType) {
      return EntityTypes.Organization.getEntityType().equals(entityType) && id.contains(DataSources.ZOHO_ID);
  }
  
}
