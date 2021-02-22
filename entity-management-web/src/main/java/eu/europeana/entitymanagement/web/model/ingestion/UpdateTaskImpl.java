package eu.europeana.entitymanagement.web.model.ingestion;

import eu.europeana.entitymanagement.definitions.model.EntityRecord;

public class UpdateTaskImpl implements UpdateTask{

    private final String entityId;
    private EntityRecord record;
    private final String[] dataSources;
    private String status;
    
    public UpdateTaskImpl (String entityId, String[] dataSources) {
	this.entityId = entityId;
	this.dataSources = dataSources;
    }

    @Override
    public EntityRecord getRecord() {
        return record;
    }

    @Override
    public void setRecord(EntityRecord record) {
        this.record = record;
    }

    @Override
    public String getEntityId() {
        return entityId;
    }

    @Override
    public String[] getDataSources() {
        return dataSources;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }
    
    
}