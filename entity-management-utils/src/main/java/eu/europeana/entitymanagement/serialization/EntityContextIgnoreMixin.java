package eu.europeana.entitymanagement.serialization;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Ignores the context field during serialization
 */
abstract class EntityContextIgnoreMixin {
  @JsonIgnore
  abstract String getContext();
}