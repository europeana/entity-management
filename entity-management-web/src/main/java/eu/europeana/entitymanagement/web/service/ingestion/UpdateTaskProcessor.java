package eu.europeana.entitymanagement.web.service.ingestion;

import eu.europeana.entitymanagement.exception.ingestion.EntityUpdateException;
import eu.europeana.entitymanagement.web.model.ingestion.UpdateTask;

public interface UpdateTaskProcessor {

    /**
     * Obtain the entity record from MongoDB associated to the entity identifier;
     */
    void fetchEntityRecord(UpdateTask task) throws EntityUpdateException;
    
    /**
     * Call the Metis derenference service using the Proxy Data Source identifier;
     * @param task
     */
    void dereferenceEntity(UpdateTask task) throws EntityUpdateException;
    
    /**
     * Perform cleaning and normalization;
     * @param task
     */
    void normalizeData(UpdateTask task) throws EntityUpdateException;
    
    /**
     * Fix referential integrity;
     * @param task
     */
    void updateReferences(UpdateTask task) throws EntityUpdateException;
    
    /**
     * Validate entity’s metadata;
     * @param task
     */
    void validateMetadata(UpdateTask task) throws EntityUpdateException;
    
    /**
     * Compare the metadata associated with the Proxy Data Source against the new metadata, if it is the same finish processing;
     */
    void checkProcessingState(UpdateTask task) throws EntityUpdateException;
    
    /**
     * Update metadata associated with the Proxy Data Source;
     * Perform reconciliation by combining metadata from both the Proxy Data Source and Proxy Europeana, and update the Consolidated Proxy;
     * Update the modified field associated to the Consolidated Aggregation;
     */
    void consolidateMetadata(UpdateTask task) throws EntityUpdateException;
    
    /**
     * Calculate a) Wikidata PageRank; b) item count and c) derived score based on precalculated max score;
     * @param task
     */
    void computeRakingMetrics(UpdateTask task) throws EntityUpdateException;
    
    /**
     * Update Consolidated Aggregation with new derived score and item count; Update modified field associated to the Aggregation Data Source;
     * @param task
     */
    void updateEntityRecord(UpdateTask task) throws EntityUpdateException; 
    
    /**
     * Update indexing of the entity in Solr;
     * @param task
     */
    void triggerIndexing(UpdateTask task) throws EntityUpdateException;
   
}
