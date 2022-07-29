package eu.europeana.entitymanagement.utils;

import java.util.List;
import java.util.Map;

public class ComparisonUtils {

  /**
   * Checks if two Maps of Lists are equal.
   *
   * @param first first map
   * @param second second map
   * @return true if both maps are equal, false otherwise
   */
  public static <T, U> boolean areMapsEqual(Map<T, List<U>> first, Map<T, List<U>> second) {
    if (first == null && second == null) {
      return true;
    }

    // if only one of them is null, then they're not equal
    if (first == null ^ second == null) {
      return false;
    }

    if (first.size() != second.size()) {
      return false;
    }

    return first.entrySet().stream()
        .allMatch(e -> areListsEqual(e.getValue(), second.get(e.getKey())));
  }

  /**
   * Checks if two lists are equal.
   *
   * @param first first list
   * @param second second list
   * @return true if both lists are equal, false otherwise
   */
  public static <T> boolean areListsEqual(List<T> first, List<T> second) {
    if (first == null && second == null) {
      return true;
    }

    // if only one of them is null, then they're not equal
    if (first == null ^ second == null) {
      return false;
    }

    return first.size() == second.size() && first.containsAll(second) && second.containsAll(first);
  }
}
