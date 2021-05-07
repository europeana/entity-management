package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.ABOUT;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.ALT_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.BROADER;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.BROAD_MATCH;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.CLOSE_MATCH;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.DEPICTION;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.EXACT_MATCH;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.HIDDEN_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.IN_SCHEMA;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.IS_AGGREGATED_BY;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_SKOS;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NARROWER;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NARROW_MATCH;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NOTATION;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NOTE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.PREF_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.RELATED;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.RELATED_MATCH;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_SAME_AS;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_SKOS_CONCEPT;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.exception.EntityCreationException;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

@JacksonXmlRootElement(localName = XML_SKOS_CONCEPT)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ ABOUT, DEPICTION, PREF_LABEL, ALT_LABEL, HIDDEN_LABEL, NOTE, NOTATION, BROADER, NARROWER, RELATED,
        BROAD_MATCH, NARROW_MATCH, RELATED_MATCH, CLOSE_MATCH, EXACT_MATCH, IN_SCHEMA, XML_SAME_AS, IS_AGGREGATED_BY })
public class XmlConceptImpl extends XmlBaseEntityImpl<Concept> {

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
    	this.entity = new Concept();
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

    public Concept toEntityModel() throws EntityCreationException {
	super.toEntityModel();

	entity.setRelated(RdfXmlUtils.toStringArray(getRelated()));
        entity.setNarrower(RdfXmlUtils.toStringArray(getNarrower()));
        entity.setBroader(RdfXmlUtils.toStringArray(getBroader()));
        entity.setBroadMatch(RdfXmlUtils.toStringArray(getBroadMatch()));
        entity.setNarrowMatch(RdfXmlUtils.toStringArray(getNarrowMatch()));
        entity.setExactMatch(RdfXmlUtils.toStringArray(getExactMatch()));
        entity.setRelatedMatch(RdfXmlUtils.toStringArray(getRelatedMatch()));
        entity.setCloseMatch(RdfXmlUtils.toStringArray(getCloseMatch()));
        entity.setInScheme(RdfXmlUtils.toStringArray(getInScheme()));
        entity.setNote(RdfXmlUtils.toLanguageMapList(getNote()));
        entity.setNotation(RdfXmlUtils.toLanguageMapList(getNotation()));
        entity.setHiddenLabel(RdfXmlUtils.toLanguageMapList(getHiddenLabel()));
        this.inScheme = RdfXmlUtils.convertToRdfResource(entity.getInScheme());
        
        return entity;
    }

    @XmlElement(namespace = NAMESPACE_SKOS, name = BROADER)
    public List<LabelledResource> getBroader() {
        return this.broader;
    }


    @XmlElement(namespace = NAMESPACE_SKOS, name = NARROWER)
    public List<LabelledResource> getNarrower() {
        return this.narrower;
    }


    @XmlElement(namespace = NAMESPACE_SKOS, name = RELATED)
    public List<LabelledResource> getRelated() {
        return this.related;
    }

    @XmlElement(namespace = NAMESPACE_SKOS, name = BROAD_MATCH)
    public List<LabelledResource> getBroadMatch() {
        return this.broadMatch;
    }

    @XmlElement(namespace = NAMESPACE_SKOS, name = NARROW_MATCH)
    public List<LabelledResource> getNarrowMatch() {
        return this.narrowMatch;
    }


    @XmlElement(namespace = NAMESPACE_SKOS, name = EXACT_MATCH)
    public List<LabelledResource> getExactMatch() {
        return this.exactMatch;
    }

    @XmlElement(namespace = NAMESPACE_SKOS, name = RELATED_MATCH)
    public List<LabelledResource> getRelatedMatch() {
        return this.relatedMatch;
    }

    @XmlElement(namespace = NAMESPACE_SKOS, name = CLOSE_MATCH)
    public List<LabelledResource> getCloseMatch() {
        return this.closeMatch;
    }

    @XmlElement(namespace = NAMESPACE_SKOS, name = NOTATION)
    public List<LabelledResource> getNotation() {
        return this.notation;
    }

    @XmlElement(namespace = NAMESPACE_SKOS, name = HIDDEN_LABEL)
    public List<LabelledResource> getHiddenLabel() {
        return this.hiddenLabel;
    }

    @XmlElement(namespace = NAMESPACE_SKOS, name = NOTE)
    public List<LabelledResource> getNote() {
        return note;
    }


    @XmlElement(namespace = NAMESPACE_SKOS, name = IN_SCHEMA)
    public List<LabelledResource> getInScheme() {
        return this.inScheme;
    }

    @Override
    @JsonIgnore
    protected EntityTypes getTypeEnum() {
	return EntityTypes.Concept;
    }
}
