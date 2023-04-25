package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityConstants.ENTITY_CONTEXT;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.CONTEXT;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.CREATED;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.DEFINITION;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ID;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ITEMS;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.MODIFIED;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.PREF_LABEL;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.SUBJECT;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.TOTAL;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.TYPE;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexed;
import dev.morphia.annotations.Transient;
import eu.europeana.entitymanagement.normalization.EntityFieldsCompleteValidationGroup;
import eu.europeana.entitymanagement.normalization.EntityFieldsCompleteValidationInterface;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.ValidationObject;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;

@JsonIgnoreProperties(ignoreUnknown = false)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
  CONTEXT,
  ID,
  TYPE,
  PREF_LABEL,
  DEFINITION,
  SUBJECT,
  TOTAL,
  ITEMS,
  CREATED,
  MODIFIED
})
@dev.morphia.annotations.Entity("ConceptScheme")
@EntityFieldsCompleteValidationInterface(groups = {EntityFieldsCompleteValidationGroup.class})
public class ConceptScheme implements ValidationObject {

  @Id @JsonIgnore private ObjectId dbId;

  private String type = EntityTypes.ConceptScheme.getEntityType();

  @Indexed(options = @IndexOptions(unique = true))
  private Long identifier;

  @Transient private String conceptSchemeId;

  private Map<String, String> prefLabel;
  private Map<String, String> definition;
  private String subject;
  private Date created;
  private Date modified;
  @JsonIgnore private Date disabled;
  private int total;
  private List<String> items;

  public ConceptScheme() {}

  public boolean isDisabled() {
    return disabled != null;
  }

  public void setDisabled(Date disabledParam) {
    this.disabled = disabledParam;
  }

  @JsonGetter(CONTEXT)
  public String getContext() {
    return ENTITY_CONTEXT;
  }

  @JsonIgnore
  public Long getIdentifier() {
    return identifier;
  }

  @JsonIgnore
  public void setIdentifier(Long identifier) {
    this.identifier = identifier;
  }

  @JsonGetter(ID)
  public String getConceptSchemeId() {
    return conceptSchemeId;
  }

  public void setConceptSchemeId(String conceptSchemeId) {
    this.conceptSchemeId = conceptSchemeId;
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

  @JsonGetter(TOTAL)
  public int getTotal() {
    return total;
  }

  @JsonSetter(TOTAL)
  public void setTotal(int total) {
    this.total = total;
  }

  @JsonGetter(ITEMS)
  public List<String> getItems() {
    return items;
  }

  @JsonSetter(ITEMS)
  public void setItems(List<String> items) {
    this.items = items;
  }

  public Object getFieldValue(Field field) throws IllegalArgumentException, IllegalAccessException {
    return field.get(this);
  }

  public void setFieldValue(Field field, Object value)
      throws IllegalArgumentException, IllegalAccessException {
    field.set(this, value);
  }
}
