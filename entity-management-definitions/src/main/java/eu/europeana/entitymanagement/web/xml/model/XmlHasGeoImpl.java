package eu.europeana.entitymanagement.web.xml.model;

import eu.europeana.entitymanagement.definitions.model.HasGeo;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_VCARD;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmlHasGeoImpl {

    @XmlElement(namespace = NAMESPACE_VCARD, name = "id")
    private String id;

    @XmlElement(namespace = NAMESPACE_VCARD, name = "type")
    private String type;

    @XmlElement(namespace = NAMESPACE_VCARD, name = "lat")
    private String latitude;

    @XmlElement(namespace = NAMESPACE_VCARD, name = "long")
    private String longitude;


    public XmlHasGeoImpl() {
        // no-arg default constructor
    }

    // TODO add emoty checks
    public XmlHasGeoImpl(HasGeo hasGeo) {
        if (StringUtils.isNotEmpty(hasGeo.getId())) {
            this.id = hasGeo.getId();
        }
        this.type = hasGeo.getType();
        this.latitude = hasGeo.getLatitude();
        this.longitude = hasGeo.getLongitude();
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}