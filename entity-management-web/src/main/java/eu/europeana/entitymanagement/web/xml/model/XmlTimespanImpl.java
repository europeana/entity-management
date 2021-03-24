package eu.europeana.entitymanagement.web.xml.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.europeana.entitymanagement.definitions.model.Timespan;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;



@JacksonXmlRootElement(localName = XmlConstants.XML_TIMESPAN)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({XmlConstants.DEPICTION, XmlConstants.PREF_LABEL, XmlConstants.ALT_LABEL, XmlConstants.XML_SKOS_HIDDEN_LABEL,
    	XmlConstants.XML_BEGIN,XmlConstants.XML_END,XmlConstants.XML_IS_PART_OF,XmlConstants.XML_SAME_AS,
    	XmlConstants.XML_EDM_WEB_RESOURCE, XmlConstants.XML_IS_NEXT_IN_SEQUENCE})
public class XmlTimespanImpl extends XmlBaseEntityImpl {
    	
    	public XmlTimespanImpl(Timespan timespan) {
    	    	super(timespan);
    	}

	public XmlTimespanImpl() {
		// default constructor
	}

	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_IS_PART_OF)
	public List<RdfResource> getIsPartOf() {
	    	return RdfXmlUtils.convertToRdfResource(((Timespan)entity).getIsPartOfArray());
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_BEGIN)
	public String getBegin() {
	    	return ((Timespan)entity).getBeginString();
	}

	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_END)
	public String getEnd() {
	    	return ((Timespan)entity).getEndString();
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_SKOS_HIDDEN_LABEL)
	public List<XmlMultilingualString> getHiddenLabel() {
		return RdfXmlUtils.convertToXmlMultilingualString(entity.getHiddenLabel());
	}

	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_IS_NEXT_IN_SEQUENCE)
	public List<RdfResource> getIsNextInSequence() {
	    	return RdfXmlUtils.convertToRdfResource(((Timespan)entity).getIsNextInSequence());
	}

	@Override
	@JsonIgnore
	protected EntityTypes getTypeEnum() {
	    return EntityTypes.Timespan;
	}

	
}
