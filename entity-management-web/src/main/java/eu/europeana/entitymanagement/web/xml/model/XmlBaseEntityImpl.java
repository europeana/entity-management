package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.ALT_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.DEPICTION;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_RDF;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.PREF_LABEL;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.exception.EntityCreationException;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.web.service.EntityObjectFactory;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import org.springframework.util.StringUtils;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class XmlBaseEntityImpl<T extends Entity> {

    @XmlTransient
    protected T entity;
    @XmlTransient
    String aggregationId;
    /**
     * relatedentityElementsToSerialize - this list is maintained by each serialized
     * entity and contains the entities that need to be serialized in addition,
     * outside of the given entity
     */
    @XmlTransient
    List<XmlWebResourceImpl> referencedWebResources;

    @XmlAttribute(namespace = NAMESPACE_RDF, name = XmlConstants.ABOUT)
    private String about;

  @XmlElement(namespace = XmlConstants.NAMESPACE_SKOS, name = ALT_LABEL)
  private List<LabelledResource> altLabel = new ArrayList<>();

    @XmlElement(namespace = XmlConstants.NAMESPACE_SKOS, name = PREF_LABEL)
    private List<LabelledResource> prefLabel = new ArrayList<>();

  @XmlElement(namespace = XmlConstants.NAMESPACE_OWL, name = XmlConstants.XML_SAME_AS)
  private List<LabelledResource> sameAs = new ArrayList<>();

  @XmlElement(namespace = XmlConstants.NAMESPACE_FOAF, name = DEPICTION)
  private LabelledResource depiction;

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
	if(StringUtils.hasLength(entity.getDepiction())){
    this.depiction = new LabelledResource(entity.getDepiction());
  }
	
	aggregationId = entity.getAbout() + "#aggregation";
	referencedWebResources = new ArrayList<>();
    }

    public T toEntityModel() throws EntityCreationException {
	if(entity == null) {
	    entity = EntityObjectFactory.createEntityObject(getTypeEnum());
	}
	entity.setType(getTypeEnum().getEntityType());
	
	entity.setEntityId(getAbout());
	entity.setPrefLabelStringMap(RdfXmlUtils.toLanguageMap(getPrefLabel()));
	entity.setAltLabel(RdfXmlUtils.toLanguageMapList(getAltLabel()));
	entity.setSameAs(RdfXmlUtils.toStringList(getSameAs()));
	if(depiction != null) {
	    entity.setDepiction(depiction.getResource());
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


  public LabelledResource getDepiction() {
    return depiction;
  }


    public List<LabelledResource> getPrefLabel() {
	return this.prefLabel;
    }

    public List<LabelledResource> getAltLabel() {
	return this.altLabel;
    }

    public List<LabelledResource> getSameAs() {
	return this.sameAs;
    }

    public T getEntity() {
	return entity;
    }

}
