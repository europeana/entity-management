package eu.europeana.entitymanagement.web.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.*;

@XmlRootElement(namespace = NAMESPACE_VCARD, name = XML_HAS_GEO)
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlGeoLocation {

   @XmlElement(namespace = NAMESPACE_VCARD, name = XML_LOCATION)
   XmlLocationImpl geoLocation;

   public XmlGeoLocation() {
    // no-arg public constructor
   }

   public XmlGeoLocation(XmlLocationImpl xmlLocation) {
      this.geoLocation = xmlLocation;
   }

   public XmlLocationImpl getGeoLocation() {
      return geoLocation;
   }

   public void setGeoLocation(XmlLocationImpl geoLocation) {
      this.geoLocation = geoLocation;
   }
}