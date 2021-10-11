package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.definitions.EntityRecordFields.*;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.BASE_DATA_EUROPEANA_URI;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ID;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import dev.morphia.annotations.*;
import eu.europeana.entitymanagement.utils.EntityRecordWatcher;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@dev.morphia.annotations.Entity("EntityRecord")
@Indexes({
  @Index(fields = {@Field(ENTITY_EXACT_MATCH)}),
  @Index(fields = {@Field(ENTITY_SAME_AS)}),

  // temporary index for migration
  @Index(fields = @Field(ENTITY_TYPE))
})
@EntityListeners(EntityRecordWatcher.class)
public class EntityRecord {

  @Id @JsonIgnore private ObjectId dbId;

  @Indexed(options = @IndexOptions(unique = true))
  private String entityId;

  private Entity entity;

  private final List<EntityProxy> proxies = new ArrayList<>();

  @JsonIgnore private boolean disabled;

  @JsonIgnore private Date created;

  @JsonIgnore @Indexed private Date modified;

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
    return this.disabled;
  }

  public void setDisabled(boolean disabledParam) {
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

  public List<EntityProxy> getExternalProxies() {
    return proxies.stream()
        .filter(s -> !s.getProxyId().startsWith(BASE_DATA_EUROPEANA_URI))
        .collect(Collectors.toList());
  }
}
