package eu.europeana.entitymanagement.web.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

import javax.validation.constraints.NotNull;

/**
 * Represents the body for Entity creation POST requests.
 */
public class EntityPreview {
	
    private String id;

    @JsonProperty("@context")
    private String context;

    @NotNull
    private EntityTypes type;


    private Map<String, String> prefLabel;
    private Map<String, List<String>> altLabel;
    private String depiction;

    public EntityPreview() {
        // create explicit empty constructor
    }

    public EntityPreview(String id, String context, Map<String, String> prefLabel, Map<String, List<String>> altLabel, String depiction) {
        this.id = id;
        this.context = context;
        this.prefLabel = prefLabel;
        this.altLabel = altLabel;
        this.depiction = depiction;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public Map<String, String> getPrefLabel() {
        return prefLabel;
    }

    public void setPrefLabel(Map<String, String> prefLabel) {
        this.prefLabel = prefLabel;
    }

    public Map<String, List<String>> getAltLabel() {
        return altLabel;
    }

    public void setAltLabel(Map<String, List<String>> altLabel) {
        this.altLabel = altLabel;
    }

    public String getDepiction() {
        return depiction;
    }

    public void setDepiction(String depiction) {
        this.depiction = depiction;
    }

    public String getType() {
        return type.getEntityType();
    }
}