package eu.europeana.entitymanagement.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.europeana.entitymanagement.vocabulary.UsageStatsFields;

import java.util.Date;
import java.util.List;

@JsonInclude(value = JsonInclude.Include.NON_DEFAULT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Metric {

    @JsonProperty(UsageStatsFields.TYPE)
    private String type;

    @JsonProperty(UsageStatsFields.CREATED)
    private Date timestamp;

    @JsonProperty(UsageStatsFields.ENTITIES_PER_LANG_TYPE)
    private List<EntitiesPerLanguage> entities;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public List<EntitiesPerLanguage> getEntities() {
        return entities;
    }

    public void setEntities(List<EntitiesPerLanguage> entities) {
        this.entities = entities;
    }
}
