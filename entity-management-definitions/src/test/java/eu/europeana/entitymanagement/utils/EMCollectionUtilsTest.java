package eu.europeana.entitymanagement.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class EMCollectionUtilsTest {

  @Test
  void shouldAddToListWhenMaxSizeIsNotReached() {
    List<Integer> dest = new ArrayList<>();
    dest.add(1);
    dest.add(2);

    EMCollectionUtils.addToList(dest, List.of(3, 4), 5);

    assertEquals(4, dest.size());
  }

  @Test
  void shouldAddToListWhenMaxSizeIsReached() {
    List<Integer> dest = new ArrayList<>();
    dest.add(1);
    dest.add(2);

    EMCollectionUtils.addToList(dest, List.of(3, 4, 5, 6), 5);

    assertEquals(5, dest.size());
  }

  @Test
  void shouldDoNothingIfDestIsLargerThanMaxSize() {
    List<Integer> dest = new ArrayList<>();
    dest.add(1);
    dest.add(2);
    dest.add(3);

    EMCollectionUtils.addToList(dest, List.of(4, 5), 3);

    assertEquals(3, dest.size());
  }
}
