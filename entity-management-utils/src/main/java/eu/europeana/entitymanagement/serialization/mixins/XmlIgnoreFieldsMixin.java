package eu.europeana.entitymanagement.serialization.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Ignores the context field during serialization
 */
public abstract class XmlIgnoreFieldsMixin {
  @JsonIgnore
  public abstract String getContext();
}