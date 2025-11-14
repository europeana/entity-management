package eu.europeana.entitymanagement.web.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.*;

@XmlRootElement(namespace = NAMESPACE_VCARD, name = XML_HAS_GEO)
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlGeoLocationList {

   @XmlElement(namespace = NAMESPACE_VCARD, name = XML_LOCATION)
   List<XmlLocationImpl> geoLocations;

   public XmlGeoLocationList() {
    // no-arg public constructor
   }

   public XmlGeoLocationList(List<XmlLocationImpl> geoLocations) {
      if (geoLocations != null) {
         this.geoLocations = new ArrayList<>(geoLocations);
      }
   }

   public List<XmlLocationImpl> getVcardHasGeoList() {
      return geoLocations == null ? null : new ArrayList<>(geoLocations);
   }
}