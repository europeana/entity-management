package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.CONTEXT;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.COUNTRY_NAME;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.HAS_GEO;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ID;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.LOCALITY;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.POSTAL_CODE;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.POST_OFFICE_BOX;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.REGION;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.STREET_ADDRESS;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.TYPE;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import dev.morphia.annotations.Embedded;

@Embedded
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
  CONTEXT,
  ID,
  TYPE,
  STREET_ADDRESS,
  POSTAL_CODE,
  POST_OFFICE_BOX,
  LOCALITY,
  REGION,
  COUNTRY_NAME,
  HAS_GEO
})
public class Address {

  public Address() {
    super();
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

  /** Checks that this Address metadata properties set. 'about' field not included in this check. */
  public boolean hasMetadataProperties() {
    return StringUtils.isNotEmpty(streetAddress)
        || StringUtils.isNotEmpty(postalCode)
        || StringUtils.isNotEmpty(postBox)
        || StringUtils.isNotEmpty(locality)
        || StringUtils.isNotEmpty(countryName)
        || StringUtils.isNotEmpty(hasGeo);
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Address that = (Address) o;
    
    if (Objects.equals(about, that.getAbout()) &&
        Objects.equals(streetAddress, that.getVcardStreetAddress()) &&
        Objects.equals(postalCode, that.getVcardPostalCode()) &&
        Objects.equals(postBox, that.getVcardPostOfficeBox()) &&
        Objects.equals(locality, that.getVcardLocality()) &&
        Objects.equals(countryName, that.getVcardCountryName()) &&
        Objects.equals(hasGeo, that.getVcardHasGeo())) {
      return true;
    }
    else {
      return false;
    }
  }

  public int hashCode() {
    return ((streetAddress == null) ? 0 : streetAddress.hashCode()) +
        ((locality == null) ? 0 : locality.hashCode()) +
        ((hasGeo == null) ? 0 : hasGeo.hashCode());
  }
  
}
