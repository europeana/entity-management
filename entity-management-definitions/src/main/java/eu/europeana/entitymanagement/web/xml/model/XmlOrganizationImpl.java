package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.ABOUT;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.ALT_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.DEPICTION;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.HIDDEN_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.IS_AGGREGATED_BY;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.IS_SHOWN_BY;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_DC;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_EDM;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_FOAF;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_VCARD;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.PREF_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_ACRONYM;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_COUNTRY;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_COUNTRY_ID;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_COUNTRY_PLACE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_DESCRIPTION;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_EUROPEANA_ROLE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_HAS_ADDRESS;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_HOMEPAGE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_IDENTIFIER;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_LANGUAGE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_LOGO;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_MBOX;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_ORGANIZATION;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_PHONE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_SAME_AS;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.collections.CollectionUtils;
import eu.europeana.entitymanagement.definitions.exceptions.EntityModelCreationException;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

@XmlRootElement(namespace = NAMESPACE_EDM, name = XML_ORGANIZATION)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    propOrder = {
      ABOUT,
      DEPICTION,
      IS_SHOWN_BY,
      PREF_LABEL,
      ALT_LABEL,
      HIDDEN_LABEL,
      XML_ACRONYM,
      XML_DESCRIPTION,
      XML_LOGO,
      XML_EUROPEANA_ROLE,
      XML_COUNTRY,
      XML_COUNTRY_ID,
      XML_COUNTRY_PLACE,
      XML_LANGUAGE,
      XML_HOMEPAGE,
      XML_PHONE,
      XML_MBOX,
      XML_HAS_ADDRESS,
      XML_IDENTIFIER,
      XML_SAME_AS,
      IS_AGGREGATED_BY
    })
public class XmlOrganizationImpl extends XmlBaseEntityImpl<Organization> {

  @XmlElement(namespace = XmlConstants.NAMESPACE_OWL, name = XmlConstants.XML_SAME_AS)
  private List<LabelledResource> sameAs = new ArrayList<>();

  @XmlElement(namespace = NAMESPACE_EDM, name = XML_ACRONYM)
  private List<LabelledResource> acronym = new ArrayList<>();

  @XmlElement(namespace = NAMESPACE_DC, name = XML_DESCRIPTION)
  private List<LabelledResource> description = new ArrayList<>();

  @XmlElement(namespace = NAMESPACE_FOAF, name = XML_LOGO)
  private XmlWebResourceWrapper logo;

  @XmlElement(namespace = NAMESPACE_EDM, name = XML_EUROPEANA_ROLE)
  private List<LabelledResource> europeanaRole = new ArrayList<>();

  @XmlElement(namespace = NAMESPACE_EDM, name = XML_COUNTRY)
  private String country;

  @XmlElement(namespace = NAMESPACE_EDM, name = XML_COUNTRY_ID)
  private String countryId;

  @XmlElement(namespace = NAMESPACE_EDM, name = XML_COUNTRY_PLACE)
  private XmlPlaceImpl countryPlace;

  @XmlElement(namespace = NAMESPACE_FOAF, name = XML_HOMEPAGE)
  private LabelledResource homepage;

  @XmlElement(namespace = NAMESPACE_FOAF, name = XML_PHONE)
  private List<String> phone;

  @XmlElement(namespace = NAMESPACE_FOAF, name = XML_MBOX)
  private List<String> mbox;

  @XmlElement(namespace = NAMESPACE_VCARD, name = XML_HAS_ADDRESS)
  private XmlAddresses hasAddress;

  @XmlElement(namespace = NAMESPACE_DC, name = XML_IDENTIFIER)
  private List<String> identifier;

  @XmlElement(namespace = NAMESPACE_EDM, name = XML_LANGUAGE)
  private List<String> language;

