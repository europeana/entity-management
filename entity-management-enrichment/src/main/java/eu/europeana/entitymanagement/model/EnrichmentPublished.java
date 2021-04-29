package eu.europeana.entitymanagement.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class EnrichmentPublished {

    private long count;
    private List<String> entities;

    public EnrichmentPublished(long count, List<String> entities) {
        this.count = count;
        this.entities = entities;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public List<String> getEntities() {
        return entities;
    }

    public void setEntities(List<String> entities) {
        this.entities = entities;
    }
}
