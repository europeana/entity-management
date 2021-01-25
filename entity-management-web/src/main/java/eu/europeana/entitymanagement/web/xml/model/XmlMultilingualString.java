package eu.europeana.entitymanagement.web.xml.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import eu.europeana.entitymanagement.web.xml.XmlStringSerializer;

public class XmlMultilingualString {

    private String value;

    private String language;

    public XmlMultilingualString() {
        // default constructor
    }

    public XmlMultilingualString(String value, String language) {
        this.value = value;
        this.language = language;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JacksonXmlProperty(isAttribute = true, namespace = XmlConstants.XML, localName = XmlConstants.LANG)
    public String getLanguage() {
        return language;
    }

    @JacksonXmlText
    @JsonSerialize(using = XmlStringSerializer.class)
    public String getValue() {
        return value;
    }
}
