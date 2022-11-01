package eu.europeana.entitymanagement.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EMCollectionUtilsTest {

  private List<Object> primaryList;

  @BeforeEach
  void setup() {
    primaryList = new ArrayList<>();
    primaryList.add("Netherlands Institute of Sound and Vision");
    primaryList.add("Nederlands Instituut Voor Beeld en Geluid");
    primaryList.add("http://data.europeana.eu/test/14");
    primaryList.add("http://data.europeana.eu/test/15");
  }

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

  @Test
  void testIfUriAlreadyExists() {
    // exact match of Uri
    Assertions.assertTrue(
        EMCollectionUtils.ifUriAlreadyExists(primaryList, "http://data.europeana.eu/test/14"));
    Assertions.assertFalse(
        EMCollectionUtils.ifUriAlreadyExists(primaryList, "http://data.europeana.eu/test/16"));
    Assertions.assertTrue(
        EMCollectionUtils.ifUriAlreadyExists(primaryList, "http://data.europeana.eu/test/15"));
  }

  @Test
  void testIfValueAlreadyExistsInList() {
    // exact match if value is uri
    Assertions.assertTrue(
        EMCollectionUtils.ifValueAlreadyExistsInList(
            primaryList, "http://data.europeana.eu/test/14"));
    Assertions.assertFalse(
        EMCollectionUtils.ifValueAlreadyExistsInList(
            primaryList, "http://data.europeana.eu/test/16"));
    Assertions.assertTrue(
        EMCollectionUtils.ifValueAlreadyExistsInList(
            primaryList, "http://data.europeana.eu/test/15"));

    // URI value present in primary list but in lower case, should return false as it's not a exact
    // match
    Assertions.assertFalse(
        EMCollectionUtils.ifValueAlreadyExistsInList(
            primaryList, "http://data.europeana.eu/TEST/15"));

    // exact match for string values
    Assertions.assertTrue(
        EMCollectionUtils.ifValueAlreadyExistsInList(
            primaryList, "Netherlands Institute of Sound and Vision"));

    // further string checks
    // small case
    Assertions.assertTrue(
        EMCollectionUtils.ifValueAlreadyExistsInList(
            primaryList, "netherlands institute of sound and vision"));
    // spaces
    Assertions.assertTrue(
        EMCollectionUtils.ifValueAlreadyExistsInList(
            primaryList, "Nederlands   Instituut Voor   Beeld en  Geluid"));
  }
}
