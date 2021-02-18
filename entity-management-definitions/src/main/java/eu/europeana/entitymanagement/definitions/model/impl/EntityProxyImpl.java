package eu.europeana.entitymanagement.definitions.model.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.vocabulary.XmlFields;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class EntityProxyImpl implements EntityProxy {

    String proxyId;
    Entity entity;
    String proxyFor;
    Aggregation proxyIn;
    String type;

    @JsonProperty(WebEntityFields.TYPE)
    @JacksonXmlProperty(localName = XmlFields.XML_RDF_TYPE)
    public String getType() {
	return type;
    }

    public void setType(String type) {
	this.type = type;
    }

    @JsonProperty(WebEntityFields.ID)
    @JacksonXmlProperty(isAttribute = true, localName = XmlFields.XML_RDF_ABOUT)
    public String getProxyId() {
	return proxyId;
    }

    public void setProxyId(String proxyId) {
	this.proxyId = proxyId;
    }

    @JsonProperty
    @JacksonXmlProperty
    public Entity getEntity() {
	return entity;
    }

    public void setEntity(Entity entity) {
	this.entity = entity;
    }

    @JsonProperty(WebEntityFields.PROXY_FOR)
    @JacksonXmlProperty(localName = XmlFields.XML_ORE_PROXY_FOR)
    public String getProxyFor() {
	return proxyFor;
    }

    public void setProxyFor(String proxyFor) {
	this.proxyFor = proxyFor;
    }

    @JsonProperty(WebEntityFields.PROXY_IN)
    @JacksonXmlProperty(localName = XmlFields.XML_ORE_PROXY_IN)
    public Aggregation getProxyIn() {
	return proxyIn;
    }

    public void setProxyIn(Aggregation proxyIn) {
	this.proxyIn = proxyIn;
    }
}
