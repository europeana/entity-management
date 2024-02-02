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
import eu.europeana.entitymanagement.utils.VocabularyWatcher;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@dev.morphia.annotations.Entity("Vocabulary")
@EntityListeners(VocabularyWatcher.class)
public class Vocabulary {

  private String type = "Vocabulary";
  
  @Id @JsonIgnore private ObjectId dbId;

  @Indexed(options = @IndexOptions(unique = true))
  private String vocabularyUri;
  
  protected List<String> inScheme;
  
  protected Map<String, String> prefLabel;

  @JsonIgnore 
  private Date created;

  @JsonIgnore 
  private Date modified;
  
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
    return String.format("Vocabulary.vocabularyUri: %s", getVocabularyUri());
  }

  @JsonGetter
  public String getVocabularyUri() {
    return vocabularyUri;
  }

  @JsonSetter
  public void setVocabularyUri(String vocabularyUri) {
    this.vocabularyUri = vocabularyUri;
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