package eu.europeana.entitymanagement.scoring.model;

import org.apache.solr.client.solrj.beans.Field;

public class PageRank {

    @Field("page_url")
    String pageUrl;
    @Field("page_rank")
    Double pageRank;

    public PageRank() {

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
