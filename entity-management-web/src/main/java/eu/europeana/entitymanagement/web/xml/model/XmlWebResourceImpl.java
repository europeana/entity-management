package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_DC;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_EDM;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_FOAF;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_RDF;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(namespace = NAMESPACE_EDM, name = XmlConstants.XML_EDM_WEB_RESOURCE)
@XmlType(propOrder={XmlConstants.ABOUT, XmlConstants.XML_DC_SOURCE, XmlConstants.XML_THUMBNAIL})
public class XmlWebResourceImpl {

			@XmlElement(namespace = NAMESPACE_DC, name = XmlConstants.XML_DC_SOURCE)
			private LabelledResource source;

			@XmlAttribute(namespace = NAMESPACE_RDF, name = XmlConstants.ABOUT)
    	private String about;

			@XmlAttribute(namespace = NAMESPACE_FOAF, name = XmlConstants.XML_THUMBNAIL)
			private LabelledResource thumbnail;
    	
    	public XmlWebResourceImpl(String about, String source, String thumbnail) {
    	    this.about = about;
    	    this.source = new LabelledResource(source);
    	    this.thumbnail = new LabelledResource(thumbnail);
    	}
    	
	public String getAbout() {
		return about;
	}

	public LabelledResource getSource() {
		return source;
	}

	public LabelledResource getThumbnail() {
		return thumbnail;
	}

}
