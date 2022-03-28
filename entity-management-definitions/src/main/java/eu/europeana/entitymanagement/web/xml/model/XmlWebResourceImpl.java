package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_DC;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_EDM;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_FOAF;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_RDF;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.springframework.util.StringUtils;

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
    if (thumbnail != null) {
      this.thumbnail = new LabelledResource(thumbnail);
    }
    if (source != null) {
      this.source = new LabelledResource(source);
    }
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

  public boolean isEmpty() {
    return !StringUtils.hasLength(about) && (source == null) && (thumbnail == null);
  }

}
