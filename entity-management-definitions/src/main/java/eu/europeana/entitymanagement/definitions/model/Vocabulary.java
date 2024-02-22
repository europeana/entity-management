package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.IN_SCHEME;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.PREF_LABEL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import dev.morphia.annotations.EntityListeners;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexed;
import dev.morphia.annotations.Transient;
import eu.europeana.entitymanagement.utils.VocabularyWatcher;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@dev.morphia.annotations.Entity("Vocabulary")
@EntityListeners(VocabularyWatcher.class)
/**
 * class used for storing static vocabularies (e.g. europeana roles)
 */
public class Vocabulary {

  @Transient
  private String type = "Concept";
  
  @Id @JsonIgnore private ObjectId dbId;

  @Indexed(options = @IndexOptions(unique = true))
  private String id;
  
  protected List<String> inScheme;
  
  protected Map<String, String> prefLabel;

  @JsonIgnore 
  private Date created;

  @JsonIgnore 
  private Date modified;
  
  /**
   * default constructor
   */
  public Vocabulary() {
  }

  public void setDbId(ObjectId dbId_param) {
    this.dbId = dbId_param;
  }

  public ObjectId getDbId() {
    return dbId;
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

  @Override
  public String toString() {
    return String.format("Vocabulary.id: %s", getId());
  }

  @JsonGetter
  public String getId() {
    return id;
  }

  @JsonSetter
  public void setId(String id) {
    this.id = id;
  }

  @JsonGetter(IN_SCHEME)
  public List<String> getInScheme() {
    return inScheme;
  }

  @JsonSetter(IN_SCHEME)
  public void setInScheme(List<String> inScheme) {
    this.inScheme = inScheme;
  }  

  @JsonGetter(PREF_LABEL)
  public Map<String, String> getPrefLabel() {
    return prefLabel;
  }

  @JsonSetter(PREF_LABEL)
  public void setPrefLabel(Map<String, String> prefLabel) {
    this.prefLabel = prefLabel;
  }

  public String getType() {
    return type;
  }
}