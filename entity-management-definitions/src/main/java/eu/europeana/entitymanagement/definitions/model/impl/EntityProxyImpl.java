package eu.europeana.entitymanagement.definitions.model.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonGetter;
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

    @JsonGetter(WebEntityFields.TYPE)
    @JacksonXmlProperty(localName = XmlFields.XML_RDF_TYPE)
    public String getType() {
	return type;
    }

    @JsonSetter(WebEntityFields.TYPE)
    public void setType(String type) {
	this.type = type;
    }

    @JsonGetter(WebEntityFields.ID)
    @JacksonXmlProperty(isAttribute = true, localName = XmlFields.XML_RDF_ABOUT)
    public String getProxyId() {
	return proxyId;
    }

    @JsonSetter(WebEntityFields.ID)
    public void setProxyId(String proxyId) {
	this.proxyId = proxyId;
    }

    @JsonGetter
    @JacksonXmlProperty
    public Entity getEntity() {
	return entity;
    }

    @JsonSetter
    public void setEntity(Entity entity) {
	this.entity = entity;
    }

    @JsonGetter(WebEntityFields.PROXY_FOR)
    @JacksonXmlProperty(localName = XmlFields.XML_ORE_PROXY_FOR)
    public String getProxyFor() {
	return proxyFor;
    }

    @JsonSetter(WebEntityFields.PROXY_FOR)
    public void setProxyFor(String proxyFor) {
	this.proxyFor = proxyFor;
    }

    @JsonGetter(WebEntityFields.PROXY_IN)
    @JacksonXmlProperty(localName = XmlFields.XML_ORE_PROXY_IN)
    public Aggregation getProxyIn() {
	return proxyIn;
    }

    @JsonSetter(WebEntityFields.PROXY_IN)
    public void setProxyIn(Aggregation proxyIn) {
	this.proxyIn = proxyIn;
    }
}
