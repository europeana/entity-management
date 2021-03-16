package eu.europeana.entitymanagement.definitions.model.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.vocabulary.XmlFields;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.AGGREGATION;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = XmlFields.XML_ORE_AGGREGATION)
public class AggregationImpl implements Aggregation{

	public AggregationImpl(Aggregation copy) {
		super();
		this.id = copy.getId();
		this.type = copy.getType();
		this.rights = copy.getRights();
		this.source = copy.getSource();
		this.created = new Date(copy.getCreated().getTime());
		this.modified = new Date(copy.getModified().getTime());
		this.score = copy.getScore();
		this.recordCount = copy.getRecordCount();
		this.pageRank = copy.getPageRank();
		this.aggregates = copy.getAggregates()!=null ? new ArrayList<String>(copy.getAggregates()) : null;
	}
	public AggregationImpl() {
		super();
	}
	String id, type, rights, source;
	Date created, modified;
	int score, recordCount;
	double pageRank;
	List<String> aggregates;
    
    @Override
    @JsonGetter(WebEntityFields.ID)
	@JacksonXmlProperty(isAttribute= true, localName = XmlFields.XML_RDF_ABOUT)
    public String getId() {
        return id;
    }
    @Override
    @JsonSetter(WebEntityFields.ID)
    public void setId(String id) {
        this.id = id;
    }
    @Override
    @JsonGetter(WebEntityFields.TYPE)
    @JacksonXmlProperty(localName = XmlFields.XML_RDF_TYPE)
    public String getType() {
      return AGGREGATION;
    }


    @Override
    @JsonGetter(WebEntityFields.RIGHTS)
    @JacksonXmlProperty(localName = XmlFields.XML_EDM_RIGHTS)
    public String getRights() {
        return rights;
    }
    @Override
    @JsonSetter(WebEntityFields.RIGHTS)
    public void setRights(String rights) {
        this.rights = rights;
    }
    @Override
    @JsonGetter(WebEntityFields.SOURCE)
    @JacksonXmlProperty(localName = XmlFields.XML_DC_SOURCE)
    public String getSource() {
        return source;
    }
    @Override
    @JsonSetter(WebEntityFields.SOURCE)
    public void setSource(String source) {
        this.source = source;
    }
    @Override
    @JsonGetter(WebEntityFields.CREATED)
    @JacksonXmlProperty(localName = XmlFields.XML_DCTERMS_CREATED)
    public Date getCreated() {
        return created;
    }
    @Override
    @JsonSetter(WebEntityFields.CREATED)
    public void setCreated(Date created) {
        this.created = created;
    }
    @Override
    @JsonGetter(WebEntityFields.MODIFIED)
    @JacksonXmlProperty(localName = XmlFields.XML_DCTERMS_MODIFIED)
    public Date getModified() {
        return modified;
    }
    @Override
    @JsonSetter(WebEntityFields.MODIFIED)
    public void setModified(Date modified) {
        this.modified = modified;
    }
    @Override
    @JsonGetter
    @JacksonXmlProperty
    public double getPageRank() {
        return pageRank;
    }
    
    @Override
    @JsonSetter
    public void setPageRank(double pageRank) {
        this.pageRank = pageRank;
    }
    @Override
    @JsonGetter
    @JacksonXmlProperty
    public int getRecordCount() {
        return recordCount;
    }
    @Override
    @JsonSetter
    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }
    @Override
    @JsonGetter
    @JacksonXmlProperty
    public int getScore() {
        return score;
    }
    @Override
    @JsonSetter
    public void setScore(int score) {
        this.score = score;
    }
    @Override
    @JsonGetter(WebEntityFields.AGGREGATES)
    @JacksonXmlProperty(localName = XmlFields.XML_ORE_AGGREGATES)
    public List<String> getAggregates() {
        return aggregates;
    }
    @Override
    @JsonSetter(WebEntityFields.AGGREGATES)
    public void setAggregates(List<String> aggregates) {
        this.aggregates = aggregates;
    }
    
    
}
