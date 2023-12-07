package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.CONTEXT;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ID;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.PREF_LABEL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import dev.morphia.annotations.Embedded;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;

@Embedded
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
  CONTEXT,
  ID,
  PREF_LABEL
})
public class Country {

  public Country() {
    super();
  }

  public Country(Country copy) {
    super();
    this.id = copy.getId();
    if(copy.getPrefLabel()!=null) {
      this.prefLabel=new HashMap<>(copy.getPrefLabel());
    }
  }

  private String id;
  private Map<String, String> prefLabel;

  @JsonSetter(ID)
  public void setId(String id) {
    this.id = id;
  }

  @JsonGetter(ID)
  public String getId() {
    return id;
  }

  @JsonGetter(WebEntityFields.PREF_LABEL)
  public Map<String, String> getPrefLabel() {
    return prefLabel;
  }

  @JsonSetter(WebEntityFields.PREF_LABEL)
  public void setPrefLabel(Map<String, String> prefLabel) {
    this.prefLabel = prefLabel;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Country that = (Country) o;
    
    if (!Objects.equals(id, that.getId())) return false;
    return Objects.equals(prefLabel, that.getPrefLabel());
  }

  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((prefLabel == null) ? 0 : prefLabel.hashCode());
    return result;
  }

}
