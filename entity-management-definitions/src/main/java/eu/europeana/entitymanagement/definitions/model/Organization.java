package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ACRONYM;
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
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.HAS_ADDRESS;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.HIDDEN_LABEL;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ID;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.IDENTIFIER;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.LANGUAGE;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.PREF_LABEL;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.SAME_AS;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.TYPE;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import dev.morphia.annotations.Reference;
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
  COUNTRY,
  LANGUAGE,
  FOAF_HOMEPAGE,
  FOAF_PHONE,
  FOAF_MBOX,
  HAS_ADDRESS,
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
  
  @Reference(lazy = true)
  private EntityRecord countryRef;
  private String countryId;
  @Transient
  private Place country;
  
  private List<String> europeanaRoleIds;
  @Reference(lazy = true)
  private List<Vocabulary> europeanaRoleRefs;
  @Transient
  private List<Vocabulary> europeanaRole;
  
  private Address hasAddress;
  private List<String> sameAs;
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
    //because the europeanaRoleRef is a reference to the object we keep it the same (therefore also for europeanaRole)
    this.europeanaRoleRefs = copy.getEuropeanaRoleRefs();
    if(copy.getEuropeanaRoleIds()!=null) this.europeanaRoleIds=new ArrayList<>(copy.getEuropeanaRoleIds());
    //because the countryRef is a reference to the object we keep it the same (therefore also for country)
    this.countryRef=copy.getCountryRef();
    this.countryId = copy.getCountryId();
    this.country = copy.getCountry();
    
    if (copy.getAddress() != null) this.hasAddress = new Address(copy.getAddress());
    if (copy.sameAs != null) this.sameAs = (new ArrayList<>(copy.sameAs));
    if (copy.language != null) this.language = (new ArrayList<>(copy.language));
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

  @JsonGetter(HAS_ADDRESS)
  public Address getAddress() {
    return hasAddress;
  }

  @JsonSetter(HAS_ADDRESS)
  public void setAddress(Address hasAddress) {
    this.hasAddress = hasAddress;
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
  public Object getFieldValue(Field field) throws IllegalAccessException {
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
      throws IllegalAccessException {
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
  
  @JsonIgnore
  public EntityRecord getCountryRef() {
    return countryRef;
  }
  
  public void setCountryRef(EntityRecord countryRef) {
    this.countryRef=countryRef;
  }

  @JsonGetter(COUNTRY)
  public Place getCountry() {
    if(country == null && getCountryId() != null) {
      //set country if not dereferenced during retrieval from database
      country = new Place(getCountryId());
    }
    return country;
  }

  @JsonSetter(COUNTRY)
  public void setCountry(Place country) {
    this.country = country;
  }

  @JsonIgnore
  public String getCountryId() {
    return countryId;
  }

  public void setCountryId(String countryId) {
    this.countryId = countryId;
  }

  @JsonIgnore
  public List<String> getEuropeanaRoleIds() {
    return europeanaRoleIds;
  }

  public void setEuropeanaRoleIds(List<String> europeanaRoleIds) {
    this.europeanaRoleIds = europeanaRoleIds;
  }

  @JsonIgnore
  public List<Vocabulary> getEuropeanaRoleRefs() {
    return europeanaRoleRefs;
  }

  public void setEuropeanaRoleRefs(List<Vocabulary> europeanaRoleRefs) {
    this.europeanaRoleRefs = europeanaRoleRefs;
  }

  @JsonGetter(EUROPEANA_ROLE)
  public List<Vocabulary> getEuropeanaRole() {
    if(europeanaRole==null && europeanaRoleIds!=null && !europeanaRoleIds.isEmpty()) {
      europeanaRole=new ArrayList<>();
      for(String roleId : europeanaRoleIds) {
        Vocabulary vocab = new Vocabulary();
        vocab.setUri(roleId);
        europeanaRole.add(vocab);
      }
    }
    return europeanaRole;
  }

  @JsonSetter(EUROPEANA_ROLE)
  public void setEuropeanaRole(List<Vocabulary> europeanaRole) {
    this.europeanaRole = europeanaRole;
  }
  
}
