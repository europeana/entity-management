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
import org.springframework.util.CollectionUtils;

/**
 * Necessary for serializing agent instances contained within the consolidated entity.
 *
 * Also see {@link eu.europeana.entitymanagement.definitions.mixins.ConsolidatedAgentMixin}
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
    return CollectionUtils.firstElement(getBegin());
  }

  @JsonProperty(END)
  private String endString() {
    return CollectionUtils.firstElement(getEnd());
  }

  @JsonProperty(DATE_OF_BIRTH)
  private String dateOfBirthString() {
    return CollectionUtils.firstElement(getDateOfBirth());
  }

  @JsonProperty(DATE_OF_ESTABLISHMENT)
  private String dateOfEstablishmentString() {
    return CollectionUtils.firstElement(getDateOfEstablishment());
  }

  @JsonProperty(PLACE_OF_DEATH)
  private String placeOfDeathString() {
    return CollectionUtils.firstElement(getPlaceOfDeath());
  }

  @JsonProperty(DATE_OF_DEATH)
  private String dateOfDeathString() {
    return CollectionUtils.firstElement(getDateOfDeath());
  }

  @JsonProperty(PLACE_OF_BIRTH)
  private String placeOfBirthString() {
    return CollectionUtils.firstElement(getPlaceOfBirth());
  }

  @JsonProperty(GENDER)
  private String genderString() {
    return CollectionUtils.firstElement(getGender());
  }

  @JsonProperty(DATE_OF_TERMINATION)
  private String dateOfTerminationString() {
    return CollectionUtils.firstElement(getDateOfTermination());
  }
}
