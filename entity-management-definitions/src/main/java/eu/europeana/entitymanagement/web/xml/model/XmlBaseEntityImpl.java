package eu.europeana.entitymanagement.web.xml.model;

import eu.europeana.entitymanagement.definitions.exceptions.EntityCreationException;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.utils.EntityObjectFactory;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.*;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class XmlBaseEntityImpl<T extends Entity> {

    @XmlTransient
    protected T entity;

    @XmlAttribute(namespace = NAMESPACE_RDF, name = XmlConstants.ABOUT)
    private String about;

    @XmlElement(namespace = XmlConstants.NAMESPACE_EDM, name = IS_SHOWN_BY)
    private XmlWebResourceImpl isShownBy;

    @XmlElement(namespace = NAMESPACE_FOAF, name = DEPICTION)
    private XmlWebResourceImpl depiction;


  @XmlElement(namespace = XmlConstants.NAMESPACE_SKOS, name = ALT_LABEL)
  private List<LabelledResource> altLabel = new ArrayList<>();

    @XmlElement(namespace = XmlConstants.NAMESPACE_SKOS, name = PREF_LABEL)
    private List<LabelledResource> prefLabel = new ArrayList<>();


    @XmlElement(namespace = XmlConstants.NAMESPACE_ORE, name = XmlConstants.IS_AGGREGATED_BY)
    private XmlAggregationImpl isAggregatedBy;

    public XmlBaseEntityImpl() {
	// default constructor
    }

    public XmlWebResourceImpl getIsShownBy() {
	return isShownBy;
    }

    public XmlBaseEntityImpl(T entity) {
	this.entity = entity;
	this.about = entity.getAbout();
	this.prefLabel = RdfXmlUtils.convertMapToXmlMultilingualString(entity.getPrefLabel());
	this.altLabel = RdfXmlUtils.convertToXmlMultilingualString(entity.getAltLabel());
	setSameReferenceLinks(RdfXmlUtils.convertToRdfResource(entity.getSameReferenceLinks()));
	// isAggregatedBy not always set in tests
    // TODO: fix tests, then remove null check here
    if(entity.getIsAggregatedBy() != null) {
        this.isAggregatedBy = new XmlAggregationImpl(entity.getIsAggregatedBy());
    }

    if(entity.getIsShownBy() != null){
        isShownBy = XmlWebResourceImpl.fromWebResource(entity.getIsShownBy());
    }

    if(entity.getDepiction() != null){
        depiction = XmlWebResourceImpl.fromWebResource(entity.getDepiction());
    }

    }

    public T toEntityModel() throws EntityCreationException {
	if(entity == null) {
	    entity = EntityObjectFactory.createProxyEntityObject(getTypeEnum().getEntityType());
	}
	entity.setEntityId(getAbout());
	entity.setPrefLabel(RdfXmlUtils.toLanguageMap(getPrefLabel()));
	entity.setAltLabel(RdfXmlUtils.toLanguageMapList(getAltLabel()));
	// sets sameAs or exactMatch values (for concepts)
	entity.setSameReferenceLinks(RdfXmlUtils.toStringList(getSameReferenceLinks()));
	if(depiction != null && !depiction.isEmpty()) {
	    entity.setDepiction(XmlWebResourceImpl.toWebResource(depiction));
	}
	return entity;
    }

  protected abstract EntityTypes getTypeEnum();

    public String getAbout() {
	return this.about;
    }

    public void setAbout(String about) {
	this.about = about;
    }


  public XmlWebResourceImpl getDepiction() {
    return depiction;
  }


    public List<LabelledResource> getPrefLabel() {
	return this.prefLabel;
    }

    public List<LabelledResource> getAltLabel() {
	return this.altLabel;
    }

    public T getEntity() {
	return entity;
    }

    public abstract List<LabelledResource> getSameReferenceLinks();
    public abstract void setSameReferenceLinks(List<LabelledResource> uris);
}
