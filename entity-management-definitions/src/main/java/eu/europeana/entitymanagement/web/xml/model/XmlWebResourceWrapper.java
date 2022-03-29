package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_EDM;

import eu.europeana.entitymanagement.definitions.model.WebResource;
import eu.europeana.entitymanagement.utils.EntityUtils;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import org.springframework.util.StringUtils;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmlWebResourceWrapper {

  @XmlElement(namespace = NAMESPACE_EDM, name = XmlConstants.XML_EDM_WEB_RESOURCE)
  private XmlWebResourceImpl webResource;

  public XmlWebResourceImpl getWebResource() {
    return webResource;
  }

  public void setWebResource(XmlWebResourceImpl webResource) {
    this.webResource = webResource;
  }

  public XmlWebResourceWrapper(XmlWebResourceImpl webResource) {
    this.webResource = webResource;
  }

  public XmlWebResourceWrapper() {
    // no arg constructor
  }

  public static XmlWebResourceWrapper fromWebResource(WebResource webResource) {
    if (webResource == null) {
      return null;
    }
    return new XmlWebResourceWrapper(
        new XmlWebResourceImpl(
            webResource.getId(), webResource.getSource(), webResource.getThumbnail()));
  }

  public static WebResource toWebResource(XmlWebResourceWrapper xmlWebResourceWrapper) {
    // id is mandatory for web resources
    if (xmlWebResourceWrapper == null
        || xmlWebResourceWrapper.getWebResource() == null
        || xmlWebResourceWrapper.getWebResource().getAbout() == null) {
      return null;
    }

    WebResource webResource = new WebResource();
    webResource.setId(xmlWebResourceWrapper.getWebResource().getAbout());

    LabelledResource source = xmlWebResourceWrapper.getWebResource().getSource();
    if (source != null && StringUtils.hasLength(source.getResource())) {
      webResource.setSource(source.getResource());
    } else {
      webResource.setSource(EntityUtils.createWikimediaResourceString(webResource.getId()));
    }

    LabelledResource thumbnail = xmlWebResourceWrapper.getWebResource().getThumbnail();
    if (thumbnail != null) {
      webResource.setThumbnail(thumbnail.getResource());
    }

    return webResource;
  }
}
