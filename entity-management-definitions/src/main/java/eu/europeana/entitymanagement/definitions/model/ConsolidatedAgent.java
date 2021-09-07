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
 * This class is used when serializing agent instances contained within the consolidated entity.
 */
public class ConsolidatedAgent extends Agent {

  @JsonProperty(BEGIN)
  private String beginString() {
    return CollectionUtils.lastElement(getBegin());
  }

  @JsonProperty(END)
  private String endString() {
    return CollectionUtils.lastElement(getEnd());
  }

  @JsonProperty(DATE_OF_BIRTH)
  private String dateOfBirthString() {
    return CollectionUtils.lastElement(getDateOfBirth());
  }

  @JsonProperty(DATE_OF_ESTABLISHMENT)
  private String dateOfEstablishmentString() {
    return CollectionUtils.lastElement(getDateOfEstablishment());
  }

  @JsonProperty(PLACE_OF_DEATH)
  private String placeOfDeathString() {
    return CollectionUtils.lastElement(getPlaceOfDeath());
  }

  @JsonProperty(DATE_OF_DEATH)
  private String dateOfDeathString() {
    return CollectionUtils.lastElement(getDateOfDeath());
  }

  @JsonProperty(PLACE_OF_BIRTH)
  private String placeOfBirthString() {
    return CollectionUtils.lastElement(getPlaceOfBirth());
  }

  @JsonProperty(GENDER)
  private String genderString() {
    return CollectionUtils.lastElement(getGender());
  }

  @JsonProperty(DATE_OF_TERMINATION)
  private String dateOfTerminationString() {
    return CollectionUtils.lastElement(getDateOfTermination());
  }
}
