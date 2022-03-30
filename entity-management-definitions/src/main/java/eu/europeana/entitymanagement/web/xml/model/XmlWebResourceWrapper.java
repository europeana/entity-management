package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_EDM;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import org.springframework.util.StringUtils;
import eu.europeana.entitymanagement.definitions.model.WebResource;
import eu.europeana.entitymanagement.utils.EntityUtils;

@XmlAccessorType(XmlAccessType.FIELD)
/**
 * Class used for serialization and deserialization of elements with embedded  web resources
 * @author GordeaS
 *
 */
public class XmlWebResourceWrapper {

  @XmlElement(namespace = NAMESPACE_EDM, name = XmlConstants.XML_EDM_WEB_RESOURCE)
  private XmlWebResourceImpl webResource;

  /**
   * Constructor using the embedded web resource
   * @param webResource
   */
  public XmlWebResourceWrapper(XmlWebResourceImpl webResource) {
    this.webResource = webResource;
  }

  /**
   * Default no arg constructor 
   */
  public XmlWebResourceWrapper() {
    //no arg constructor
  }

  /**
   * getter method for embedded web resource
   * @return - the embedded XML web resource
   */
  public XmlWebResourceImpl getWebResource() {
    return webResource;
  }

  /**
   * setter method for embedded web resource
   * @param webResource - the embedded XML web resource
   */
  public void setWebResource(XmlWebResourceImpl webResource) {
    this.webResource = webResource;
  }

  /**
   * Utility method to build the wrapper for a web resource (data model)  
   * @param webResource
   * @return
   */
  public static XmlWebResourceWrapper fromWebResource(WebResource webResource) {
    if (webResource == null) {
      return null;
    }
    return new XmlWebResourceWrapper(new XmlWebResourceImpl(
        webResource.getId(), webResource.getSource(), webResource.getThumbnail()));
  }
  

  /**
   * utility method to extract the web resource (data model), from the deserialized wrapper element
   * @param xmlWebResourceWrapper
   * @return
   */
  public static WebResource toWebResource(XmlWebResourceWrapper xmlWebResourceWrapper) {
    //id is mandatory for web resources
    if(xmlWebResourceWrapper == null || xmlWebResourceWrapper.getWebResource() == null || xmlWebResourceWrapper.getWebResource().getAbout() == null) {
      return null;
    }
      
    WebResource webResource = new WebResource();
    webResource.setId(xmlWebResourceWrapper.getWebResource().getAbout());

    LabelledResource source = xmlWebResourceWrapper.getWebResource().getSource();
    if (source != null
        && StringUtils.hasLength(source.getResource())) {
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


