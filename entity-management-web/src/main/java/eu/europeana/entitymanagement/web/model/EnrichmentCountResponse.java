package eu.europeana.entitymanagement.web.model;

/** Response from Search API enrichment count query */
public class EnrichmentCountResponse {

  private int totalResults;

  public void setTotalResults(int totalResults) {
    this.totalResults = totalResults;
  }

  public int getTotalResults() {
    return totalResults;
  }

  @Override
  public String toString() {
    return "{" + "totalResults=" + totalResults + '}';
  }
}
