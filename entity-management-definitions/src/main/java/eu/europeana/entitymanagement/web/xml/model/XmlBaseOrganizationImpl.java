package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.*;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.collections.CollectionUtils;
import eu.europeana.entitymanagement.definitions.exceptions.EntityModelCreationException;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.definitions.model.Vocabulary;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlTransient
public abstract class XmlBaseOrganizationImpl extends XmlBaseEntityImpl<Organization> {

  @XmlElement(namespace = NAMESPACE_OWL, name = XML_SAME_AS)
  protected List<LabelledResource> sameAs = new ArrayList<>();

  @XmlElement(namespace = NAMESPACE_EDM, name = XML_AGGREGATES_FROM)
  protected List<LabelledResource> aggregatesFrom = new ArrayList<>();

  @XmlElement(namespace = NAMESPACE_EDM, name = XML_AGGREGATED_VIA)
  protected List<LabelledResource> aggregatedVia = new ArrayList<>();

  @XmlElement(namespace = NAMESPACE_EDM, name = XML_ACRONYM)
  protected List<LabelledResource> acronym = new ArrayList<>();

  @XmlElement(namespace = NAMESPACE_DC, name = XML_DESCRIPTION)
  protected List<LabelledResource> description = new ArrayList<>();

  @XmlElement(namespace = NAMESPACE_FOAF, name = XML_LOGO)
  protected XmlWebResourceWrapper logo;

  @XmlElement(namespace = NAMESPACE_EDM, name = XML_EUROPEANA_ROLE)
  protected List<XmlEdmEuropeanaRoleWrapper> europeanaRole = new ArrayList<>();

  @XmlElement(namespace = NAMESPACE_EDM, name = XML_COUNTRY)
  protected XmlEdmCountryWrapper country;

  @XmlElement(namespace = NAMESPACE_FOAF, name = XML_HOMEPAGE)
  protected LabelledResource homepage;

  @XmlElement(namespace = NAMESPACE_FOAF, name = XML_PHONE)
  protected List<String> phone;

  @XmlElement(namespace = NAMESPACE_VCARD, name = XML_HAS_ADDRESS)
  protected XmlAddresses hasAddress;

  @XmlElement(namespace = NAMESPACE_DC, name = XML_IDENTIFIER)
  protected List<String> identifier;

  @XmlElement(namespace = NAMESPACE_EDM, name = XML_LANGUAGE)
  protected List<String> language;

  public XmlBaseOrganizationImpl(Organization organization) {
    super(organization);
    this.sameAs = RdfXmlUtils.convertToRdfResource(organization.getSameReferenceLinks());
    this.aggregatesFrom = RdfXmlUtils.convertToRdfResource(organization.getAggregatesFrom());
    this.aggregatedVia = RdfXmlUtils.convertToRdfResource(organization.getAggregatedVia());
    this.acronym = RdfXmlUtils.convertToXmlMultilingualString(organization.getAcronym());
    this.description = RdfXmlUtils.convertMapToXmlMultilingualString(organization.getDescription());
    if (organization.getLogo() != null) {
      this.logo = XmlWebResourceWrapper.fromWebResource(organization.getLogo());
    }
    //set the europeanaRole
    List<Vocabulary> orgRole=organization.getEuropeanaRole();
    if(orgRole!=null && !orgRole.isEmpty()) {
      List<XmlEdmEuropeanaRoleWrapper> orgXmlRole= new ArrayList<>(orgRole.size());
      for(Vocabulary vocab : orgRole) {
        XmlEdmEuropeanaRoleWrapper xmlRole = new XmlEdmEuropeanaRoleWrapper(vocab);
        orgXmlRole.add(xmlRole);
      }
      this.europeanaRole=orgXmlRole;
    }
    
    if(organization.getCountry() != null) {
      this.country=new XmlEdmCountryWrapper(organization.getCountry());
    }
    
    if (organization.getHomepage() != null) {
      this.homepage = new LabelledResource(organization.getHomepage());
    }
    if (organization.getPhone() != null) {
      this.phone = new ArrayList<String>(organization.getPhone());
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
    //set europeanaRole id (external dereferencers deliver only the ids, not transitive data)
    if(getEuropeanaRole()!=null && !getEuropeanaRole().isEmpty()) {
      List<String> roleIds = getEuropeanaRole().stream().map(e -> e.getConcept().getAbout()).toList();
      entity.setEuropeanaRoleIds(roleIds);
    }
    
    //set country id (external dereferencers deliver only the ids, not transitive data)
    if(getCountry() != null) {
      entity.setCountryId(getCountry().getPlace().getAbout());
    }
    
    if (getHomepage() != null) {
      entity.setHomepage(getHomepage().getResource());
    }
    entity.setPhone(getPhone());
    if (hasAddress != null
        && !CollectionUtils.isEmpty(hasAddress.getVcardAddressesList())
        && hasAddress.getVcardAddressesList().get(0).hasMetadataProperties()) {
      entity.setAddress(hasAddress.getVcardAddressesList().get(0).toAddress());
    }
    entity.setIdentifier(getIdentifier());
    entity.setLanguage(getLanguage());
    entity.setAggregatesFrom(RdfXmlUtils.toStringList(getAggregatesFrom()));
    entity.setAggregatedVia(RdfXmlUtils.toStringList(getAggregatedVia()));
    return entity;
  }

  public XmlBaseOrganizationImpl() {
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

  public List<XmlEdmEuropeanaRoleWrapper> getEuropeanaRole() {
    return europeanaRole;
  }

  public XmlEdmCountryWrapper getCountry() {
    return country;
  }

  public LabelledResource getHomepage() {
    return homepage;
  }

  public List<String> getPhone() {
    return phone;
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

  public List<LabelledResource> getAggregatesFrom() {
    return aggregatesFrom;
  }

  public List<LabelledResource> getAggregatedVia() {
    return aggregatedVia;
  }
}
