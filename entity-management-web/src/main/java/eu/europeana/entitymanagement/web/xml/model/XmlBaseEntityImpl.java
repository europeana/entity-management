package eu.europeana.entitymanagement.web.xml.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import eu.europeana.entitymanagement.definitions.model.Entity;

public abstract class XmlBaseEntityImpl {

    @XmlTransient
    Entity entity;
    @XmlTransient
    String aggregationId;
    /**
     * relatedElementsToSerialize - this list is maintained by each serialized
     * entity and contains the entities that need to be serialized in addition,
     * outside of the given entity
     */
    @XmlTransient
    List<Object> referencedWebResources;

    private String about;
    private List<XmlMultilingualString> altLabel = new ArrayList<>();
    private List<XmlMultilingualString> prefLabel = new ArrayList<>();
    private List<RdfResource> sameAs = new ArrayList<>();

    public XmlBaseEntityImpl() {
	// default constructor
    }

    public List<Object> getReferencedWebResources() {
	return referencedWebResources;
    }

    public XmlBaseEntityImpl(Entity entity) {
	this.entity = entity;
	this.about = entity.getAbout();
	aggregationId = entity.getAbout() + "#aggregation";
	referencedWebResources = new ArrayList<Object>();
	this.prefLabel = RdfXmlUtils.convertToXmlMultilingualString(entity.getPrefLabel());
	this.altLabel = RdfXmlUtils.convertToXmlMultilingualString(entity.getAltLabel());
	this.sameAs = RdfXmlUtils.convertToRdfResource(entity.getSameAs());
    }

    @XmlAttribute(namespace = XmlConstants.NAMESPACE_RDF, name = XmlConstants.ABOUT)
    public String getAbout() {
	return this.about;
    }

    public void setAbout(String about) {
	this.about = about;
    }

//	@XmlElement(namespace = XmlConstants.NAMESPACE_OWL, name = XmlConstants.IS_AGGREGATED_BY)
//	public XmlIsAggregatedByImpl getIsAggregatedBy() {
//	    	if(entity.getCreated() == null && entity.getModified() == null)
//	    	    return null;
//		return new XmlIsAggregatedByImpl(aggregationId);
//	}
//	
    public XmlAggregationImpl createXmlAggregation() {
//	    	if(entity.getCreated() == null && entity.getModified() == null)
//	    	    return null;
	return new XmlAggregationImpl(entity);
    }

    @XmlElement(namespace = XmlConstants.NAMESPACE_FOAF, name = XmlConstants.DEPICTION)
    public EdmWebResource getDepiction() {
	if (entity.getDepiction() == null)
	    return null;
	return new EdmWebResource(entity.getDepiction());
    }

    @XmlElement(namespace = XmlConstants.NAMESPACE_SKOS, name = XmlConstants.PREF_LABEL)
    public List<XmlMultilingualString> getPrefLabel() {
	return this.prefLabel;
    }

    @XmlElement(namespace = XmlConstants.NAMESPACE_SKOS, name = XmlConstants.ALT_LABEL)
    public List<XmlMultilingualString> getAltLabel() {
	return this.altLabel;
    }

    @XmlElement(namespace = XmlConstants.NAMESPACE_OWL, name = XmlConstants.XML_OWL_SAME_AS)
    public List<RdfResource> getSameAs() {
	return this.sameAs;
    }

    @XmlElement(namespace = XmlConstants.NAMESPACE_EDM, name = XmlConstants.IS_SHOWN_BY)
    @Deprecated
    /**
     * 
     * @deprecated
     */
    public RdfResource getIsShownBy() {

	if (entity.getReferencedWebResource() != null) {
//		    referencedWebResources.add(new XmlWebResourceImpl(((BaseEntity)entity).getIsShownById(),((BaseEntity)entity).getIsShownBySource(), ((BaseEntity)entity).getIsShownByThumbnail()));
//	        return new RdfResource(((BaseEntity)entity).getIsShownById());
	    referencedWebResources.add(new XmlWebResourceImpl(entity.getReferencedWebResource().getId(),
		    entity.getReferencedWebResource().getSource(), entity.getReferencedWebResource().getThumbnail()));
	    return new RdfResource(entity.getReferencedWebResource().getId());
	} else {
	    return null;
	}
    }

}
