package eu.europeana.entity.edm.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.europeana.entity.utils.Constants;

import java.util.*;


public class Entity {

    private final String id;
    private final String type;

    @JsonProperty("@context")
    private final String context = Constants.CONTEXT_ENTITY;

    private Map<String, List<String>> prefLabel;
    private Map<String, List<String>> altLabel;
    private String depiction;
    private List<String> sameAs;


    private Entity(String id, String type, Map<String, List<String>> prefLabel, Map<String, List<String>> altLabel, String depiction, List<String> sameAs) {
        this.id = id;
        this.type = type;
        this.prefLabel = prefLabel;
        this.altLabel = altLabel;
        this.depiction = depiction;
        this.sameAs = sameAs;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getContext() {
        return context;
    }

    public Map<String, List<String>> getPrefLabel() {
        return prefLabel;
    }

    public Map<String, List<String>> getAltLabel() {
        return altLabel;
    }

    public String getDepiction() {
        return depiction;
    }

    public List<String> getSameAs() {
        return sameAs;
    }


    public static class Builder {
        private final String id;
        private final String type;
        private final Map<String, List<String>> prefLabel = new HashMap<>();
        private final Map<String, List<String>> altLabel = new HashMap<>();
        private final List<String> sameAs = new ArrayList<>();
        private String depiction;

        public Builder(String id, String type) {
            this.id = id;
            this.type = type;
        }

        public Builder addPrefLabel(String language, List<String> values) {
            this.prefLabel.put(language, values);
            return this;
        }

        public Builder addAltLabel(String language, List<String> values) {
            this.altLabel.put(language, values);
            return this;
        }

        public Builder setDepiction(String depiction) {
            this.depiction = depiction;
            return this;
        }

        public Builder addSameAs(String value) {
            this.sameAs.add(value);
            return this;
        }

        public Entity build() {
            return new Entity(id, type, prefLabel, altLabel, depiction, sameAs);
        }
    }
}
