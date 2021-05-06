package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.*;

import dev.morphia.annotations.Embedded;
import eu.europeana.entitymanagement.serialization.PositiveNumberFilter;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.vocabulary.XmlFields;

@Embedded
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = XmlFields.XML_ORE_AGGREGATION)
@JsonPropertyOrder({ID, TYPE, CREATED, MODIFIED, PAGE_RANK, RECORD_COUNT, SCORE, AGGREGATES})
public class Aggregation {
    
    String id, type, rights, source;
    Date created, modified;
    int score, recordCount;
    double pageRank;
    List<String> aggregates;

    
    @JsonGetter(ID)
    @JacksonXmlProperty(isAttribute= true, localName = XmlFields.XML_RDF_ABOUT)
    public String getId() {
        return id;
    }
    
    @JsonSetter(ID)
    public void setId(String id) {
        this.id = id;
    }
    
    @JsonGetter(TYPE)
    @JacksonXmlProperty(localName = XmlFields.XML_RDF_TYPE)
    public String getType() {
        return AGGREGATION;
    }


    
    @JsonGetter(RIGHTS)
    @JacksonXmlProperty(localName = XmlFields.XML_EDM_RIGHTS)
    public String getRights() {
        return rights;
    }
    
    @JsonSetter(RIGHTS)
    public void setRights(String rights) {
        this.rights = rights;
    }
    
    @JsonGetter(SOURCE)
    @JacksonXmlProperty(localName = XmlFields.XML_DC_SOURCE)
    public String getSource() {
        return source;
    }
    
    @JsonSetter(SOURCE)
    public void setSource(String source) {
        this.source = source;
    }
    
    @JsonGetter(CREATED)
    @JacksonXmlProperty(localName = XmlFields.XML_DCTERMS_CREATED)
    public Date getCreated() {
        return created;
    }
    
    @JsonSetter(CREATED)
    public void setCreated(Date created) {
        this.created = created;
    }
    
    @JsonGetter(MODIFIED)
    @JacksonXmlProperty(localName = XmlFields.XML_DCTERMS_MODIFIED)
    public Date getModified() {
        return modified;
    }
    
    @JsonSetter(MODIFIED)
    public void setModified(Date modified) {
        this.modified = modified;
    }
    
    @JsonGetter
    @JacksonXmlProperty
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = PositiveNumberFilter.class)
    public double getPageRank() {
        return pageRank;
    }

    
    @JsonSetter

    public void setPageRank(double pageRank) {
        this.pageRank = pageRank;
    }
    
    @JsonGetter
    @JacksonXmlProperty
    public int getRecordCount() {
        return recordCount;
    }
    
    @JsonSetter
    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }
    
    @JsonGetter
    @JacksonXmlProperty
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = PositiveNumberFilter.class)
    public int getScore() {
        return score;
    }
    
    @JsonSetter
    public void setScore(int score) {
        this.score = score;
    }
    
    @JsonGetter(AGGREGATES)
    @JacksonXmlProperty(localName = XmlFields.XML_ORE_AGGREGATES)
    public List<String> getAggregates() {
        return aggregates;
    }
    
    @JsonSetter(AGGREGATES)
    public void setAggregates(List<String> aggregates) {
        this.aggregates = aggregates;
    }


}
