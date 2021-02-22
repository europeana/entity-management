package eu.europeana.entitymanagement.web.model.ingestion;

import eu.europeana.entitymanagement.definitions.model.EntityRecord;

public interface UpdateTask {

    String[] getDataSources();

    String getEntityId();

    void setRecord(EntityRecord record);

    EntityRecord getRecord();

    void setStatus(String status);

    String getStatus();

    
}