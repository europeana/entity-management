package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_RDF;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_VCARD;

import eu.europeana.entitymanagement.definitions.model.Address;
import eu.europeana.entitymanagement.utils.EntityUtils;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang3.StringUtils;

@XmlRootElement(namespace = NAMESPACE_VCARD, name = XmlConstants.XML_ADDRESS)
// This throws an error because of fieldname - xml property mismatch.
// TODO: Fix and uncomment
// @XmlType(propOrder={XmlConstants.XML_STREET_ADDRESS, XmlConstants.XML_POSTAL_CODE,
// XmlConstants.XML_POST_OFFICE_BOX,
//    	XmlConstants.XML_LOCALITY, XmlConstants.XML_REGION, XmlConstants.XML_COUNTRY_NAME,
// XmlConstants.XML_HAS_GEO})
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlAddressImpl {

  @XmlAttribute(namespace = NAMESPACE_RDF, name = XmlConstants.ABOUT)
  private String about;

  @XmlElement(namespace = NAMESPACE_VCARD, name = XmlConstants.XML_STREET_ADDRESS)
  private String streetAddress;

  @XmlElement(namespace = NAMESPACE_VCARD, name = XmlConstants.XML_POSTAL_CODE)
  private String postalCode;

  @XmlElement(namespace = NAMESPACE_VCARD, name = XmlConstants.XML_POST_OFFICE_BOX)
  private String postBox;

  @XmlElement(namespace = NAMESPACE_VCARD, name = XmlConstants.XML_LOCALITY)
  private String locality;

  @XmlElement(namespace = NAMESPACE_VCARD, name = XmlConstants.XML_COUNTRY_NAME)
  private String countryName;

  @XmlElement(namespace = NAMESPACE_VCARD, name = XmlConstants.XML_HAS_GEO)
  private LabelledResource hasGeo;

  public XmlAddressImpl() {
    // no-arg default constructor
  }

  public XmlAddressImpl(Address address) {
    if (StringUtils.isNotEmpty(address.getAbout())) {
      this.about = address.getAbout();
    }
    this.streetAddress = address.getVcardStreetAddress();
    this.locality = address.getVcardLocality();
    this.countryName = address.getVcardCountryName();
    this.postalCode = address.getVcardPostalCode();
    this.postBox = address.getVcardPostOfficeBox();
    this.locality = address.getVcardLocality();

    if (StringUtils.isNotEmpty(address.getVcardHasGeo())) {
      this.hasGeo = new LabelledResource(EntityUtils.toGeoUri(address.getVcardHasGeo()));
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

  public String getCountryName() {
    return countryName;
  }

  public LabelledResource getHasGeo() {
    return hasGeo;
  }

  public Address toAddress() {
    Address address = new Address();
    address.setAbout(about);
    address.setVcardStreetAddress(streetAddress);
    address.setVcardPostalCode(postalCode);
    address.setVcardPostOfficeBox(postBox);
    address.setVcardLocality(locality);
    address.setVcardCountryName(countryName);
    if (hasGeo != null) {
      address.setVcardHasGeo(hasGeo.getResource());
    }

    return address;
  }

  /** Checks that this Address metadata properties set. 'about' field not included in this check. */
  public boolean hasMetadataProperties() {
    return StringUtils.isNotEmpty(streetAddress)
        || StringUtils.isNotEmpty(postalCode)
        || StringUtils.isNotEmpty(postBox)
        || StringUtils.isNotEmpty(locality)
        || StringUtils.isNotEmpty(countryName)
        || hasGeo != null;
  }
}
