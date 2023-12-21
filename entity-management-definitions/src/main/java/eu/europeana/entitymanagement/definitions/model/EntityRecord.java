package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.definitions.EntityRecordFields.ENTITY_EXACT_MATCH;
import static eu.europeana.entitymanagement.definitions.EntityRecordFields.ENTITY_SAME_AS;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.BASE_DATA_EUROPEANA_URI;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ID;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import dev.morphia.annotations.EntityListeners;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexed;
import dev.morphia.annotations.Indexes;
import eu.europeana.entitymanagement.utils.EntityRecordWatcher;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@dev.morphia.annotations.Entity("EntityRecord")
@Indexes({@Index(fields = {@Field(ENTITY_EXACT_MATCH)}), @Index(fields = {@Field(ENTITY_SAME_AS)})})
@EntityListeners(EntityRecordWatcher.class)
public class EntityRecord {

  @Id @JsonIgnore private ObjectId dbId;

  @Indexed(options = @IndexOptions(unique = true))
  private String entityId;

  private Entity entity;

  private final List<EntityProxy> proxies = new ArrayList<>();

  @JsonIgnore private Date disabled;

  @JsonIgnore private Date created;

  @JsonIgnore private Date modified;
  
  public EntityRecord() {
  }

  @JsonGetter
  public Entity getEntity() {
    return entity;
  }

  @JsonSetter
  public void setEntity(Entity entity) {
    this.entity = entity;
  }

  @JsonGetter(ID)
  public String getEntityId() {
    return entityId;
  }

  @JsonSetter(ID)
  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  @JsonGetter
  public List<EntityProxy> getProxies() {
    return proxies;
  }
  
  @JsonSetter
  public void addProxy(EntityProxy proxy) {
    this.proxies.add(proxy);
  }

  public void setDbId(ObjectId dbId_param) {
    this.dbId = dbId_param;
  }

  public ObjectId getDbId() {
    return dbId;
  }

  public boolean isDisabled() {
    return disabled != null;
  }

  public Date getDisabled() {
    return disabled;
  }

  public void setDisabled(Date disabledParam) {
    this.disabled = disabledParam;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public Date getCreated() {
    return this.created;
  }

  public void setModified(Date modified) {
    this.modified = modified;
  }

  public Date getModified() {
    return this.modified;
  }

  public EntityProxy getEuropeanaProxy() {
    return proxies.stream()
        .filter(s -> s.getProxyId().startsWith(BASE_DATA_EUROPEANA_URI))
        .findFirst()
        .orElse(null);
  }

  public EntityProxy getZohoProxy() {
    return getProxyByHost(WebEntityFields.ZOHO_CRM_HOST);
  }

  public EntityProxy getWikidataProxy() {
    return getProxyByHost(WebEntityFields.WIKIDATA_HOST);
  }

  EntityProxy getProxyByHost(String host_name) {
    return proxies.stream()
        .filter(
            s -> {
              return s.getProxyId().contains(host_name);
            })
        .findFirst()
        .orElse(null);
  }

  public List<EntityProxy> getExternalProxies() {
    return proxies.stream()
        .filter(s -> !s.getProxyId().startsWith(BASE_DATA_EUROPEANA_URI))
        .collect(Collectors.toList());
  }

  public List<String> getExternalProxyIds() {
    return proxies.stream()
        .filter(s -> !s.getProxyId().startsWith(BASE_DATA_EUROPEANA_URI))
        .map(EntityProxy::getProxyId)
        .collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return String.format("EntityRecord.entityTd: %s", getEntityId());
  }
}