  public XmlOrganizationImpl(Organization organization) {
    super(organization);
    this.sameAs = RdfXmlUtils.convertToRdfResource(organization.getSameReferenceLinks());
    this.acronym = RdfXmlUtils.convertToXmlMultilingualString(organization.getAcronym());
    this.description = RdfXmlUtils.convertMapToXmlMultilingualString(organization.getDescription());
    if (organization.getLogo() != null) {
      this.logo = XmlWebResourceWrapper.fromWebResource(organization.getLogo());
    }
    this.europeanaRole =
        RdfXmlUtils.convertToXmlMultilingualString(organization.getEuropeanaRole());
    
    this.country = organization.getCountry();
    this.countryId = organization.getCountryId();
    if(organization.getCountryPlace()!=null) {
      this.countryPlace=new XmlPlaceImpl(organization.getCountryPlace());
    }
    
    if (organization.getHomepage() != null) {
      this.homepage = new LabelledResource(organization.getHomepage());
    }
    if (organization.getPhone() != null) {
      this.phone = new ArrayList<String>(organization.getPhone());
    }
    if (organization.getMbox() != null) {
      this.mbox = new ArrayList<String>(organization.getMbox());
    }
    if (organization.getAddress() != null) {
      this.hasAddress = new XmlAddresses(List.of(new XmlAddressImpl(organization.getAddress())));
    }
    if (organization.getIdentifier() != null) {
      this.identifier = new ArrayList<String>(organization.getIdentifier());
    }
    if (organization.getLanguage() != null) {
      this.language = new ArrayList<String>(organization.getLanguage());
    }
  }

  @Override
  public Organization toEntityModel() throws EntityModelCreationException {
    super.toEntityModel();
    entity.setAcronym(RdfXmlUtils.toLanguageMapList(getAcronym()));
    entity.setDescription(RdfXmlUtils.toLanguageMap(getDescription()));
    entity.setLogo(XmlWebResourceWrapper.toWebResource(getLogo()));
    entity.setEuropeanaRole(RdfXmlUtils.toLanguageMapList(getEuropeanaRole()));
    
    entity.setCountry(getCountry());
    entity.setCountryId(getCountryId());
    if(getCountryPlace()!=null) {
      entity.setCountryPlace(getCountryPlace().toEntityModel());
    }
  
    if (getHomepage() != null) {
      entity.setHomepage(getHomepage().getResource());
    }
    entity.setPhone(getPhone());
    entity.setMbox(getMbox());
    if (hasAddress != null
        && !CollectionUtils.isEmpty(hasAddress.getVcardAddressesList())
        && hasAddress.getVcardAddressesList().get(0).hasMetadataProperties()) {
      entity.setAddress(hasAddress.getVcardAddressesList().get(0).toAddress());
    }
    entity.setIdentifier(getIdentifier());
    entity.setLanguage(getLanguage());
    return entity;
  }

  public XmlOrganizationImpl() {
    // default constructor
  }

  public List<LabelledResource> getAcronym() {
    return acronym;
  }

  public List<LabelledResource> getDescription() {
    return description;
  }

  public XmlWebResourceWrapper getLogo() {
    return logo;
  }

  public List<LabelledResource> getEuropeanaRole() {
    return europeanaRole;
  }

  public String getCountry() {
    return country;
  }

  public LabelledResource getHomepage() {
    return homepage;
  }

  public List<String> getPhone() {
    return phone;
  }

  public List<String> getMbox() {
    return mbox;
  }

  public List<String> getIdentifier() {
    return identifier;
  }

  public XmlAddresses getHasAddress() {
    return hasAddress;
  }

  @Override
  protected EntityTypes getTypeEnum() {
    return EntityTypes.Organization;
  }

  @Override
  public List<LabelledResource> getSameReferenceLinks() {
    return this.sameAs;
  }

  @Override
  public void setSameReferenceLinks(List<LabelledResource> uris) {
    this.sameAs = uris;
  }

  public List<String> getLanguage() {
    return language;
  }

  public String getCountryId() {
    return countryId;
  }

  public XmlPlaceImpl getCountryPlace() {
    return countryPlace;
  }
}
