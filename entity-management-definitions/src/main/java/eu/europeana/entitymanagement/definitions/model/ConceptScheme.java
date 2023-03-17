package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityConstants.ENTITY_CONTEXT;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.CONTEXT;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.CREATED;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.DEFINITION;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ID;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.IS_DEFINED_BY;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.MODIFIED;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.PREF_LABEL;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.SUBJECT;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.TYPE;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;
import org.bson.types.ObjectId;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexed;
import eu.europeana.entitymanagement.normalization.EntityFieldsCompleteValidationGroup;
import eu.europeana.entitymanagement.normalization.EntityFieldsCompleteValidationInterface;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.ValidationEntity;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
  CONTEXT,
  ID,
  TYPE,
  PREF_LABEL,
  DEFINITION,
  SUBJECT,
  IS_DEFINED_BY,
  CREATED,
  MODIFIED
})
@dev.morphia.annotations.Entity
@EntityFieldsCompleteValidationInterface(groups = {EntityFieldsCompleteValidationGroup.class})
public class ConceptScheme implements ValidationEntity{
  
  @Id @JsonIgnore private ObjectId dbId;
  
  private String type = EntityTypes.ConceptScheme.name();
  
  @Indexed(options = @IndexOptions(unique = true))
  private String entityId;
  
  private Map<String, String> prefLabel;
  private Map<String, String> definition;
  private String isDefinedBy;
  private String subject;
  private Date created;
  private Date modified;

  public ConceptScheme() {
  }

  @JsonGetter(CONTEXT)
  public String getContext() {
    return ENTITY_CONTEXT;
  }
  
  @JsonGetter(ID)
  public String getEntityId() {
    return entityId;
  }

  @JsonSetter(ID)
  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }
  
  @JsonGetter(WebEntityFields.PREF_LABEL)
  public Map<String, String> getPrefLabel() {
    return prefLabel;
  }

  @JsonSetter(WebEntityFields.PREF_LABEL)
  public void setPrefLabel(Map<String, String> prefLabel) {
    this.prefLabel = prefLabel;
  }

  @JsonGetter(DEFINITION)
  public Map<String, String> getDefinition() {
    return definition;
  }

  @JsonSetter(DEFINITION)
  public void setDefinition(Map<String, String> definition) {
    this.definition = definition;
  }

  @JsonGetter(TYPE)
  public String getType() {
    return type;
  }

  @JsonGetter(IS_DEFINED_BY)
  public String getIsDefinedBy() {
    return isDefinedBy;
  }

  @JsonSetter(IS_DEFINED_BY)
  public void setIsDefinedBy(String isDefinedBy) {
    this.isDefinedBy = isDefinedBy;
  }

  @JsonGetter(SUBJECT)
  public String getSubject() {
    return subject;
  }

  @JsonSetter(SUBJECT)
  public void setSubject(String subject) {
    this.subject = subject;
  }

  @JsonGetter(CREATED)
  public Date getCreated() {
    return created;
  }

  @JsonSetter(CREATED)
  public void setCreated(Date created) {
    this.created = created;
  }

  @JsonGetter(MODIFIED)
  public Date getModified() {
    return modified;
  }

  @JsonSetter(MODIFIED)
  public void setModified(Date modified) {
    this.modified = modified;
  }

  public Object getFieldValue(Field field) throws IllegalArgumentException, IllegalAccessException {
    // TODO:in case of the performance overhead cause by using the reflecion code, change this
    // method to call the getters for each field individually
    return field.get(this);
  }

  public void setFieldValue(Field field, Object value)
      throws IllegalArgumentException, IllegalAccessException {
    // TODO:in case of the performance overhead cause by using the reflecion code, change this
    // method to call the setter for each field individually
    field.set(this, value);
  }
  
}
