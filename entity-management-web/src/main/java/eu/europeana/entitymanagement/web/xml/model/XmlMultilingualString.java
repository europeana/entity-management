package eu.europeana.entitymanagement.web.xml.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import eu.europeana.entitymanagement.web.xml.XmlStringSerializer;

//@XmlRootElement
public class XmlMultilingualString {

    @XmlValue
    private String value;

    @XmlAttribute(name= XmlConstants.LANG, namespace=javax.xml.XMLConstants.XML_NS_URI)
    private String language = "";

    public XmlMultilingualString() {
        // default constructor
	super();
    }

    public XmlMultilingualString(String value, String language) {
        this.value = value;
        if(language != null) {
            this.language = language;
        } else {
            //fix for #EA-2325, missing language attributions changed to ""
	    this.language = "";  
        }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
//    @JacksonXmlProperty(isAttribute = true, namespace = XmlConstants.XML, localName = XmlConstants.LANG)
//    @XmlAttribute(name = XmlConstants.LANG, namespace = "@XmlAttribute(name=\"lang\", namespace=\"http://www.w3.org/XML/1998/namespace\")")
    public String getLanguage() {
        if(language == null) {
            //fix for #EA-2325, missing language attributions changed to ""
	    return "";
        }
	return language;
    }

    @JacksonXmlText
    @JsonSerialize(using = XmlStringSerializer.class)
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return String.format("{lang: %s, value: %s}", getLanguage(), getValue());
    }

}
