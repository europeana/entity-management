package eu.europeana.entitymanagement.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.europeana.entitymanagement.vocabulary.UsageStatsFields;

import java.util.List;

public class EntityApiResponse {

    @JsonProperty(UsageStatsFields.ENTITY_LANG)
    private String lang;

    @JsonProperty(UsageStatsFields.ENTITY_LANG_VALUES)
    private List<EntityCountType> entityCountTypeList;

    public EntityApiResponse(String lang, List<EntityCountType> entityCountTypeList) {
        this.lang = lang;
        this.entityCountTypeList = entityCountTypeList;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public List<EntityCountType> getEntityCountTypeList() {
        return entityCountTypeList;
    }

    public void setEntityCountTypeList(List<EntityCountType> entityCountTypeList) {
        this.entityCountTypeList = entityCountTypeList;
    }
}
