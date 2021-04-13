package eu.europeana.entitymanagement.web.xml.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.impl.ConceptImpl;
import eu.europeana.entitymanagement.exception.EntityCreationException;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

@JacksonXmlRootElement(localName = XmlConstants.XML_SKOS_CONCEPT)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({XmlConstants.ABOUT, XmlConstants.DEPICTION, XmlConstants.PREF_LABEL, XmlConstants.ALT_LABEL, XmlConstants.HIDDEN_LABEL,
        XmlConstants.NOTE, XmlConstants.NOTATION, XmlConstants.BROADER, XmlConstants.NARROWER, XmlConstants.RELATED,
        XmlConstants.BROAD_MATCH, XmlConstants.NARROW_MATCH, XmlConstants.RELATED_MATCH, XmlConstants.CLOSE_MATCH,
        XmlConstants.EXACT_MATCH, XmlConstants.IN_SCHEMA, XmlConstants.XML_SAME_AS, XmlConstants.IS_AGGREGATED_BY})
public class XmlConceptImpl extends XmlBaseEntityImpl {

    private List<LabelledResource> narrower = new ArrayList<>();
    private List<LabelledResource> related = new ArrayList<>();
    private List<LabelledResource> broader = new ArrayList<>();
    private List<LabelledResource> note = new ArrayList<>();
    private List<LabelledResource> broadMatch = new ArrayList<>();
    private List<LabelledResource> narrowMatch = new ArrayList<>();
    private List<LabelledResource> exactMatch = new ArrayList<>();
    private List<LabelledResource> relatedMatch = new ArrayList<>();
    private List<LabelledResource> closeMatch = new ArrayList<>();
    private List<LabelledResource> notation;
    private List<LabelledResource> hiddenLabel;
    private List<LabelledResource> inScheme;

    public XmlConceptImpl() {
        // default constructor required for deserialization
    	this.entity = new ConceptImpl();
    }

    public XmlConceptImpl(Concept concept) {
        super(concept);
        this.related = RdfXmlUtils.convertToRdfResource(concept.getRelated());
        this.narrower = RdfXmlUtils.convertToRdfResource(concept.getNarrower());
        this.broader = RdfXmlUtils.convertToRdfResource(concept.getBroader());
        this.broadMatch = RdfXmlUtils.convertToRdfResource(concept.getBroadMatch());
        this.narrowMatch = RdfXmlUtils.convertToRdfResource(concept.getNarrowMatch());
        this.exactMatch = RdfXmlUtils.convertToRdfResource(concept.getExactMatch());
        this.relatedMatch = RdfXmlUtils.convertToRdfResource(concept.getRelatedMatch());
        this.closeMatch = RdfXmlUtils.convertToRdfResource(concept.getCloseMatch());
        this.note = RdfXmlUtils.convertToXmlMultilingualString(concept.getNote());
        this.notation = RdfXmlUtils.convertToXmlMultilingualString(concept.getNotation());
        this.hiddenLabel = RdfXmlUtils.convertToXmlMultilingualString(concept.getHiddenLabel());
        this.inScheme = RdfXmlUtils.convertToRdfResource(concept.getInScheme());
    }

    public Entity toEntityModel() throws EntityCreationException {
	super.toEntityModel();
	Concept concept = (Concept) getEntity(); 
	concept.setRelated(RdfXmlUtils.toStringArray(getRelated()));
        concept.setNarrower(RdfXmlUtils.toStringArray(getNarrower()));
        concept.setBroader(RdfXmlUtils.toStringArray(getBroader()));
        concept.setBroadMatch(RdfXmlUtils.toStringArray(getBroadMatch()));
        concept.setNarrowMatch(RdfXmlUtils.toStringArray(getNarrowMatch()));
        concept.setExactMatch(RdfXmlUtils.toStringArray(getExactMatch()));
        concept.setRelatedMatch(RdfXmlUtils.toStringArray(getRelatedMatch()));
        concept.setCloseMatch(RdfXmlUtils.toStringArray(getCloseMatch()));
        concept.setInScheme(RdfXmlUtils.toStringArray(getInScheme()));
        concept.setNote(RdfXmlUtils.toLanguageMapList(getNote()));
        concept.setNotation(RdfXmlUtils.toLanguageMapList(getNotation()));
        concept.setHiddenLabel(RdfXmlUtils.toLanguageMapList(getHiddenLabel()));
        this.inScheme = RdfXmlUtils.convertToRdfResource(concept.getInScheme());
        
        return getEntity();
    }

    @XmlElement(namespace = XmlConstants.NAMESPACE_SKOS, name = XmlConstants.BROADER)
    public List<LabelledResource> getBroader() {
        return this.broader;
    }


    @XmlElement(namespace = XmlConstants.NAMESPACE_SKOS, name = XmlConstants.NARROWER)
    public List<LabelledResource> getNarrower() {
        return this.narrower;
    }


    @XmlElement(namespace = XmlConstants.NAMESPACE_SKOS, name = XmlConstants.RELATED)
    public List<LabelledResource> getRelated() {
        return this.related;
    }

    @XmlElement(namespace = XmlConstants.NAMESPACE_SKOS, name = XmlConstants.BROAD_MATCH)
    public List<LabelledResource> getBroadMatch() {
        return this.broadMatch;
    }

    @XmlElement(namespace = XmlConstants.NAMESPACE_SKOS, name = XmlConstants.NARROW_MATCH)
    public List<LabelledResource> getNarrowMatch() {
        return this.narrowMatch;
    }


    @XmlElement(namespace = XmlConstants.NAMESPACE_SKOS, name = XmlConstants.EXACT_MATCH)
    public List<LabelledResource> getExactMatch() {
        return this.exactMatch;
    }

    @XmlElement(namespace = XmlConstants.NAMESPACE_SKOS, name = XmlConstants.RELATED_MATCH)
    public List<LabelledResource> getRelatedMatch() {
        return this.relatedMatch;
    }

    @XmlElement(namespace = XmlConstants.NAMESPACE_SKOS, name = XmlConstants.CLOSE_MATCH)
    public List<LabelledResource> getCloseMatch() {
        return this.closeMatch;
    }

    @XmlElement(namespace = XmlConstants.NAMESPACE_SKOS, name = XmlConstants.NOTATION)
    public List<LabelledResource> getNotation() {
        return this.notation;
    }

    @XmlElement(namespace = XmlConstants.NAMESPACE_SKOS, name = XmlConstants.HIDDEN_LABEL)
    public List<LabelledResource> getHiddenLabel() {
        return this.hiddenLabel;
    }

    @JsonIgnore
    private Concept getConcept() {
        return (Concept) entity;
    }


    @XmlElement(namespace = XmlConstants.NAMESPACE_SKOS, name = XmlConstants.NOTE)
    public List<LabelledResource> getNote() {
        return note;
    }


    @XmlElement(namespace = XmlConstants.NAMESPACE_SKOS, name = XmlConstants.IN_SCHEMA)
    public List<LabelledResource> getInScheme() {
        return this.inScheme;
    }

    @Override
    @JsonIgnore
    protected EntityTypes getTypeEnum() {
	return EntityTypes.Concept;
    }
}
