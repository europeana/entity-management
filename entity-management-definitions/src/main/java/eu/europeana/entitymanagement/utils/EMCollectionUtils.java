package eu.europeana.entitymanagement.utils;

import eu.europeana.api.commons.definitions.utils.ComparatorUtils;
import java.util.*;
import javax.validation.constraints.NotNull;

public class EMCollectionUtils {

  private EMCollectionUtils() {
    // hide implicit public constructor
  }

  /**
   * Appends elements to a destination list from a source list, ensuring that the size of the
   * destination does not exceed a specified threshold.
   *
   * @param dest destination list
   * @param addition list to copy elements from
   * @param maxSize max size of destination list
   * @param <E>
   */
  public static <E> void addToList(@NotNull List<E> dest, @NotNull List<E> addition, int maxSize) {
    if (dest.size() < maxSize) {
      dest.addAll(addition.subList(0, Math.min(maxSize - dest.size(), addition.size())));
    }
  }

  /**
   * Returns the first element from the list
   *
   * @param values list of values
   * @return first element of the list
   */
  public static String getFirstElement(List<String> values) {
    if (values != null && !values.isEmpty()) {
      Optional<String> value = values.stream().findFirst();
      return value.isPresent() ? value.get() : null;
    }
    return null;
  }

  /**
   * Checks if the element of Secondary List is present in the primary list of objects Takes into
   * account punctuation, spaces and case
   *
   * @param listPrimaryObject
   * @param elemSecondaryList
   * @param doSloppyMatch does a loose comparison neglecting lower/upper case, spaces or
   *     punctuations.
   * @return
   */
  public static boolean ifValueAlreadyExistsInList(
      List<Object> listPrimaryObject, Object elemSecondaryList, boolean doSloppyMatch) {
    // check for exact match if present. If a value is present exactly there is no need to do any
    // further checks
    if (listPrimaryObject.contains(elemSecondaryList)) {
      return true;
    }

    // if doSloppyMatch is true, do further checks for any space or cases etc.
    if (doSloppyMatch) {
      for (Object primaryValue : listPrimaryObject) {
        if (ComparatorUtils.sameValueWithoutSpace(
            ComparatorUtils.stripPunctuation(primaryValue.toString()),
            ComparatorUtils.stripPunctuation(elemSecondaryList.toString()))) {
          return true;
        }
      }
    }
    return false;
  }
}
