package eu.europeana.entitymanagement.web.xml.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = XmlConstants.XML_EDM_WEB_RESOURCE)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({XmlConstants.ABOUT, XmlConstants.XML_DC_SOURCE, XmlConstants.XML_THUMBNAIL})
public class XmlWebResourceImpl {

    	@JsonIgnore
    	String source;
    	@JsonIgnore
    	String about;
    	@JsonIgnore
    	String thumbnail;
    	
    	public XmlWebResourceImpl(String about, String source, String thumbnail) {
    	    this.about = about;
    	    this.source = source;
    	    this.thumbnail = thumbnail;
    	}
    	
	@JacksonXmlProperty(isAttribute= true, localName = XmlConstants.ABOUT)
	public String getAbout() {
		return about;
	}

	@JacksonXmlProperty(localName = XmlConstants.XML_DC_SOURCE)
	public LabelResource getSource() {
		return new LabelResource(source);
	}

	@JacksonXmlProperty(localName = XmlConstants.XML_THUMBNAIL)
	public LabelResource getThumbnail() {
		return new LabelResource(thumbnail);
	}

}
