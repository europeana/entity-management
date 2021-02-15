package eu.europeana.entitymanagement.definitions.model.impl;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.vocabulary.XmlFields;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = XmlFields.XML_ORE_AGGREGATION)
public class AggregationImpl implements Aggregation{

	String id, type, rights, source;
	Date created, modified;
	int score, recordCount;
	double pageRank;
	List<String> aggregates;
    
    @Override
    @JsonProperty(WebEntityFields.ID)
	@JacksonXmlProperty(isAttribute= true, localName = XmlFields.XML_RDF_ABOUT)
    public String getId() {
        return id;
    }
    @Override
    public void setId(String id) {
        this.id = id;
    }
    @Override
    @JsonProperty(WebEntityFields.TYPE)
    @JacksonXmlProperty(localName = XmlFields.XML_RDF_TYPE)
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    @Override
    @JsonProperty(WebEntityFields.RIGHTS)
    @JacksonXmlProperty(localName = XmlFields.XML_EDM_RIGHTS)
    public String getRights() {
        return rights;
    }
    @Override
    public void setRights(String rights) {
        this.rights = rights;
    }
    @Override
    @JsonProperty(WebEntityFields.SOURCE)
    @JacksonXmlProperty(localName = XmlFields.XML_DC_SOURCE)
    public String getSource() {
        return source;
    }
    @Override
    public void setSource(String source) {
        this.source = source;
    }
    @Override
    @JsonProperty(WebEntityFields.CREATED)
    @JacksonXmlProperty(localName = XmlFields.XML_DCTERMS_CREATED)
    public Date getCreated() {
        return created;
    }
    @Override
    public void setCreated(Date created) {
        this.created = created;
    }
    @Override
    @JsonProperty(WebEntityFields.MODIFIED)
    @JacksonXmlProperty(localName = XmlFields.XML_DCTERMS_MODIFIED)
    public Date getModified() {
        return modified;
    }
    @Override
    public void setModified(Date modified) {
        this.modified = modified;
    }
    @Override
    @JsonProperty
    @JacksonXmlProperty
    public double getPageRank() {
        return pageRank;
    }
    
    @Override
    public void setPageRank(double pageRank) {
        this.pageRank = pageRank;
    }
    @Override
    @JsonProperty
    @JacksonXmlProperty
    public int getRecordCount() {
        return recordCount;
    }
    @Override
    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }
    @Override
    @JsonProperty
    @JacksonXmlProperty
    public int getScore() {
        return score;
    }
    @Override
    public void setScore(int score) {
        this.score = score;
    }
    @Override
    @JsonProperty(WebEntityFields.AGGREGATES)
    @JacksonXmlProperty(localName = XmlFields.XML_ORE_AGGREGATES)
    public List<String> getAggregates() {
        return aggregates;
    }
    @Override
    public void setAggregates(List<String> aggregates) {
        this.aggregates = aggregates;
    }
    
    
}
