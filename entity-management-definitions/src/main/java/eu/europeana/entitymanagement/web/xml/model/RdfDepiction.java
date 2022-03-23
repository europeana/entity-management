package eu.europeana.entitymanagement.web.xml.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import eu.europeana.entitymanagement.utils.EntityUtils;

@Deprecated
/**
 * @deprecated not use anymore, see {@link XmlWebResourceImpl} and {@link WebResource}
 * @author GordeaS
 *
 */
public class RdfDepiction {

  @XmlTransient private String about;
  private String resource;

  public RdfDepiction(String about) {
    this.about = about;
    resource = EntityUtils.createWikimediaResourceString(about);
  }

  @XmlAttribute(name = XmlConstants.ABOUT)
  public String getAbout() {
    return about;
  }

  @XmlElement(name = XmlConstants.XML_DC_SOURCE)
  public LabelledResource getResource() {
    return new LabelledResource(resource);
  }
}
