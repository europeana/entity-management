package eu.europeana.entitymanagement.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.europeana.entitymanagement.vocabulary.UsageStatsFields;

public class Entity {

    @JsonProperty(UsageStatsFields.TIMESPAN)
    private int timespans;

    @JsonProperty(UsageStatsFields.CONCEPT)
    private int concepts;

    @JsonProperty(UsageStatsFields.ORGANISATION)
    private int organisations;

    @JsonProperty(UsageStatsFields.AGENT)
    private int agents;

    @JsonProperty(UsageStatsFields.PLACE)
    private int places;

    @JsonProperty(UsageStatsFields.TOTAL)
    private int total;

    public int getTimespans() {
        return timespans;
    }

    public void setTimespans(int timespans) {
        this.timespans = timespans;
    }

    public int getConcepts() {
        return concepts;
    }

    public void setConcepts(int concepts) {
        this.concepts = concepts;
    }

    public int getOrganisations() {
        return organisations;
    }

    public void setOrganisations(int organisations) {
        this.organisations = organisations;
    }

    public int getAgents() {
        return agents;
    }

    public void setAgents(int agents) {
        this.agents = agents;
    }

    public int getPlaces() {
        return places;
    }

    public void setPlaces(int places) {
        this.places = places;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
