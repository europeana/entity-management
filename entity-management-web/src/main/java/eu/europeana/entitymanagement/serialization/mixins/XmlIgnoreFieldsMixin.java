package eu.europeana.entitymanagement.serialization.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Ignores the context field during serialization
 */
public abstract class XmlIgnoreFieldsMixin {
  @JsonIgnore
  public abstract String getContext();
}