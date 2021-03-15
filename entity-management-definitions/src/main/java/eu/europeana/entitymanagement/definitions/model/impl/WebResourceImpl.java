package eu.europeana.entitymanagement.definitions.model.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import eu.europeana.entitymanagement.definitions.model.WebResource;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.vocabulary.XmlFields;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class WebResourceImpl implements WebResource{

	public WebResourceImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

	public WebResourceImpl(WebResource copy) {
		super();
		this.source = copy.getSource();
		this.id = copy.getId();
		this.thumbnail = copy.getThumbnail();
		this.type = copy.getType();
	}

	String source;
	String id;
	String thumbnail;
	String type;

	@JsonGetter(WebEntityFields.ID)
	@JacksonXmlProperty(isAttribute= true, localName = XmlFields.XML_RDF_ABOUT)
	public String getId() {
		return id;
	}
	
	@JsonGetter(WebEntityFields.SOURCE)
	@JacksonXmlProperty(localName = XmlFields.XML_DC_SOURCE)
	public String getSource() {
		return source;
	}
	
	@JsonGetter(WebEntityFields.THUMBNAIL)
	@JacksonXmlProperty(localName = XmlFields.XML_FOAF_THUMBNAIL)
	public String getThumbnail() {
		return thumbnail;
	}
	
	@Override
	@JsonGetter(WebEntityFields.TYPE)
	@JacksonXmlProperty(localName = XmlFields.XML_RDF_TYPE)
	public String getType() {
		return type;
	}

	@Override
	@JsonSetter(WebEntityFields.THUMBNAIL)
	public void setThumbnail(String thumbnailParam) {
		thumbnail=thumbnailParam;		
	}

	@Override
	@JsonSetter(WebEntityFields.SOURCE)
	public void setSource(String sourceParam) {
		source=sourceParam;
	}

	@Override
	@JsonSetter(WebEntityFields.TYPE)
	public void setType(String typeParam) {
		type=typeParam;
	}

	@Override
	@JsonSetter(WebEntityFields.ID)
	public void setId(String idParam) {
		id=idParam;
	}

}
