package eu.europeana.entitymanagement.web.xml.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import eu.europeana.entitymanagement.utils.EntityUtils;


public class RdfDepiction {
    
    @JsonIgnore
    private String about;
    private String resource;
    
    public RdfDepiction(String about) {
	this.about = about;
	resource = EntityUtils.createWikimediaResourceString(about);
    }
    
    @JacksonXmlProperty(isAttribute=true, localName=XmlConstants.ABOUT)
    public String getAbout() {
	return about;
    }
    
    @JacksonXmlProperty(localName=XmlConstants.XML_DC_SOURCE)
    public LabelledResource getResource() {
	return new LabelledResource(resource);
    }
}
