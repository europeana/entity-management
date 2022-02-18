package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.*;

import eu.europeana.entitymanagement.definitions.exceptions.EntityCreationException;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = NAMESPACE_EDM, name = XML_AGENT)
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlAgentImpl extends XmlBaseEntityImpl<Agent> {

  @XmlElement(namespace = XmlConstants.NAMESPACE_OWL, name = XmlConstants.XML_SAME_AS)
  private List<LabelledResource> sameAs = new ArrayList<>();

  @XmlElement(name = XML_IS_PART_OF, namespace = NAMESPACE_DC_TERMS)
  private List<LabelledResource> isPartOf = new ArrayList<>();

  @XmlElement(name = XML_IDENTIFIER, namespace = NAMESPACE_DC)
  private List<String> identifier;

  @XmlElement(name = NOTE, namespace = NAMESPACE_SKOS)
  private List<LabelledResource> note = new ArrayList<>();

  @XmlElement(name = XML_HAS_PART, namespace = NAMESPACE_DC_TERMS)
  private List<LabelledResource> hasPart = new ArrayList<>();

  @XmlElement(name = XML_HASMET, namespace = NAMESPACE_EDM)
  private List<LabelledResource> hasMet = new ArrayList<>();

  @XmlElement(name = HIDDEN_LABEL, namespace = NAMESPACE_SKOS)
  private List<String> hiddenLabel = new ArrayList<>();

  @XmlElement(name = XML_BIOGRAPHICAL_INFORMATION, namespace = NAMESPACE_RDAGR2)
  private List<LabelledResource> biographicalInformation = new ArrayList<>();

  @XmlElement(name = XML_BEGIN, namespace = NAMESPACE_EDM)
  private List<String> begin;

  @XmlElement(name = XML_END, namespace = NAMESPACE_EDM)
  private List<String> end;

  @XmlElement(name = XML_IS_RELATED_TO, namespace = NAMESPACE_EDM)
  private List<LabelledResource> isRelatedTo = new ArrayList<>();

  @XmlElement(name = XML_NAME, namespace = NAMESPACE_FOAF)
  private List<LabelledResource> name = new ArrayList<>();

  @XmlElement(name = XML_DATE_OF_BIRTH, namespace = NAMESPACE_RDAGR2)
  private List<String> dateOfBirth;

  @XmlElement(name = XML_DATE_OF_DEATH, namespace = NAMESPACE_RDAGR2)
  private List<String> dateOfDeath;

  @XmlElement(name = XML_DATE_OF_ESTABLISHMENT, namespace = NAMESPACE_RDAGR2)
  private List<String> dateOfEstablishment;

  @XmlElement(name = XML_DATE_OF_TERMINATION, namespace = NAMESPACE_RDAGR2)
  private List<String> dateOfTermination;

  @XmlElement(name = XML_DATE, namespace = NAMESPACE_DC)
  private List<String> dcDate;

  @XmlElement(name = XML_GENDER, namespace = NAMESPACE_RDAGR2)
  private List<String> gender;

  @XmlElement(name = XML_PLACE_OF_BIRTH, namespace = NAMESPACE_RDAGR2)
  private List<LabelledResource> placeOfBirth = new ArrayList<>();

  @XmlElement(name = XML_PLACE_OF_DEATH, namespace = NAMESPACE_RDAGR2)
  private List<LabelledResource> placeOfDeath = new ArrayList<>();

  @XmlElement(name = XML_PROFESSION_OR_OCCUPATION, namespace = NAMESPACE_RDAGR2)
  private List<LabelledResource> professionOrOccupation = new ArrayList<>();

  public XmlAgentImpl(Agent agent) {
    super(agent);
    this.sameAs = RdfXmlUtils.convertToRdfResource(agent.getSameReferenceLinks());
    if (agent.getHiddenLabel() != null) {
      this.hiddenLabel = new ArrayList<String>(agent.getHiddenLabel());
    }
    this.note = RdfXmlUtils.convertToXmlMultilingualString(agent.getNote());
    if (agent.getIdentifier() != null) {
      this.identifier = agent.getIdentifier();
    }
    this.hasPart = RdfXmlUtils.convertToRdfResource(agent.getHasPart());
    this.isPartOf = RdfXmlUtils.convertToRdfResource(agent.getIsPartOfArray());
    if (agent.getBegin() != null) {
      this.begin = agent.getBegin();
    }
    if (agent.getEnd() != null) {
      this.end = agent.getEnd();
    }
    this.hasMet = RdfXmlUtils.convertToRdfResource(agent.getHasMet());
    this.isRelatedTo = RdfXmlUtils.convertToRdfResource(agent.getIsRelatedTo());
    this.name = RdfXmlUtils.convertMapToXmlMultilingualString(agent.getName());
    this.biographicalInformation =
        RdfXmlUtils.convertToXmlMultilingualString(agent.getBiographicalInformation());
    if (agent.getDateOfBirth() != null) {
      this.dateOfBirth = agent.getDateOfBirth();
    }
    if (agent.getDateOfDeath() != null) {
      this.dateOfDeath = agent.getDateOfDeath();
    }
    if (agent.getDateOfEstablishment() != null) {
      this.dateOfEstablishment = agent.getDateOfEstablishment();
    }
    if (agent.getDateOfTermination() != null) {
      this.dateOfTermination = agent.getDateOfTermination();
    }
    if (agent.getDate() != null) {
      this.dcDate = agent.getDate();
    }
    if (agent.getGender() != null) {
      this.gender = agent.getGender();
    }
    this.placeOfBirth = RdfXmlUtils.convertToRdfResource(agent.getPlaceOfBirth());
    this.placeOfDeath = RdfXmlUtils.convertToRdfResource(agent.getPlaceOfDeath());
    this.professionOrOccupation =
        RdfXmlUtils.convertToRdfResource(agent.getProfessionOrOccupation());
  }

  public XmlAgentImpl() {
    // default constructor
  }

  @Override
  public Agent toEntityModel() throws EntityCreationException {
    super.toEntityModel();
    entity.setHiddenLabel(hiddenLabel);
    entity.setNote(RdfXmlUtils.toLanguageMapList(note));
    entity.setIdentifier(identifier);
    entity.setHasPart(RdfXmlUtils.toStringList(hasPart));
    entity.setIsPartOfArray(RdfXmlUtils.toStringList(isPartOf));
    entity.setBegin(begin);
    entity.setEnd(end);
    entity.setHasMet(RdfXmlUtils.toStringList(hasMet));
    entity.setIsRelatedTo(RdfXmlUtils.toStringList(isRelatedTo));
    entity.setName(RdfXmlUtils.toLanguageMap(name));
    entity.setBiographicalInformation(RdfXmlUtils.toLanguageMapList(biographicalInformation));
    entity.setDateOfBirth(dateOfBirth);
    entity.setDateOfDeath(dateOfDeath);
    entity.setDateOfEstablishment(dateOfEstablishment);
    entity.setDateOfTermination(dateOfTermination);
    entity.setDate(dcDate);
    entity.setGender(gender);
    entity.setPlaceOfBirth(RdfXmlUtils.toStringList(placeOfBirth));
    entity.setPlaceOfDeath(RdfXmlUtils.toStringList(placeOfDeath));
    entity.setProfessionOrOccupation(RdfXmlUtils.toStringList(professionOrOccupation));

    return entity;
  }

  public List<String> getHiddenLabel() {
    return hiddenLabel;
  }

  public List<LabelledResource> getNote() {
    return note;
  }

  public List<String> getDcDate() {
    return dcDate;
  }

  public List<String> getIdentifier() {
    return identifier;
  }

  public List<LabelledResource> getHasPart() {
    return hasPart;
  }

  public List<LabelledResource> getIsPartOf() {
    return isPartOf;
  }

  public List<String> getBegin() {
    return begin;
  }

  public List<String> getEnd() {
    return end;
  }

  public List<LabelledResource> getHasMet() {
    return hasMet;
  }

  public List<LabelledResource> getIsRelatedTo() {
    return isRelatedTo;
  }

  public List<LabelledResource> getName() {
    return name;
  }

  public List<LabelledResource> getBiographicalInformation() {
    return biographicalInformation;
  }

  public List<String> getDateOfBirth() {
    return dateOfBirth;
  }

  public List<String> getDateOfDeath() {
    return dateOfDeath;
  }

  public List<String> getDateOfEstablishment() {
    return dateOfEstablishment;
  }

  public List<String> getDateOfTermination() {
    return dateOfTermination;
  }

  public List<String> getGender() {
    return gender;
  }

  public List<LabelledResource> getPlaceOfBirth() {
    return placeOfBirth;
  }

  public List<LabelledResource> getPlaceOfDeath() {
    return placeOfDeath;
  }

  public List<LabelledResource> getProfessionOrOccupation() {
    return professionOrOccupation;
  }

  @Override
  protected EntityTypes getTypeEnum() {
    return EntityTypes.Agent;
  }

  @Override
  public List<LabelledResource> getSameReferenceLinks() {
    return this.sameAs;
  }

  @Override
  public void setSameReferenceLinks(List<LabelledResource> uris) {
    this.sameAs = uris;
  }
}
