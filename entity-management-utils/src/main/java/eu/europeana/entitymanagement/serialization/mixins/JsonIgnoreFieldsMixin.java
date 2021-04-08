package eu.europeana.entitymanagement.serialization.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Configures fields to be ignored when serializing JSON
 */
public abstract class JsonIgnoreFieldsMixin {
  @JsonIgnore
  public abstract String getAbout();
}