package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonGetter;

import dev.morphia.annotations.Embedded;

@Embedded
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
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
    public String getVcardPostOfficeBox() {
        return postBox;
    }

    
    @JsonSetter(COUNTRY_NAME)
    public void setVcardCountryName(String vcardCountryName) {
        this.countryName = vcardCountryName;
    }

    
    @JsonGetter(COUNTRY_NAME)
    public String getVcardCountryName() {
        return countryName;
    }

    
    @JsonSetter(POSTAL_CODE)
    public void setVcardPostalCode(String vcardPostalCode) {
        this.postalCode = vcardPostalCode;
    }

    
    @JsonGetter(POSTAL_CODE)
    public String getVcardPostalCode() {
        return postalCode;
    }

    
    @JsonSetter(LOCALITY)
    public void setVcardLocality(String vcardLocality) {
        this.locality = vcardLocality;
    }

    
    @JsonGetter(LOCALITY)
    public String getVcardLocality() {
        return locality;
    }

    
    @JsonSetter(STREET_ADDRESS)
    public void setVcardStreetAddress(String vcardStreetAddress) {
        this.streetAddress = vcardStreetAddress;
    }

    
    @JsonGetter(STREET_ADDRESS)
    public String getVcardStreetAddress() {
        return streetAddress;
    }


    
    @JsonSetter(ID)
    public void setAbout(String about) {
        this.about = about;
    }

    
    @JsonGetter(ID)
    public String getAbout() {
        return about;
    }

    
    @JsonGetter(HAS_GEO)
    public String getVcardHasGeo() {
        return hasGeo;
    }

    
    @JsonSetter(HAS_GEO)
    public void setVcardHasGeo(String hasGeo) {
        this.hasGeo = hasGeo;
    }

}
