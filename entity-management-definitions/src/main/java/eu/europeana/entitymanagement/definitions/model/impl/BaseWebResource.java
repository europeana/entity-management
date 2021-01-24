package eu.europeana.entitymanagement.definitions.model.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import eu.europeana.entitymanagement.definitions.model.WebResource;
import eu.europeana.entitymanagement.definitions.model.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.definitions.model.vocabulary.XmlFields;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class BaseWebResource implements WebResource{

	String source;
	String id;
	String thumbnail;
	String type;

	@JsonProperty(WebEntityFields.ID)
	@JacksonXmlProperty(isAttribute= true, localName = XmlFields.XML_RDF_ABOUT)
	public String getId() {
		return id;
	}
	
	@JsonProperty(WebEntityFields.SOURCE)
	@JacksonXmlProperty(localName = XmlFields.XML_DC_SOURCE)
	public String getSource() {
		return source;
	}
	
	@JsonProperty(WebEntityFields.THUMBNAIL)
	@JacksonXmlProperty(localName = XmlFields.XML_FOAF_THUMBNAIL)
	public String getThumbnail() {
		return thumbnail;
	}
	
	@Override
	@JsonProperty(WebEntityFields.TYPE)
	@JacksonXmlProperty(localName = XmlFields.XML_RDF_TYPE)
	public String getType() {
		return type;
	}

	@Override
	public void setThumbnail(String thumbnailParam) {
		thumbnail=thumbnailParam;		
	}

	@Override
	public void setSource(String sourceParam) {
		source=sourceParam;
	}

	@Override
	public void setType(String typeParam) {
		type=typeParam;
	}

	@Override
	public void setId(String idParam) {
		id=idParam;
	}

}
