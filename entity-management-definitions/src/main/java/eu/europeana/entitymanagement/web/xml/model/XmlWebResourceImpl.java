package eu.europeana.entitymanagement.web.xml.model;

import eu.europeana.entitymanagement.definitions.model.WebResource;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_DC;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_EDM;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_FOAF;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_RDF;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(namespace = NAMESPACE_EDM, name = XmlConstants.XML_EDM_WEB_RESOURCE)
@XmlType(propOrder = {XmlConstants.ABOUT, XmlConstants.XML_DC_SOURCE, XmlConstants.XML_THUMBNAIL})
public class XmlWebResourceImpl {

    @XmlAttribute(namespace = NAMESPACE_RDF, name = XmlConstants.ABOUT)
    private String about;

    @XmlElement(namespace = NAMESPACE_DC, name = XmlConstants.XML_DC_SOURCE)
    private LabelledResource source;

    @XmlElement(namespace = NAMESPACE_FOAF, name = XmlConstants.XML_THUMBNAIL)
    private LabelledResource thumbnail;

    public XmlWebResourceImpl() {
        // no-arg constructor
    }

    public XmlWebResourceImpl(String about, String source, String thumbnail) {
        this.about = about;
        this.thumbnail = new LabelledResource(thumbnail);
        this.source = new LabelledResource(source);
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

    public static XmlWebResourceImpl fromWebResource(WebResource webResource){
        return new XmlWebResourceImpl(webResource.getId(), webResource.getSource(), webResource.getThumbnail());
    }

    public static WebResource toWebResource(XmlWebResourceImpl xmlWebResource) {
        WebResource webResource = new WebResource();
        webResource.setId(xmlWebResource.about);

        if (xmlWebResource.source != null) {
            webResource.setSource(xmlWebResource.source.getResource());
        }
        if (xmlWebResource.thumbnail != null) {
            webResource.setThumbnail(xmlWebResource.thumbnail.getResource());
        }

        return webResource;
    }

}
