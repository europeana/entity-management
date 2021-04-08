package eu.europeana.entitymanagement.serialization;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Ignores the context field during serialization
 */
public abstract class EntityContextIgnoreMixin {
  @JsonIgnore
  public abstract String getContext();
}