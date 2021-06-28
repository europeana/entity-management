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
  private List<LabelledResource> hiddenLabel = new ArrayList<>();

  @XmlElement(name = XML_BIOGRAPHICAL_INFORMATION, namespace = NAMESPACE_RDAGR2)
  private List<LabelledResource> biographicalInformation = new ArrayList<>();

  @XmlElement(name = XML_BEGIN, namespace = NAMESPACE_EDM)
  private String begin;

  @XmlElement(name = XML_END, namespace = NAMESPACE_EDM)
  private String end;

  @XmlElement(name = XML_IS_RELATED_TO, namespace = NAMESPACE_EDM)
  private List<LabelledResource> isRelatedTo = new ArrayList<>();

  @XmlElement(name = XML_NAME, namespace = NAMESPACE_FOAF)
  private List<LabelledResource> name = new ArrayList<>();

  @XmlElement(name = XML_DATE_OF_BIRTH, namespace = NAMESPACE_RDAGR2)
  private String dateOfBirth;

  @XmlElement(name = XML_DATE_OF_DEATH, namespace = NAMESPACE_RDAGR2)
  private String dateOfDeath;

  @XmlElement(name = XML_DATE_OF_ESTABLISHMENT, namespace = NAMESPACE_RDAGR2)
  private String dateOfEstablishment;

  @XmlElement(name = XML_DATE_OF_TERMINATION, namespace = NAMESPACE_RDAGR2)
  private String dateOfTermination;

  @XmlElement(name = XML_DATE, namespace = NAMESPACE_DC)
  private List<String> dcDate;

  @XmlElement(name = XML_GENDER, namespace = NAMESPACE_RDAGR2)
  private String gender;

  @XmlElement(name = XML_PLACE_OF_BIRTH, namespace = NAMESPACE_RDAGR2)
  private List<LabelledResource> placeOfBirth = new ArrayList<>();

  @XmlElement(name = XML_PLACE_OF_DEATH, namespace = NAMESPACE_RDAGR2)
  private List<LabelledResource> placeOfDeath = new ArrayList<>();

  @XmlElement(name = XML_PROFESSION_OR_OCCUPATION, namespace = NAMESPACE_RDAGR2)
  private List<LabelledResource> professionOrOccupation = new ArrayList<>();

  public XmlAgentImpl(Agent agent) {
    super(agent);
    this.hiddenLabel = RdfXmlUtils.convertToXmlMultilingualString(agent.getHiddenLabel());
    this.note = RdfXmlUtils.convertToXmlMultilingualString(agent.getNote());
    this.identifier = agent.getIdentifier();
    this.hasPart = RdfXmlUtils.convertToRdfResource(agent.getHasPart());
    this.isPartOf = RdfXmlUtils.convertToRdfResource(agent.getIsPartOfArray());
    this.begin = getFirstValue(agent.getBegin());
    this.end = getFirstValue(agent.getEnd());
    this.hasMet = RdfXmlUtils.convertToRdfResource(agent.getHasMet());
    this.isRelatedTo = RdfXmlUtils.convertToRdfResource(agent.getIsRelatedTo());
    this.name = RdfXmlUtils.convertMapToXmlMultilingualString(agent.getName());
    this.biographicalInformation = RdfXmlUtils
        .convertToXmlMultilingualString(agent.getBiographicalInformation());
    this.dateOfBirth = getFirstValue(agent.getDateOfBirth());
    this.dateOfDeath = getFirstValue(agent.getDateOfDeath());
    this.dateOfEstablishment = agent.getDateOfEstablishment();
    this.dateOfTermination = agent.getDateOfTermination();
    this.dcDate = agent.getDate();
    this.gender = agent.getGender();
    this.placeOfBirth = RdfXmlUtils
        .convertToXmlMultilingualStringOrRdfResource(agent.getPlaceOfBirth());
    this.placeOfDeath = RdfXmlUtils
        .convertToXmlMultilingualStringOrRdfResource(agent.getPlaceOfDeath());
    this.professionOrOccupation = RdfXmlUtils
        .convertToRdfResource(agent.getProfessionOrOccupation());
  }

  public XmlAgentImpl() {
    // default constructor
  }

  public Agent toEntityModel() throws EntityCreationException {
    super.toEntityModel();

    entity.setHiddenLabel(RdfXmlUtils.toLanguageMapList(hiddenLabel));
    entity.setNote(RdfXmlUtils.toLanguageMapList(note));
    entity.setIdentifier(identifier);
    entity.setHasPart(RdfXmlUtils.toStringList(hasPart));
    entity.setIsPartOfArray(RdfXmlUtils.toStringList(isPartOf));
    entity.setBegin(toList(begin));
    entity.setEnd(toList(end));
    entity.setHasMet(RdfXmlUtils.toStringList(hasMet));
    entity.setIsRelatedTo(RdfXmlUtils.toStringList(isRelatedTo));
    entity.setName(RdfXmlUtils.toLanguageMap(name));
    entity.setBiographicalInformation(RdfXmlUtils.toLanguageMapList(biographicalInformation));
    entity.setDateOfBirth(toList(dateOfBirth));
    entity.setDateOfDeath(toList(dateOfDeath));
    entity.setDateOfEstablishment(dateOfEstablishment);
    entity.setDateOfTermination(dateOfTermination);
    entity.setDate(dcDate);
    entity.setGender(gender);
    entity.setPlaceOfBirth(RdfXmlUtils.toLanguageMapList(placeOfBirth));
    entity.setPlaceOfDeath(RdfXmlUtils.toLanguageMapList(placeOfDeath));
    entity.setProfessionOrOccupation(RdfXmlUtils.toStringList(professionOrOccupation));

    return entity;
  }

  public List<LabelledResource> getHiddenLabel() {
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

  public String getBegin() {
    return begin;
  }

  public String getEnd() {
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


  public String getDateOfBirth() {
    return dateOfBirth;
  }

  public String getDateOfDeath() {
    return dateOfDeath;
  }

  public String getDateOfEstablishment() {
    return dateOfEstablishment;
  }

  public String getDateOfTermination() {
    return dateOfTermination;
  }

  public String getGender() {
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


  private<T> T getFirstValue(List<T> list){
     if(list == null) {
       return null;
     }
     return list.get(0);
  }

  private <T> List<T> toList(T field) {
    if (field == null){
      return null;
    }
    return List.of(field);
  }

}
