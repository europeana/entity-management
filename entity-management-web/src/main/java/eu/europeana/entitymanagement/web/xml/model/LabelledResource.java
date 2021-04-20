package eu.europeana.entitymanagement.web.xml.model;


import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class LabelledResource {
  private String lang;
  private String value;
  private String resource;

  public LabelledResource(String lang, String value) {
    this.lang = lang;
    if(lang != null) {
        this.lang = lang;
    } else {
        //fix for #EA-2325, missing language attributions changed to ""
	this.lang = "";  
    }
    this.value = value;
  }

  public LabelledResource(String resource) {
    this.resource = resource;
  }

  public LabelledResource() {
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
      if (lang == null && getValue() != null) {
	  // fix for #EA-2325, missing language attributions changed to ""
	  return "";
      }
      return lang;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }
}
