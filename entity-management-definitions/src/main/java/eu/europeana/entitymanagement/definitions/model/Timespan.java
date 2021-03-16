package eu.europeana.entitymanagement.definitions.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.europeana.entitymanagement.definitions.model.impl.TimespanImpl;

@JsonDeserialize(as = TimespanImpl.class)
public interface Timespan extends Entity {

    void setIsNextInSequence(String[] isNextInSequence);

    String[] getIsNextInSequence();

    public String getBeginString();

    public void setBeginString(String begin);

    public String getEndString();

    public void setEndString(String end);

    public String[] getIsPartOfArray();

    public void setIsPartOfArray(String[] isPartOf);
   
}
