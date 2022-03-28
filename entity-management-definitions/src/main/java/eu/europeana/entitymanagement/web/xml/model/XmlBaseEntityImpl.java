package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.ALT_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.DEPICTION;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.HIDDEN_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.IS_SHOWN_BY;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_FOAF;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_RDF;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_SKOS;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.PREF_LABEL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import eu.europeana.entitymanagement.definitions.exceptions.EntityCreationException;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.utils.EntityObjectFactory;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlTransient
public abstract class XmlBaseEntityImpl<T extends Entity> {

  @XmlTransient protected T entity;

  @XmlAttribute(namespace = NAMESPACE_RDF, name = XmlConstants.ABOUT)
  private String about;

  @XmlElement(namespace = XmlConstants.NAMESPACE_EDM, name = IS_SHOWN_BY)
  private XmlWebResourceWrapper isShownBy;

  @XmlElement(namespace = NAMESPACE_FOAF, name = DEPICTION)
  private XmlWebResourceWrapper depiction;

  @XmlElement(namespace = XmlConstants.NAMESPACE_SKOS, name = ALT_LABEL)
  private List<LabelledResource> altLabel = new ArrayList<>();

  @XmlElement(namespace = XmlConstants.NAMESPACE_SKOS, name = PREF_LABEL)
  private List<LabelledResource> prefLabel = new ArrayList<>();

  @XmlElement(namespace = NAMESPACE_SKOS, name = HIDDEN_LABEL)
  private List<String> hiddenLabel;

  @XmlElement(namespace = XmlConstants.NAMESPACE_ORE, name = XmlConstants.IS_AGGREGATED_BY)
  private XmlAggregationImpl isAggregatedBy;

  protected XmlBaseEntityImpl() {
    // default constructor
  }

  public XmlWebResourceWrapper getIsShownBy() {
    return isShownBy;
  }

  protected XmlBaseEntityImpl(T entity) {
    this.entity = entity;
    this.about = entity.getAbout();
    this.prefLabel = RdfXmlUtils.convertMapToXmlMultilingualString(entity.getPrefLabel());
    this.altLabel = RdfXmlUtils.convertToXmlMultilingualString(entity.getAltLabel());
    if (entity.getHiddenLabel() != null) {
      this.hiddenLabel = new ArrayList<String>(entity.getHiddenLabel());
    }
    if (entity.getIsAggregatedBy() != null) {
      this.isAggregatedBy = new XmlAggregationImpl(entity.getIsAggregatedBy());
    }

    this.isShownBy = XmlWebResourceWrapper.fromWebResource(entity.getIsShownBy());
    this.depiction = XmlWebResourceWrapper.fromWebResource(entity.getDepiction());
  }

  public T toEntityModel() throws EntityCreationException {
    if (entity == null) {
      entity = EntityObjectFactory.createProxyEntityObject(getTypeEnum().getEntityType());
    }
    entity.setEntityId(getAbout());
    entity.setPrefLabel(RdfXmlUtils.toLanguageMap(getPrefLabel()));
    entity.setAltLabel(RdfXmlUtils.toLanguageMapList(getAltLabel()));
    entity.setHiddenLabel(getHiddenLabel());
    // sets sameAs or exactMatch values (for concepts)
    entity.setSameReferenceLinks(RdfXmlUtils.toStringList(getSameReferenceLinks()));
    entity.setDepiction(XmlWebResourceWrapper.toWebResource(depiction));
    return entity;
  }

  protected abstract EntityTypes getTypeEnum();

  public String getAbout() {
    return this.about;
  }

  public void setAbout(String about) {
    this.about = about;
  }

  public XmlWebResourceWrapper getDepiction() {
    return depiction;
  }

  public List<LabelledResource> getPrefLabel() {
    return this.prefLabel;
  }

  public List<LabelledResource> getAltLabel() {
    return this.altLabel;
  }

  public List<String> getHiddenLabel() {
    return hiddenLabel;
  }

  public T getEntity() {
    return entity;
  }

  public abstract List<LabelledResource> getSameReferenceLinks();

  public abstract void setSameReferenceLinks(List<LabelledResource> uris);

  public boolean hasCoref(String uri) {
    if (uri == null || getSameReferenceLinks() == null || getSameReferenceLinks().isEmpty()) {
      return false;
    }

    return getSameReferenceLinks().stream().anyMatch(e -> uri.equals(e.getResource()));
  }
}
