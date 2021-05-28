package eu.europeana.entitymanagement.web.xml.model;

import eu.europeana.api.commons.definitions.utils.DateUtils;
import java.util.Date;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

public class RdfTypedElement {

  @XmlTransient
    private String value;
    @XmlTransient
    private String type;
    
    public RdfTypedElement(Date date) {
	this.value = DateUtils.convertDateToStr(date);
      this.type = "http://www.w3.org/2001/XMLSchema#dateTime";
    }
    
    @XmlAttribute(name= XmlConstants.XML_DATATYPE)
    public String getType() {
	return type;
    }
    
    @XmlValue
    public String getValue() {
	return value;
    }
}
