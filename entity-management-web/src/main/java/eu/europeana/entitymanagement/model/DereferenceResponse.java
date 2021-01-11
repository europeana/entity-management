package eu.europeana.entitymanagement.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Stub class to contain de-reference response from Metis
 * This only contains fields that are required for processing, and not the entire response object.
 */
@JsonDeserialize(using = DereferenceResponseSerializer.class)
public class DereferenceResponse {

    private final String exactMatch;

    public DereferenceResponse(String exactMatch) {
        this.exactMatch = exactMatch;
    }

    public String getExactMatch() {
        return exactMatch;
    }

    @Override
    public String toString() {
        return "DereferenceResponse{" +
                "exactMatch='" + exactMatch + '\'' +
                '}';
    }
}
