package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.HIDDEN_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_EDM;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_SKOS;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NOTE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_AGENT;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_BEGIN;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_END;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_HAS_PART;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_IDENTIFIER;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_IS_PART_OF;

import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = NAMESPACE_EDM, name = XML_AGENT)
public class XmlAgentImpl extends XmlBaseEntityImpl {

	private List<LabelResource> isPartOf = new ArrayList<>();
	private String[] identifier;
	private List<LabelResource> note = new ArrayList<>();
	private List<LabelResource> hasPart = new ArrayList<>();
	private List<LabelResource> hasMet = new ArrayList<>();
	private List<LabelResource> hiddenLabel = new ArrayList<>();
	private List<LabelResource> biographicalInformation = new ArrayList<>();
	private  String[] begin;
	private String[] end;
	private List<LabelResource> isRelatedTo = new ArrayList<>();
	private List<LabelResource> name = new ArrayList<>();
	private String[] dateOfBirth;
	private String[] dateOfDeath;
	private String dateOfEstablishment;
	private String dateOfTermination;
	private String gender;
	private List<LabelResource> placeOfBirth = new ArrayList<>();
	private List<LabelResource> placeOfDeath = new ArrayList<>();
	private List<LabelResource> professionOrOccupation = new ArrayList<>();

	public XmlAgentImpl(Agent agent) {
    	    	super(agent);
    	    	this.hiddenLabel = RdfXmlUtils.convertToXmlMultilingualString(agent.getHiddenLabel());
    	    	this.note = RdfXmlUtils.convertToXmlMultilingualString(agent.getNote());
    	    	this.identifier = agent.getIdentifier();
    	    	this.hasPart = RdfXmlUtils.convertToRdfResource(agent.getHasPart());
    	    	this.isPartOf = RdfXmlUtils.convertToRdfResource(agent.getIsPartOfArray());
    	    	this.begin = agent.getBeginArray();
    	    	this.end = agent.getEndArray();
    	    	this.hasMet = RdfXmlUtils.convertToRdfResource(agent.getHasMet());
    	    	this.isRelatedTo = RdfXmlUtils.convertToRdfResource(agent.getIsRelatedTo());
    	    	this.name = RdfXmlUtils.convertMapToXmlMultilingualString(agent.getName());
						this.biographicalInformation = RdfXmlUtils.convertToXmlMultilingualString(agent.getBiographicalInformation());
						this.dateOfBirth = agent.getDateOfBirth();
						this.dateOfDeath = agent.getDateOfDeath();
						this.dateOfEstablishment = agent.getDateOfEstablishment();
						this.dateOfTermination = agent.getDateOfTermination();
						this.gender = agent.getGender();
						this.placeOfBirth = RdfXmlUtils.convertToXmlMultilingualStringOrRdfResource(agent.getPlaceOfBirth());
						this.placeOfDeath = RdfXmlUtils.convertToXmlMultilingualStringOrRdfResource(agent.getPlaceOfDeath());
						this.professionOrOccupation = RdfXmlUtils.convertToXmlMultilingualStringOrRdfResource(agent.getProfessionOrOccupation());
					}

	public XmlAgentImpl() {
		// default constructor
	}

	@XmlElement(name = HIDDEN_LABEL, namespace = NAMESPACE_SKOS)
	public List<LabelResource> getHiddenLabel() {
		return hiddenLabel;
	}

	@XmlElement(name = NOTE, namespace = NAMESPACE_SKOS)
	public List<LabelResource> getNote() {
		return note;
	}
    	
//	@JacksonXmlElementWrapper(useWrapping=false)
//	@JacksonXmlProperty(localName = XmlConstants.XML_DC_DATE)
//	public List<Object> getDcDate() {
//	    	// TODO: GetDcDate from Agent currently not implemented
//	    	return null;
//		//return XmlMultilingualString.convertToXmlMultilingualStringOrRdfResource(agent.getDcDate());
//	}

	@XmlElement(name = XML_IDENTIFIER)
	public String[] getIdentifier() {
	    	return identifier;
	}

	@XmlElement(name = XML_HAS_PART)
	public List<LabelResource> getHasPart() {
	    	return hasPart;
	}

	@XmlElement(name = XML_IS_PART_OF)
	public List<LabelResource> getIsPartOf() {
	    	return isPartOf;
	}

	@XmlElement(name = XML_BEGIN)
	public String[] getBegin() {
	    	return begin;
	}

	@XmlElement(name = XML_END)
	public String[] getEnd() {
	    	return end;
	}

	@XmlElement(name = XmlConstants.XML_HASMET)
	public List<LabelResource> getHasMet() {
	    	return hasMet;
	}
	
	@XmlElement(name = XmlConstants.XML_IS_RELATED_TO)
	public List<LabelResource> getIsRelatedTo() {
	    	return isRelatedTo;
	}
	
	@XmlElement(name = XmlConstants.XML_NAME)
	public List<LabelResource> getName(){
	    	return name;
	}
	
	@XmlElement(name = XmlConstants.XML_BIOGRAPHICAL_INFORMATION)
	public List<LabelResource> getBiographicalInformation(){
	    	return biographicalInformation;
	}
	
	@XmlElement(name = XmlConstants.XML_DATE_OF_BIRTH)
	public String[] getDateOfBirth() {
	    	return dateOfBirth;
	}
	
	@XmlElement(name = XmlConstants.XML_DATE_OF_DEATH)
	public String[] getDateOfDeath() {
	    	return dateOfDeath;
	}
	
	@XmlElement(name = XmlConstants.XML_DATE_OF_ESTABLISHMENT)
	public String getDateOfEstablishment() {
	    	return dateOfEstablishment;
	}
	
	@XmlElement(name = XmlConstants.XML_DATE_OF_TERMINATION)
	public String getDateOfTermination() {
	    	return dateOfTermination;
	}
	
	@XmlElement(name = XmlConstants.XML_GENDER)
	public String getGender() {
	    	return gender;
	}
	
	@XmlElement(name = XmlConstants.XML_PLACE_OF_BIRTH)
	public List<LabelResource> getPlaceOfBirth(){
	    	return placeOfBirth;
	}
	
	@XmlElement(name = XmlConstants.XML_PLACE_OF_DEATH)
	public List<LabelResource> getPlaceOfDeath(){
	    	return placeOfDeath;
	}
	
	@XmlElement(name = XmlConstants.XML_PROFESSION_OR_OCCUPATION, namespace = "http://rdvocab.info/ElementsGr2/")
	public List<LabelResource> getProfessionOrOccupation(){
	    	return professionOrOccupation;
	}
	
//	@JacksonXmlElementWrapper(useWrapping=false)
//	@JacksonXmlProperty(localName = XmlConstants.XML_SAME_AS)
//	public List<RdfResource> getSameAs(){
//	    	return RdfXmlUtils.convertToRdfResource(getAgent().getSameAs());
//	}


	@Override
	protected EntityTypes getTypeEnum() {
	    return EntityTypes.Agent;
	}
	
}
