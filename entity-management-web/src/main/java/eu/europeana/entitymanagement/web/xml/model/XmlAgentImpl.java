package eu.europeana.entitymanagement.web.xml.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

@JacksonXmlRootElement(localName = XmlConstants.XML_EDM_AGENT)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({XmlConstants.DEPICTION, XmlConstants.PREF_LABEL, XmlConstants.ALT_LABEL, XmlConstants.XML_SKOS_HIDDEN_LABEL,
    	XmlConstants.XML_NAME, XmlConstants.XML_BEGIN, XmlConstants.XML_DATE_OF_BIRTH, XmlConstants.XML_DATE_OF_ESTABLISHMENT,
    	XmlConstants.XML_END, XmlConstants.XML_DATE_OF_DEATH, XmlConstants.XML_DATE_OF_TERMINATION, XmlConstants.XML_DATE,
    	XmlConstants.XML_PLACE_OF_BIRTH, XmlConstants.XML_PLACE_OF_DEATH, XmlConstants.XML_GENDER, 
    	XmlConstants.XML_PROFESSION_OR_OCCUPATION, XmlConstants.XML_BIOGRAPHICAL_INFORMATION, XmlConstants.NOTE,
    	XmlConstants.XML_HAS_PART, XmlConstants.XML_IS_PART_OF, XmlConstants.XML_HASMET, XmlConstants.XML_IS_RELATED_TO,
    	XmlConstants.XML_IDENTIFIER, XmlConstants.XML_SAME_AS, XmlConstants.IS_AGGREGATED_BY})
public class XmlAgentImpl extends XmlBaseEntityImpl {
    	
    	public XmlAgentImpl(Agent agent) {
    	    	super(agent);    	    	
    	}

	public XmlAgentImpl() {
		// default constructor
	}

	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_SKOS_HIDDEN_LABEL)
	public List<XmlMultilingualString> getHiddenLabel() {
		return RdfXmlUtils.convertToXmlMultilingualString(entity.getHiddenLabel());
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.NOTE)
	public List<XmlMultilingualString> getNote() {
		return RdfXmlUtils.convertToXmlMultilingualString(entity.getNote());
	}
    	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_DC_DATE)
	public List<Object> getDcDate() {
	    	// TODO: GetDcDate from Agent currently not implemented
	    	return null;
		//return XmlMultilingualString.convertToXmlMultilingualStringOrRdfResource(agent.getDcDate());
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_IDENTIFIER)
	public String[] getIdentifier() {
	    	return entity.getIdentifier();
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_HAS_PART)
	public List<RdfResource> getHasPart() {
	    	return RdfXmlUtils.convertToRdfResource(getAgent().getHasPart());
	}

	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_IS_PART_OF)
	public List<RdfResource> getIsPartOf() {
	    	return RdfXmlUtils.convertToRdfResource(getAgent().getIsPartOfArray());
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_BEGIN)
	public String[] getBegin() {
	    	return getAgent().getBeginArray();
	}

	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_END)
	public String[] getEnd() {
	    	return getAgent().getEndArray();
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_HASMET)
	public List<RdfResource> getHasMet() {
	    	return RdfXmlUtils.convertToRdfResource(getAgent().getHasMet());
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_IS_RELATED_TO)
	public List<RdfResource> getIsRelatedTo() {
	    	return RdfXmlUtils.convertToRdfResource(getAgent().getIsRelatedTo());
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_NAME)
	public List<XmlMultilingualString> getName(){
	    	return RdfXmlUtils.convertMapToXmlMultilingualString(getAgent().getName());
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_BIOGRAPHICAL_INFORMATION)
	public List<XmlMultilingualString> getBiographicalInformation(){
	    	return RdfXmlUtils.convertToXmlMultilingualString(getAgent().getBiographicalInformation());
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_DATE_OF_BIRTH)
	public String[] getDateOfBirth() {
	    	return getAgent().getDateOfBirth();
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_DATE_OF_DEATH)
	public String[] getDateOfDeath() {
	    	return getAgent().getDateOfDeath();
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_DATE_OF_ESTABLISHMENT)
	public String getDateOfEstablishment() {
	    	return getAgent().getDateOfEstablishment();
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_DATE_OF_TERMINATION)
	public String getDateOfTermination() {
	    	return getAgent().getDateOfTermination();
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_GENDER)
	public String getGender() {
	    	return getAgent().getGender();
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_PLACE_OF_BIRTH)
	public List<Object> getPlaceOfBirth(){
	    	return RdfXmlUtils.convertToXmlMultilingualStringOrRdfResource(getAgent().getPlaceOfBirth());
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_PLACE_OF_DEATH)
	public List<Object> getPlaceOfDeath(){
	    	return RdfXmlUtils.convertToXmlMultilingualStringOrRdfResource(getAgent().getPlaceOfDeath());
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_PROFESSION_OR_OCCUPATION)
	public List<Object> getProfessionOrOccupation(){
	    	return RdfXmlUtils.convertToXmlMultilingualStringOrRdfResource(getAgent().getProfessionOrOccupation());
	}
	
//	@JacksonXmlElementWrapper(useWrapping=false)
//	@JacksonXmlProperty(localName = XmlConstants.XML_SAME_AS)
//	public List<RdfResource> getSameAs(){
//	    	return RdfXmlUtils.convertToRdfResource(getAgent().getSameAs());
//	}

	@JsonIgnore
	private Agent getAgent() {
	    return (Agent)entity;
	}

	@Override
	@JsonIgnore
	protected EntityTypes getTypeEnum() {
	    return EntityTypes.Agent;
	}
	
}
