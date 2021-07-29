package eu.europeana.entitymanagement.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import eu.europeana.entitymanagement.vocabulary.UsageStatsFields;

@JsonPropertyOrder({UsageStatsFields.ENTITY_LANG_COUNT, UsageStatsFields.ENTITY_LANG_TYPE})
public class EntityCountType {

    @JsonProperty(UsageStatsFields.ENTITY_LANG_COUNT)
    private String count;

    @JsonProperty(UsageStatsFields.ENTITY_LANG_TYPE)
    private String type;

    public EntityCountType(String count, String type) {
        this.count = count;
        this.type = type;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
