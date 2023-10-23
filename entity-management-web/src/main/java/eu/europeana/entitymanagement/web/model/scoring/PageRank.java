package eu.europeana.entitymanagement.web.model.scoring;

import org.apache.solr.client.solrj.beans.Field;

public class PageRank {

  @Field("identifier")
  String identifier;

  @Field("page_url")
  String pageUrl;

  @Field("page_rank")
  Double pageRank;

  public PageRank() {}
  
  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }  

  public String getPageUrl() {
    return pageUrl;
  }

  public void setPageUrl(String pageUrl) {
    this.pageUrl = pageUrl;
  }

  public Double getPageRank() {
    return pageRank;
  }

  public void setPageRank(Double pageRank) {
    this.pageRank = pageRank;
  }
}
