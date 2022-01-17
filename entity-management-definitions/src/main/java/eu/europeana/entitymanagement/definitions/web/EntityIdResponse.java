package eu.europeana.entitymanagement.definitions.web;

import static eu.europeana.entitymanagement.utils.EMCollectionUtils.addToList;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EntityIdResponse {

  private long expected;
  private final List<String> successful;
  private final List<String> skipped;
  private final List<String> failed;

  public EntityIdResponse() {
    this.successful = new ArrayList<>();
    this.skipped = new ArrayList<>();
    this.failed = new ArrayList<>();
  }

  public EntityIdResponse(
      long expected, List<String> successful, List<String> failed, List<String> skipped) {
    this.expected = expected;
    this.successful = successful;
    this.skipped = skipped;
    this.failed = failed;
  }

  public long getExpected() {
    return expected;
  }

  public List<String> getSuccessful() {
    return successful;
  }

  public List<String> getSkipped() {
    return skipped;
  }

  public List<String> getFailed() {
    return failed;
  }

  public void updateValues(
      int expected,
      List<String> successfulToAdd,
      List<String> failedToAdd,
      List<String> skippedToAdd,
      int maxSize) {
    this.expected += expected;

    addToList(this.successful, successfulToAdd, maxSize);
    addToList(this.failed, failedToAdd, maxSize);
    addToList(this.skipped, skippedToAdd, maxSize);
  }
}
