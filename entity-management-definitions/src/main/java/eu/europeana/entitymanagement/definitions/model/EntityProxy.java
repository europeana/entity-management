package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.PROXY;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;

import dev.morphia.annotations.Embedded;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;

@Embedded
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({WebEntityFields.ID, WebEntityFields.TYPE, WebEntityFields.ENTITY, WebEntityFields.PROXY_FOR, WebEntityFields.PROXY_IN})
public class EntityProxy {

    String proxyId;
    Entity entity;
    String proxyFor;
    Aggregation proxyIn;
    String type;

    @JsonGetter(WebEntityFields.TYPE)
    public String getType() {
        return PROXY;
    }


    @JsonGetter(WebEntityFields.ID)
    public String getProxyId() {
        return proxyId;
    }

    @JsonSetter(WebEntityFields.ID)
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

    @JsonGetter(WebEntityFields.PROXY_FOR)
    public String getProxyFor() {
        return proxyFor;
    }

    @JsonSetter(WebEntityFields.PROXY_FOR)
    public void setProxyFor(String proxyFor) {
        this.proxyFor = proxyFor;
    }

    @JsonGetter(WebEntityFields.PROXY_IN)
    public Aggregation getProxyIn() {
        return proxyIn;
    }

    @JsonSetter(WebEntityFields.PROXY_IN)
    public void setProxyIn(Aggregation proxyIn) {
        this.proxyIn = proxyIn;
    }
}
