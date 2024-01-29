package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ACRONYM;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.AGGREGATED_VIA;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.AGGREGATES_FROM;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ALT_LABEL;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.CONTEXT;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.COUNTRY;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.DEPICTION;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.DESCRIPTION;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.EUROPEANA_ROLE;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.FOAF_HOMEPAGE;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.FOAF_LOGO;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.FOAF_MBOX;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.FOAF_PHONE;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.GEOGRAPHIC_LEVEL;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.HAS_ADDRESS;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.HIDDEN_LABEL;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ID;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.IDENTIFIER;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.LANGUAGE;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ORGANIZATION_DOMAIN;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.PREF_LABEL;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.SAME_AS;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.TYPE;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import dev.morphia.annotations.Transient;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

/** This class defines base organization type of an entity. */
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
  CONTEXT,
  ID,
  TYPE,
  DEPICTION,
  PREF_LABEL,
  ACRONYM,
  ALT_LABEL,
  HIDDEN_LABEL,
  DESCRIPTION,
  FOAF_LOGO,
  EUROPEANA_ROLE,
  ORGANIZATION_DOMAIN,
  GEOGRAPHIC_LEVEL,
  COUNTRY,
  LANGUAGE,
  FOAF_HOMEPAGE,
  FOAF_PHONE,
  FOAF_MBOX,
  HAS_ADDRESS,
  AGGREGATES_FROM,
  AGGREGATED_VIA,
  IDENTIFIER,
  SAME_AS
})
public class Organization extends Entity {

  private String type = EntityTypes.Organization.getEntityType();
  private Map<String, String> description;
  private Map<String, List<String>> acronym;
  private WebResource logo;
  private String homepage;
  private List<String> phone;
  private List<String> mbox;
  private Map<String, List<String>> europeanaRole;
  private Map<String, List<String>> organizationDomain;
  private Map<String, String> geographicLevel;
  private String country;
  private Address hasAddress;
  private List<String> sameAs; 
  @Transient private List<String> aggregatesFrom;
  private List<String> aggregatedVia;
  private List<String> language;

  public Organization() {
    super();
  }

  public Organization(Organization copy) {
    super(copy);
    if (copy.getDescription() != null) this.description = new HashMap<>(copy.getDescription());
    if (copy.getAcronym() != null) this.acronym = new HashMap<>(copy.getAcronym());
    this.logo = copy.getLogo();
    this.homepage = copy.getHomepage();
    if (copy.getPhone() != null) this.phone = new ArrayList<>(copy.getPhone());
    if (copy.getMbox() != null) this.mbox = new ArrayList<>(copy.getMbox());
    if (copy.getEuropeanaRole() != null)
      this.europeanaRole = new HashMap<>(copy.getEuropeanaRole());
    if (copy.getOrganizationDomain() != null)
      this.organizationDomain = new HashMap<>(copy.getOrganizationDomain());
    if (copy.getGeographicLevel() != null)
      this.geographicLevel = new HashMap<>(copy.getGeographicLevel());
    this.country = copy.getCountry();
    if (copy.getAddress() != null) this.hasAddress = new Address(copy.getAddress());
    if (copy.sameAs != null) this.sameAs = (new ArrayList<>(copy.sameAs));
    if (copy.getLanguage() != null) this.language = (new ArrayList<>(copy.getLanguage()));
    if (copy.getAggregatesFrom() != null) this.aggregatesFrom = (new ArrayList<>(copy.getAggregatesFrom()));
    if (copy.getAggregatedVia() != null) this.aggregatedVia = (new ArrayList<>(copy.getAggregatedVia()));
  }

  @JsonGetter(DESCRIPTION)
  public Map<String, String> getDescription() {
    return description;
  }

  @JsonSetter(DESCRIPTION)
  public void setDescription(Map<String, String> dcDescription) {
    this.description = dcDescription;
  }

  @JsonGetter(ACRONYM)
  public Map<String, List<String>> getAcronym() {
    return acronym;
  }

  @JsonSetter(ACRONYM)
  public void setAcronym(Map<String, List<String>> acronym) {
    this.acronym = acronym;
  }

