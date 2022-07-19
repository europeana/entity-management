package eu.europeana.entitymanagement.utils;

import java.util.List;
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

  public static String getFirstElement(List<String> values) {
    if (values != null && !values.isEmpty()) {
      return values.stream().findFirst().get();
    }
    return null;
  }

}
