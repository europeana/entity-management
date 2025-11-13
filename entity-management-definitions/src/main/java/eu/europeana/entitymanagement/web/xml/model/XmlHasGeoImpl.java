package eu.europeana.entitymanagement.web.xml.model;

import eu.europeana.entitymanagement.definitions.model.HasGeo;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_RDF;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_VCARD;

@XmlRootElement(namespace = NAMESPACE_VCARD, name = XmlConstants.XML_HAS_GEO)
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlHasGeoImpl {

    @XmlElement(namespace = NAMESPACE_RDF, name = XmlConstants.ABOUT)
    private String id;

    @XmlElement(namespace = NAMESPACE_VCARD, name = XmlConstants.XML_WGS84_POS_LAT)
    private String latitude;

    @XmlElement(namespace = NAMESPACE_VCARD, name =  XmlConstants.XML_WGS84_POS_LONG)
    private String longitude;


    public XmlHasGeoImpl() {
        // no-arg default constructor
    }

    public XmlHasGeoImpl(HasGeo hasGeo) {
        if (StringUtils.isNotEmpty(hasGeo.getId())) {
            this.id = hasGeo.getId();
        }
        this.latitude = hasGeo.getLatitude();
        this.longitude = hasGeo.getLongitude();
    }

    public String getId() {
        return id;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}