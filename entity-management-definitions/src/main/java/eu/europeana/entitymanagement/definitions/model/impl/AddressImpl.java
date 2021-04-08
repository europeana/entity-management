package eu.europeana.entitymanagement.definitions.model.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.europeana.entitymanagement.definitions.model.Address;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.vocabulary.XmlFields;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = XmlFields.XML_VCARD_ADDRESS)

@JsonPropertyOrder({WebEntityFields.STREET_ADDRESS, WebEntityFields.POSTAL_CODE, WebEntityFields.POST_OFFICE_BOX,
	WebEntityFields.LOCALITY, WebEntityFields.REGION, WebEntityFields.COUNTRY_NAME, WebEntityFields.HAS_GEO})

public class AddressImpl implements Address, eu.europeana.corelib.definitions.edm.entity.Address {

    public AddressImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

	public AddressImpl(Address copy) {
		super();
		this.about = copy.getAbout();
		this.streetAddress = copy.getVcardStreetAddress();
		this.postalCode = copy.getVcardPostalCode();
		this.postBox = copy.getVcardPostOfficeBox();
		this.locality = copy.getVcardLocality();
		this.countryName = copy.getVcardCountryName();
		this.hasGeo = copy.getVcardHasGeo();
	}

	private String about;
    private String streetAddress;
    private String postalCode;
    private String postBox;
    private String locality;
    private String countryName;
    private String hasGeo;
    
    @Override
    @JsonSetter(WebEntityFields.POST_OFFICE_BOX)
    public void setVcardPostOfficeBox(String vcardPostOfficeBox) {
	this.postBox = vcardPostOfficeBox;
    }

    @Override
    @JsonGetter(WebEntityFields.POST_OFFICE_BOX)
    @JacksonXmlProperty(localName = XmlFields.XML_VCARD_POST_OFFICE_BOX)
    public String getVcardPostOfficeBox() {
	return postBox;
    }

    @Override
    @JsonSetter(WebEntityFields.COUNTRY_NAME)
    public void setVcardCountryName(String vcardCountryName) {
	this.countryName = vcardCountryName;
    }

    @Override
    @JsonGetter(WebEntityFields.COUNTRY_NAME)
    @JacksonXmlProperty(localName = XmlFields.XML_VCARD_COUNTRY_NAME)
    public String getVcardCountryName() {
	return countryName;
    }

    @Override
    @JsonSetter(WebEntityFields.POSTAL_CODE)
    public void setVcardPostalCode(String vcardPostalCode) {
	this.postalCode = vcardPostalCode;
    }

    @Override
    @JsonGetter(WebEntityFields.POSTAL_CODE)
    @JacksonXmlProperty(localName = XmlFields.XML_VCARD_POSTAL_CODE)
    public String getVcardPostalCode() {
	return postalCode;
    }

    @Override
    @JsonSetter(WebEntityFields.LOCALITY)
    public void setVcardLocality(String vcardLocality) {
	this.locality = vcardLocality;
    }

    @Override
    @JsonGetter(WebEntityFields.LOCALITY)
    @JacksonXmlProperty(localName = XmlFields.XML_VCARD_LOCALITY)
    public String getVcardLocality() {
	return locality;
    }

    @Override
    @JsonSetter(WebEntityFields.STREET_ADDRESS)
    public void setVcardStreetAddress(String vcardStreetAddress) {
	this.streetAddress = vcardStreetAddress;
    }

    @Override
    @JsonGetter(WebEntityFields.STREET_ADDRESS)
    @JacksonXmlProperty(localName = XmlFields.XML_VCARD_STREET_ADDRESS)
    public String getVcardStreetAddress() {
	return streetAddress;
    }

    
    @Override
    @JsonSetter(WebEntityFields.ID)
    public void setAbout(String about) {
	this.about = about;
    }

    @Override
    @JsonGetter(WebEntityFields.ID)
    @JacksonXmlProperty(isAttribute= true, localName = XmlFields.XML_RDF_ABOUT)
    public String getAbout() {
	return about;
    }

    @Override
    @JsonGetter(WebEntityFields.HAS_GEO)
    @JacksonXmlProperty(localName = XmlFields.XML_VCARD_HAS_GEO)
    public String getVcardHasGeo() {
	return hasGeo;
    }

    @Override
    @JsonSetter(WebEntityFields.HAS_GEO)
    public void setVcardHasGeo(String hasGeo) {
	this.hasGeo = hasGeo;
    }
    


}
