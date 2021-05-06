package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import dev.morphia.annotations.Embedded;
import eu.europeana.entitymanagement.definitions.model.Address;
import eu.europeana.entitymanagement.vocabulary.XmlFields;

@Embedded
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = XmlFields.XML_VCARD_ADDRESS)
@JsonPropertyOrder({CONTEXT, ID, TYPE, STREET_ADDRESS, POSTAL_CODE, POST_OFFICE_BOX, LOCALITY, REGION, COUNTRY_NAME, HAS_GEO})
public class Address {

    public Address() {
        super();
        // TODO Auto-generated constructor stub
    }

    public Address(Address copy) {
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
    
    @JsonSetter(POST_OFFICE_BOX)
    public void setVcardPostOfficeBox(String vcardPostOfficeBox) {
        this.postBox = vcardPostOfficeBox;
    }
    
    @JsonGetter(POST_OFFICE_BOX)
    @JacksonXmlProperty(localName = XmlFields.XML_VCARD_POST_OFFICE_BOX)
    public String getVcardPostOfficeBox() {
        return postBox;
    }

    
    @JsonSetter(COUNTRY_NAME)
    public void setVcardCountryName(String vcardCountryName) {
        this.countryName = vcardCountryName;
    }

    
    @JsonGetter(COUNTRY_NAME)
    @JacksonXmlProperty(localName = XmlFields.XML_VCARD_COUNTRY_NAME)
    public String getVcardCountryName() {
        return countryName;
    }

    
    @JsonSetter(POSTAL_CODE)
    public void setVcardPostalCode(String vcardPostalCode) {
        this.postalCode = vcardPostalCode;
    }

    
    @JsonGetter(POSTAL_CODE)
    @JacksonXmlProperty(localName = XmlFields.XML_VCARD_POSTAL_CODE)
    public String getVcardPostalCode() {
        return postalCode;
    }

    
    @JsonSetter(LOCALITY)
    public void setVcardLocality(String vcardLocality) {
        this.locality = vcardLocality;
    }

    
    @JsonGetter(LOCALITY)
    @JacksonXmlProperty(localName = XmlFields.XML_VCARD_LOCALITY)
    public String getVcardLocality() {
        return locality;
    }

    
    @JsonSetter(STREET_ADDRESS)
    public void setVcardStreetAddress(String vcardStreetAddress) {
        this.streetAddress = vcardStreetAddress;
    }

    
    @JsonGetter(STREET_ADDRESS)
    @JacksonXmlProperty(localName = XmlFields.XML_VCARD_STREET_ADDRESS)
    public String getVcardStreetAddress() {
        return streetAddress;
    }


    
    @JsonSetter(ID)
    public void setAbout(String about) {
        this.about = about;
    }

    
    @JsonGetter(ID)
    @JacksonXmlProperty(isAttribute= true, localName = XmlFields.XML_RDF_ABOUT)
    public String getAbout() {
        return about;
    }

    
    @JsonGetter(HAS_GEO)
    @JacksonXmlProperty(localName = XmlFields.XML_VCARD_HAS_GEO)
    public String getVcardHasGeo() {
        return hasGeo;
    }

    
    @JsonSetter(HAS_GEO)
    public void setVcardHasGeo(String hasGeo) {
        this.hasGeo = hasGeo;
    }

}
