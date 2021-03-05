package eu.europeana.entitymanagement.web.service.ingestion;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;

import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.impl.AggregationImpl;
import eu.europeana.entitymanagement.exception.FunctionalRuntimeException;
import eu.europeana.entitymanagement.exception.ingestion.EntityUpdateException;
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;
import eu.europeana.entitymanagement.web.model.ingestion.UpdateTask;
import eu.europeana.entitymanagement.web.model.scoring.EntityMetrics;
import eu.europeana.entitymanagement.web.service.ScoringService;

public class UpdateTaskProcessorImpl implements UpdateTaskProcessor {

    /**
     * @deprecated task processor must not call the repository directly. The required methods must be accessed through the EntityRecordService
     */
    @Resource(name=AppConfig.BEAN_ENTITY_RECORD_REPO)
    @Deprecated
    private EntityRecordRepository entityRecordRepository;
    @Resource(name=AppConfig.BEAN_EM_SCORING_SERVICE)
    private ScoringService scoringService;

    public void executeTask(UpdateTask task) {
	try {
	    computeRakingMetrics(task);
	    fetchEntityRecord(task);
	    dereferenceEntity(task);
	    normalizeData(task);
	    updateReferences(task);
	    validateMetadata(task);
	    // TODO add if to check stop condition
	    checkProcessingState(task);
	    consolidateMetadata(task);

	    updateEntityRecord(task);
	    triggerIndexing(task);
	    finalizeTask(task);
	} catch (EntityUpdateException e) {
	    finalizeTask(task, e);
	}
    }

    private void finalizeTask(UpdateTask task, EntityUpdateException e) {
	// TODO Auto-generated method stub
	task.setStatus("Processing error: " + e.getMessage());
    }

    private void finalizeTask(UpdateTask task) {
	// TODO Auto-generated method stub
	task.setStatus("Processing complete");
    }

    @Override
    public void fetchEntityRecord(UpdateTask task) {
	EntityRecord record = entityRecordRepository.findByEntityId(task.getEntityId());
	task.setRecord(record);
	task.setStatus("Entity Record Fetched");
    }

    @Override
    public void dereferenceEntity(UpdateTask task) {
	// TODO Auto-generated method stub
	task.setStatus("Entity dereferenced");
    }

    @Override
    public void normalizeData(UpdateTask task) {
	// TODO Auto-generated method stub
	task.setStatus("External metadata normalized");
    }

    @Override
    public void updateReferences(UpdateTask task) {
	// TODO Auto-generated method stub
	task.setStatus("Updated referencial integrity");

    }

    @Override
    public void validateMetadata(UpdateTask task) {
	// TODO Auto-generated method stub
	task.setStatus("External metada Normalized");
    }

    @Override
    public void checkProcessingState(UpdateTask task) {
	// TODO Auto-generated method stub
	// TODO update this method to better fit the workflow
    }

    @Override
    public void consolidateMetadata(UpdateTask task) {
	// TODO Auto-generated method stub
	task.setStatus("Entity metadata updated");
    }

    @Override
    public void computeRakingMetrics(UpdateTask task) throws EntityUpdateException{
	EntityRecord record = task.getRecord();
	Entity entity = record.getEntity();
	if (entity == null) {
	    throw new EntityUpdateException(
		    "An entity object needs to be available in EntityRecord in order to compute the scoring metrics!");
	}
	EntityMetrics metrics;
	try {
	    metrics = scoringService.computeMetrics(entity);
	} catch (FunctionalRuntimeException | UnsupportedEntityTypeException e) {
	    throw new EntityUpdateException(
		    "Cannot compute ranking metrics for entity: " + entity.getEntityId(), e);
	}
	Aggregation aggregation = entity.getIsAggregatedBy();
	if (aggregation == null) {
	    aggregation = new AggregationImpl();
	    entity.setIsAggregatedBy(aggregation);
	}

	aggregation.setPageRank(metrics.getPageRank());
	aggregation.setRecordCount(metrics.getEnrichmentCount());
	aggregation.setScore(metrics.getScore());

	task.setStatus("Ranking metrics computed");
    }

    @Override
    public void updateEntityRecord(UpdateTask task) {
	// TODO Auto-generated method stub
	task.setStatus("Entity Record updated");
    }

    @Override
    public void triggerIndexing(UpdateTask task) {
	// TODO Auto-generated method stub
	task.setStatus("Triggered Entity indexig");
    }

}
