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
public class XmlHasGeo {

   @XmlElement(namespace = NAMESPACE_VCARD, name = XML_LOCATION)
   List<XmlLocationImpl> vcardHasGeoList;

   public XmlHasGeo() {
    // no-arg public constructor
   }

   public XmlHasGeo(List<XmlLocationImpl> vcardHasGeoList) {
      if (vcardHasGeoList != null) {
         this.vcardHasGeoList = new ArrayList<>(vcardHasGeoList);
      }
   }

   public List<XmlLocationImpl> getVcardHasGeoList() {
      return vcardHasGeoList == null ? null : new ArrayList<>(vcardHasGeoList);
   }
}