package eu.europeana.entitymanagement.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import eu.europeana.entitymanagement.vocabulary.UsageStatsFields;

@JsonPropertyOrder({UsageStatsFields.ENTITY_LANG, UsageStatsFields.AGENT, UsageStatsFields.CONCEPT, UsageStatsFields.ORGANISATION, UsageStatsFields.PLACE,
        UsageStatsFields.TIMESPAN})
public class EntitiesPerLanguage extends Entity {

    @JsonProperty(UsageStatsFields.ENTITY_LANG)
    private String lang;

    public EntitiesPerLanguage() {
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}
