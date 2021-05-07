package eu.europeana.entitymanagement.web.xml.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.exception.EntityCreationException;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.web.service.EntityObjectFactory;

public abstract class XmlBaseEntityImpl<T extends Entity> {

    @XmlTransient
    T entity;
    @XmlTransient
    String aggregationId;
    /**
     * relatedElementsToSerialize - this list is maintained by each serialized
     * entity and contains the entities that need to be serialized in addition,
     * outside of the given entity
     */
    @XmlTransient
    List<XmlWebResourceImpl> referencedWebResources;

    private String about;
    private List<LabelledResource> altLabel = new ArrayList<>();
    private List<LabelledResource> prefLabel = new ArrayList<>();
    private List<LabelledResource> sameAs = new ArrayList<>();

    public XmlBaseEntityImpl() {
	// default constructor
    }

    public List<XmlWebResourceImpl> getReferencedWebResources() {
	return referencedWebResources;
    }

    public XmlBaseEntityImpl(T entity) {
	this.entity = entity;
	this.about = entity.getAbout();
	this.prefLabel = RdfXmlUtils.convertToXmlMultilingualString(entity.getPrefLabel());
	this.altLabel = RdfXmlUtils.convertToXmlMultilingualString(entity.getAltLabel());
	this.sameAs = RdfXmlUtils.convertToRdfResource(entity.getSameAs());
	
	aggregationId = entity.getAbout() + "#aggregation";
	referencedWebResources = new ArrayList<>();
    }

    public T toEntityModel() throws EntityCreationException {
	if(entity == null) {
	    this.entity = EntityObjectFactory.createEntityObject(getTypeEnum());
	}
	entity.setType(getTypeEnum().getEntityType());
	
	entity.setEntityId(getAbout());
	entity.setPrefLabelStringMap(RdfXmlUtils.toLanguageMap(getPrefLabel()));
	entity.setAltLabel(RdfXmlUtils.toLanguageMapList(getAltLabel()));
	entity.setSameAs(RdfXmlUtils.toStringArray(getSameAs()));
	if(getDepiction() != null) {
	    entity.setDepiction(getDepiction().getResource().getAbout());
	}
	return entity;
    }

    protected abstract EntityTypes getTypeEnum();

    @XmlAttribute(namespace = XmlConstants.NAMESPACE_RDF, name = XmlConstants.ABOUT)
    public String getAbout() {
	return this.about;
    }

    public void setAbout(String about) {
	this.about = about;
    }


    public XmlAggregationImpl createXmlAggregation() {
	return new XmlAggregationImpl(entity);
    }

    @XmlElement(namespace = XmlConstants.NAMESPACE_FOAF, name = XmlConstants.DEPICTION)
    public EdmWebResource getDepiction() {
	if (entity.getDepiction() == null) {
    return null;
  }
	return new EdmWebResource(entity.getDepiction());
    }

    @XmlElement(namespace = XmlConstants.NAMESPACE_SKOS, name = XmlConstants.PREF_LABEL)
    public List<LabelledResource> getPrefLabel() {
	return this.prefLabel;
    }

    @XmlElement(namespace = XmlConstants.NAMESPACE_SKOS, name = XmlConstants.ALT_LABEL)
    public List<LabelledResource> getAltLabel() {
	return this.altLabel;
    }

    @XmlElement(namespace = XmlConstants.NAMESPACE_OWL, name = XmlConstants.XML_SAME_AS)
    public List<LabelledResource> getSameAs() {
	return this.sameAs;
    }

    @XmlElement(namespace = XmlConstants.NAMESPACE_EDM, name = XmlConstants.IS_SHOWN_BY)
    @Deprecated
    /**
     * 
     * @deprecated
     */
    public LabelledResource getIsShownBy() {

	if (entity.getReferencedWebResource() != null) {
	    referencedWebResources.add(new XmlWebResourceImpl(entity.getReferencedWebResource().getId(),
		    entity.getReferencedWebResource().getSource(), entity.getReferencedWebResource().getThumbnail()));
	    return new LabelledResource(entity.getReferencedWebResource().getId());
	} else {
	    return null;
	}
    }

    public T getEntity() {
	return entity;
    }

}