  @JsonGetter(EUROPEANA_ROLE)
  public Map<String, List<String>> getEuropeanaRole() {
    return europeanaRole;
  }

  @JsonSetter(EUROPEANA_ROLE)
  public void setEuropeanaRole(Map<String, List<String>> europeanaRole) {
    this.europeanaRole = europeanaRole;
  }

  @JsonGetter(FOAF_PHONE)
  public List<String> getPhone() {
    return phone;
  }

  @JsonSetter(FOAF_PHONE)
  public void setPhone(List<String> phone) {
    this.phone = phone;
  }

  @JsonGetter(FOAF_MBOX)
  public List<String> getMbox() {
    return mbox;
  }

  @JsonSetter(FOAF_MBOX)
  public void setMbox(List<String> mbox) {
    this.mbox = mbox;
  }

  @JsonGetter(COUNTRY)
  public String getCountry() {
    return country;
  }

  @JsonSetter(COUNTRY)
  public void setCountry(String country) {
    this.country = country;
  }

  @JsonGetter(HAS_ADDRESS)
  public Address getAddress() {
    return hasAddress;
  }

  @JsonSetter(HAS_ADDRESS)
  public void setAddress(Address hasAddress) {
    this.hasAddress = hasAddress;
  }

  @JsonGetter(GEOGRAPHIC_LEVEL)
  public Map<String, String> getGeographicLevel() {
    return geographicLevel;
  }

  @JsonSetter(GEOGRAPHIC_LEVEL)
  public void setGeographicLevel(Map<String, String> geographicLevel) {
    this.geographicLevel = geographicLevel;
  }

  @JsonGetter(ORGANIZATION_DOMAIN)
  public Map<String, List<String>> getOrganizationDomain() {
    return organizationDomain;
  }

  @JsonSetter(ORGANIZATION_DOMAIN)
  public void setOrganizationDomain(Map<String, List<String>> organizationDomain) {
    this.organizationDomain = organizationDomain;
  }

  @JsonGetter(FOAF_HOMEPAGE)
  public String getHomepage() {
    return homepage;
  }

  @JsonSetter(FOAF_HOMEPAGE)
  public void setHomepage(String homepage) {
    this.homepage = homepage;
  }

  @JsonGetter(FOAF_LOGO)
  public WebResource getLogo() {
    return logo;
  }

  @JsonSetter(FOAF_LOGO)
  public void setLogo(WebResource logo) {
    this.logo = logo;
  }

  public String getType() {
    return type;
  }

  @Override
  public Object getFieldValue(Field field) throws IllegalArgumentException, IllegalAccessException {
    // method to call the getters for each field individually
    return field.get(this);
  }

  @JsonSetter(SAME_AS)
  public void setSameReferenceLinks(List<String> uris) {
    this.sameAs = uris;
  }

  @Override
  @JsonGetter(SAME_AS)
  public List<String> getSameReferenceLinks() {
    return this.sameAs;
  }

  @Override
  public void setFieldValue(Field field, Object value)
      throws IllegalArgumentException, IllegalAccessException {
    // method to call the setter for each field individually
    field.set(this, value);
  }

  @JsonGetter(LANGUAGE)
  public List<String> getLanguage() {
    return language;
  }

  @JsonSetter(LANGUAGE)
  public void setLanguage(List<String> edmLanguage) {
    this.language = edmLanguage;
  }

  @JsonGetter(AGGREGATES_FROM)
  public List<String> getAggregatesFrom() {
    return aggregatesFrom;
  }

  @JsonSetter(AGGREGATES_FROM)
  public void setAggregatesFrom(List<String> aggregatesFrom) {
    this.aggregatesFrom = aggregatesFrom;
  }

  @JsonGetter(AGGREGATED_VIA)
  public List<String> getAggregatedVia() {
    return aggregatedVia;
  }

  @JsonSetter(AGGREGATED_VIA)
  public void setAggregatedVia(List<String> aggregatedVia) {
    this.aggregatedVia = aggregatedVia;
  }
}
