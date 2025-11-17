package eu.europeana.entitymanagement.web.xml.model;

import eu.europeana.entitymanagement.definitions.model.GeoLocation;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.*;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.*;

@XmlRootElement(namespace = NAMESPACE_VCARD, name = XML_LOCATION)
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlLocationImpl {

    @XmlAttribute(namespace = NAMESPACE_RDF, name = XmlConstants.ABOUT)
    private String id;

    @XmlElement(namespace = NAMESPACE_WGS84_POS, name = XmlConstants.XML_WGS84_POS_LAT)
    private String latitude;

    @XmlElement(namespace = NAMESPACE_WGS84_POS, name =  XmlConstants.XML_WGS84_POS_LONG)
    private String longitude;


    public XmlLocationImpl() {
        // no-arg default constructor
    }

    public XmlLocationImpl(GeoLocation hasGeo) {
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

    public GeoLocation toGeo() {
        GeoLocation hasGeo = new GeoLocation();
        hasGeo.setId(id);
        hasGeo.setLatitude(latitude);
        hasGeo.setLongitude(longitude);
        return hasGeo;
    }

    public boolean hasMetadataProperties() {
        return StringUtils.isNotEmpty(id)
                || StringUtils.isNotEmpty(longitude)
                || StringUtils.isNotEmpty(latitude);
    }
}
