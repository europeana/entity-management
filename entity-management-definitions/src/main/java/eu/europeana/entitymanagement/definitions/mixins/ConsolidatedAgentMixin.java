package eu.europeana.entitymanagement.definitions.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

/**
 * In the consolidated entity, certain fields are serialized as strings (instead of lists). Since
 * these fields have the same property names within its superclass, they have to be ignored.
 */
public abstract class ConsolidatedAgentMixin {

  @JsonIgnore
  public abstract List<String> getBegin();

  @JsonIgnore
  public abstract List<String> getEnd();

  @JsonIgnore
  public abstract List<String> getDateOfBirth();

  @JsonIgnore
  public abstract List<String> getDateOfDeath();

  @JsonIgnore
  public abstract List<String> getPlaceOfBirth();

  @JsonIgnore
  public abstract List<String> getPlaceOfDeath();

  @JsonIgnore
  public abstract List<String> getDateOfEstablishment();

  @JsonIgnore
  public abstract List<String> getDateOfTermination();

  @JsonIgnore
  public abstract List<String> getGender();
}
