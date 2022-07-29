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
import eu.europeana.entitymanagement.utils.EMCollectionUtils;

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
    return EMCollectionUtils.getFirstElement(getBegin());
  }

  @JsonProperty(END)
  private String endString() {
    return EMCollectionUtils.getFirstElement(getEnd());
  }

  @JsonProperty(DATE_OF_BIRTH)
  private String dateOfBirthString() {
    return EMCollectionUtils.getFirstElement(getDateOfBirth());
  }

  @JsonProperty(DATE_OF_ESTABLISHMENT)
  private String dateOfEstablishmentString() {
    return EMCollectionUtils.getFirstElement(getDateOfEstablishment());
  }

  @JsonProperty(PLACE_OF_DEATH)
  private String placeOfDeathString() {
    return EMCollectionUtils.getFirstElement(getPlaceOfDeath());
  }

  @JsonProperty(DATE_OF_DEATH)
  private String dateOfDeathString() {
    return EMCollectionUtils.getFirstElement(getDateOfDeath());
  }

  @JsonProperty(PLACE_OF_BIRTH)
  private String placeOfBirthString() {
    return EMCollectionUtils.getFirstElement(getPlaceOfBirth());
  }

  @JsonProperty(GENDER)
  private String genderString() {
    return EMCollectionUtils.getFirstElement(getGender());
  }

  @JsonProperty(DATE_OF_TERMINATION)
  private String dateOfTerminationString() {
    return EMCollectionUtils.getFirstElement(getDateOfTermination());
  }
}
