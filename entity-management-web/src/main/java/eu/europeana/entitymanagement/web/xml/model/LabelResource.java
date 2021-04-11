package eu.europeana.entitymanagement.web.xml.model;


import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class LabelResource {


  private String lang;

  private String value;

  private String resource;

  public LabelResource(String lang, String value) {
    this.lang = lang;
    this.value = value;
  }

  public LabelResource(String resource) {
    this.resource = resource;
  }

  public LabelResource() {
  }

  @XmlAttribute(name = XmlConstants.RESOURCE, namespace = XmlConstants.NAMESPACE_RDF)
  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  @XmlValue
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @XmlAttribute(name= XmlConstants.LANG, namespace=javax.xml.XMLConstants.XML_NS_URI)
  public String getLang() {
    return lang;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }
}
