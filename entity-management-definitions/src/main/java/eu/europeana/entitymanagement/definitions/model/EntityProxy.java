package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ENTITY;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ID;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.PROXY;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.PROXY_FOR;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.PROXY_IN;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.TYPE;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import dev.morphia.annotations.Embedded;

@Embedded
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ID, TYPE, ENTITY, PROXY_FOR, PROXY_IN})
public class EntityProxy {

  String proxyId;
  Entity entity;
  String proxyFor;
  Aggregation proxyIn;
  String type;
  
  public EntityProxy() {
  }

  @JsonGetter(TYPE)
  public String getType() {
    return PROXY;
  }

  @JsonGetter(ID)
  public String getProxyId() {
    return proxyId;
  }

  @JsonSetter(ID)
  public void setProxyId(String proxyId) {
    this.proxyId = proxyId;
  }

  @JsonIgnore
  public Entity getEntity() {
    return entity;
  }

  public void setEntity(Entity entity) {
    this.entity = entity;
  }

  @JsonGetter(PROXY_FOR)
  public String getProxyFor() {
    return proxyFor;
  }

  @JsonSetter(PROXY_FOR)
  public void setProxyFor(String proxyFor) {
    this.proxyFor = proxyFor;
  }

  @JsonGetter(PROXY_IN)
  public Aggregation getProxyIn() {
    return proxyIn;
  }

  @JsonSetter(PROXY_IN)
  public void setProxyIn(Aggregation proxyIn) {
    this.proxyIn = proxyIn;
  }
}
