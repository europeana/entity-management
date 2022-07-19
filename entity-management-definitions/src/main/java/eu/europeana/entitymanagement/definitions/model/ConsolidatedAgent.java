package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.BEGIN;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.DATE_OF_BIRTH;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.DATE_OF_DEATH;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.DATE_OF_ESTABLISHMENT;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.DATE_OF_TERMINATION;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.END;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.GENDER;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.PLACE_OF_BIRTH;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.PLACE_OF_DEATH;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.collections.CollectionUtils;

/**
 * Necessary for serializing agent instances contained within the consolidated entity.
 *
 * <p>Also see {@link eu.europeana.entitymanagement.definitions.mixins.ConsolidatedAgentMixin}
 */
public class ConsolidatedAgent extends Agent {

  public ConsolidatedAgent() {
    // default constructor
  }

  public ConsolidatedAgent(Agent copy) {
    super(copy);
  }

  @JsonProperty(BEGIN)
  private String beginString() {
    return (String) CollectionUtils.get(getBegin(), 0);
  }

  @JsonProperty(END)
  private String endString() {
    return (String) CollectionUtils.get(getEnd(), 0);
  }

  @JsonProperty(DATE_OF_BIRTH)
  private String dateOfBirthString() {
    return (String) CollectionUtils.get(getDateOfBirth(), 0);
  }

  @JsonProperty(DATE_OF_ESTABLISHMENT)
  private String dateOfEstablishmentString() {
    return (String) CollectionUtils.get(getDateOfEstablishment(), 0);
  }

  @JsonProperty(PLACE_OF_DEATH)
  private String placeOfDeathString() {
    return (String) CollectionUtils.get(getPlaceOfDeath(), 0);
  }

  @JsonProperty(DATE_OF_DEATH)
  private String dateOfDeathString() {
    return (String) CollectionUtils.get(getDateOfDeath(), 0);
  }

  @JsonProperty(PLACE_OF_BIRTH)
  private String placeOfBirthString() {
    return (String) CollectionUtils.get(getPlaceOfBirth(), 0);
  }

  @JsonProperty(GENDER)
  private String genderString() {
    return (String) CollectionUtils.get(getGender(), 0);
  }

  @JsonProperty(DATE_OF_TERMINATION)
  private String dateOfTerminationString() {
    return (String) CollectionUtils.get(getDateOfTermination(), 0);
  }
}
