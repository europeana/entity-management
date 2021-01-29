package eu.europeana.entitymanagement.definitions.model.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.europeana.entitymanagement.definitions.model.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.definitions.model.vocabulary.XmlFields;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = XmlFields.XML_VCARD_ADDRESS)
public class BaseAddress implements eu.europeana.corelib.definitions.edm.entity.Address {

    private String about;
    private String streetAddress;
    private String postalCode;
    private String postBox;
    private String locality;
    private String countryName;
    private String hasGeo;
    
    
    @Override
    public void setVcardPostOfficeBox(String vcardPostOfficeBox) {
	this.postBox = vcardPostOfficeBox;
    }

    @Override
    @JsonProperty(WebEntityFields.POST_OFFICE_BOX)
    @JacksonXmlProperty(localName = XmlFields.XML_VCARD_POST_OFFICE_BOX)
    public String getVcardPostOfficeBox() {
	return postBox;
    }

    @Override
    public void setVcardCountryName(String vcardCountryName) {
	this.countryName = vcardCountryName;
    }

    @Override
    @JsonProperty(WebEntityFields.COUNTRY_NAME)
    @JacksonXmlProperty(localName = XmlFields.XML_VCARD_COUNTRY_NAME)
    public String getVcardCountryName() {
	return countryName;
    }

    @Override
    public void setVcardPostalCode(String vcardPostalCode) {
	this.postalCode = vcardPostalCode;
    }

    @Override
    @JsonProperty(WebEntityFields.POSTAL_CODE)
    @JacksonXmlProperty(localName = XmlFields.XML_VCARD_POSTAL_CODE)
    public String getVcardPostalCode() {
	return postalCode;
    }

    @Override
    public void setVcardLocality(String vcardLocality) {
	this.locality = vcardLocality;
    }

    @Override
    @JsonProperty(WebEntityFields.LOCALITY)
    @JacksonXmlProperty(localName = XmlFields.XML_VCARD_LOCALITY)
    public String getVcardLocality() {
	return locality;
    }

    @Override
    public void setVcardStreetAddress(String vcardStreetAddress) {
	this.streetAddress = vcardStreetAddress;
    }

    @Override
    @JsonProperty(WebEntityFields.STREET_ADDRESS)
    @JacksonXmlProperty(localName = XmlFields.XML_VCARD_STREET_ADDRESS)
    public String getVcardStreetAddress() {
	return streetAddress;
    }

    
    @Override
    public void setAbout(String about) {
	this.about = about;
    }

    @Override
    @JsonProperty(WebEntityFields.ID)
    @JacksonXmlProperty(isAttribute= true, localName = XmlFields.XML_RDF_ABOUT)
    public String getAbout() {
	return about;
    }

    @Override
    @JsonProperty(WebEntityFields.HAS_GEO)
    @JacksonXmlProperty(localName = XmlFields.XML_VCARD_HAS_GEO)
    public String getVcardHasGeo() {
	return hasGeo;
    }

    @Override
    public void setVcardHasGeo(String hasGeo) {
	this.hasGeo = hasGeo;
    }

}
