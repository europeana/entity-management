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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.exception.EntityCreationException;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

@XmlRootElement(namespace = NAMESPACE_EDM, name = XML_AGENT)
public class XmlAgentImpl extends XmlBaseEntityImpl {

	private List<LabelledResource> isPartOf = new ArrayList<>();
	private String[] identifier;
	private List<LabelledResource> note = new ArrayList<>();
	private List<LabelledResource> hasPart = new ArrayList<>();
	private List<LabelledResource> hasMet = new ArrayList<>();
	private List<LabelledResource> hiddenLabel = new ArrayList<>();
	private List<LabelledResource> biographicalInformation = new ArrayList<>();
	private  String[] begin;
	private String[] end;
	private List<LabelledResource> isRelatedTo = new ArrayList<>();
	private List<LabelledResource> name = new ArrayList<>();
	private String[] dateOfBirth;
	private String[] dateOfDeath;
	private String dateOfEstablishment;
	private String dateOfTermination;
	private String[] dcDate;
        private String gender;
	private List<LabelledResource> placeOfBirth = new ArrayList<>();
	private List<LabelledResource> placeOfDeath = new ArrayList<>();
	private List<LabelledResource> professionOrOccupation = new ArrayList<>();

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
            this.biographicalInformation = RdfXmlUtils
                    .convertToXmlMultilingualString(agent.getBiographicalInformation());
            this.dateOfBirth = agent.getDateOfBirth();
            this.dateOfDeath = agent.getDateOfDeath();
            this.dateOfEstablishment = agent.getDateOfEstablishment();
            this.dateOfTermination = agent.getDateOfTermination();
            this.dcDate = agent.getDate();
            this.gender = agent.getGender();
            this.placeOfBirth = RdfXmlUtils.convertToXmlMultilingualStringOrRdfResource(agent.getPlaceOfBirth());
            this.placeOfDeath = RdfXmlUtils.convertToXmlMultilingualStringOrRdfResource(agent.getPlaceOfDeath());
            this.professionOrOccupation = RdfXmlUtils
                    .convertToXmlMultilingualStringOrRdfResource(agent.getProfessionOrOccupation());
        }

	public XmlAgentImpl() {
		// default constructor
	}

        public Entity toEntityModel() throws EntityCreationException {
            super.toEntityModel();
            Agent agent = (Agent) getEntity();

            agent.setHiddenLabel(RdfXmlUtils.toLanguageMapList(getHiddenLabel()));
            agent.setNote(RdfXmlUtils.toLanguageMapList(getNote()));
            agent.setIdentifier(getIdentifier());
            agent.setHasPart(RdfXmlUtils.toStringArray(getHasPart()));
            agent.setIsPartOfArray(RdfXmlUtils.toStringArray(getIsPartOf()));
            agent.setBegin(getBegin());
            agent.setEnd(getEnd());
            agent.setHasMet(RdfXmlUtils.toStringArray(getHasMet()));
            agent.setIsRelatedTo(RdfXmlUtils.toStringArray(getIsRelatedTo()));
            agent.setName(RdfXmlUtils.toLanguageMap(getName()));
            agent.setBiographicalInformation(RdfXmlUtils.toLanguageMapList(getBiographicalInformation()));
            agent.setDateOfBirth(getDateOfBirth());
            agent.setDateOfDeath(getDateOfDeath());
            agent.setDateOfEstablishment(getDateOfEstablishment());
            agent.setDateOfTermination(getDateOfTermination());
            agent.setDate(getDcDate());
            agent.setGender(getGender());
            agent.setPlaceOfBirth(RdfXmlUtils.toLanguageMapList(getPlaceOfBirth()));
            agent.setPlaceOfDeath(RdfXmlUtils.toLanguageMapList(getPlaceOfDeath()));
            agent.setProfessionOrOccupation(RdfXmlUtils.toLanguageMapList(getProfessionOrOccupation()));

            return agent;
        }
	
	@XmlElement(name = HIDDEN_LABEL, namespace = NAMESPACE_SKOS)
	public List<LabelledResource> getHiddenLabel() {
		return hiddenLabel;
	}

	@XmlElement(name = NOTE, namespace = NAMESPACE_SKOS)
	public List<LabelledResource> getNote() {
		return note;
	}
    	
	@XmlElement(name = XmlConstants.XML_DC_DATE)
	public String[] getDcDate() {
	    	return dcDate;
	}

	@XmlElement(name = XML_IDENTIFIER)
	public String[] getIdentifier() {
	    	return identifier;
	}

	@XmlElement(name = XML_HAS_PART)
	public List<LabelledResource> getHasPart() {
	    	return hasPart;
	}

	@XmlElement(name = XML_IS_PART_OF)
	public List<LabelledResource> getIsPartOf() {
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
	public List<LabelledResource> getHasMet() {
	    	return hasMet;
	}
	
	@XmlElement(name = XmlConstants.XML_IS_RELATED_TO)
	public List<LabelledResource> getIsRelatedTo() {
	    	return isRelatedTo;
	}
	
	@XmlElement(name = XmlConstants.XML_NAME)
	public List<LabelledResource> getName(){
	    	return name;
	}
	
	@XmlElement(name = XmlConstants.XML_BIOGRAPHICAL_INFORMATION)
	public List<LabelledResource> getBiographicalInformation(){
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
	public List<LabelledResource> getPlaceOfBirth(){
	    	return placeOfBirth;
	}
	
	@XmlElement(name = XmlConstants.XML_PLACE_OF_DEATH)
	public List<LabelledResource> getPlaceOfDeath(){
	    	return placeOfDeath;
	}
	
	@XmlElement(name = XmlConstants.XML_PROFESSION_OR_OCCUPATION, namespace = "http://rdvocab.info/ElementsGr2/")
	public List<LabelledResource> getProfessionOrOccupation(){
	    	return professionOrOccupation;
	}
	
	@Override
	protected EntityTypes getTypeEnum() {
	    return EntityTypes.Agent;
	}
	
}
