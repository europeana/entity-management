package eu.europeana.entitymanagement.web.xml.model;

import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.utils.EntityUtils;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.springframework.util.StringUtils;

@XmlRootElement(name = XmlConstants.XML_ADDRESS)
@XmlType(propOrder={XmlConstants.XML_STREET_ADDRESS, XmlConstants.XML_POSTAL_CODE, XmlConstants.XML_POST_OFFICE_BOX,
    	XmlConstants.XML_LOCALITY, XmlConstants.XML_REGION, XmlConstants.XML_COUNTRY_NAME, XmlConstants.XML_HAS_GEO})
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlAddressImpl {

  @XmlAttribute(name = XmlConstants.ABOUT)
  private String about;

  @XmlAttribute(name = XmlConstants.XML_STREET_ADDRESS)
  private String streetAddress;

  @XmlAttribute(name = XmlConstants.XML_POSTAL_CODE)
  private String postalCode;

  @XmlAttribute(name = XmlConstants.XML_POST_OFFICE_BOX)
  private String postBox;

  @XmlAttribute(name = XmlConstants.XML_LOCALITY)
  private String locality;

  @XmlAttribute(name = XmlConstants.XML_REGION)
  private String region;

  @XmlAttribute(name = XmlConstants.XML_COUNTRY_NAME)
  private String countryName;

  @XmlAttribute(name = XmlConstants.XML_HAS_GEO)
  private LabelledResource hasGeo;
    	
    	
    	public XmlAddressImpl(Organization organization) {
        this.about = organization.getHasAddress();
        this.streetAddress = organization.getStreetAddress();
        this.locality = organization.getLocality();
        this.countryName = organization.getCountryName();
        this.postalCode = organization.getPostalCode();
        this.postBox = organization.getPostBox();
        this.locality = organization.getLocality();
        this.region = organization.getRegion();

        if(StringUtils.hasLength(organization.getHasGeo())){
          this.hasGeo = new LabelledResource(EntityUtils.toGeoUri(organization.getHasGeo()));
        }
    	}
    	

	public String getAbout() {
		return about;
	}

  public String getStreetAddress() {
    return streetAddress;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public String getPostBox() {
    return postBox;
  }

  public String getLocality() {
    return locality;
  }

  public String getRegion() {
    return region;
  }

  public String getCountryName() {
    return countryName;
  }

  public LabelledResource getHasGeo() {
    return hasGeo;
  }
}
