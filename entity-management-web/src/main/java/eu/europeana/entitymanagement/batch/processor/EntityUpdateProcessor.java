package eu.europeana.entitymanagement.batch.processor;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.exception.ingestion.EntityValidationException;
import eu.europeana.entitymanagement.normalization.EntityFieldsCleaner;
import eu.europeana.entitymanagement.normalization.EntityFieldsCompleteValidatorGroup;
import eu.europeana.entitymanagement.normalization.EntityFieldsMinimalValidatorGroup;
import eu.europeana.entitymanagement.web.service.EntityRecordService;

/**
 * This {@link ItemProcessor} updates Entity metadata.
 */
@Component
public class EntityUpdateProcessor implements ItemProcessor<EntityRecord, EntityRecord> {
    private final EntityRecordService entityRecordService;
    private final ValidatorFactory emValidatorFactory;

    private final EntityFieldsCleaner emEntityFieldCleaner;

//    private static final Logger logger = LogManager.getLogger(EntityUpdateProcessor.class);

    public EntityUpdateProcessor(EntityRecordService entityRecordService,
        ValidatorFactory emValidatorFactory, 
        EntityFieldsCleaner emEntityFieldCleaner) {
        this.entityRecordService = entityRecordService;
        this.emValidatorFactory = emValidatorFactory;
        this.emEntityFieldCleaner = emEntityFieldCleaner;
    }

    @Override
    public EntityRecord process(@NonNull EntityRecord entityRecord) throws EuropeanaApiException {
        
        entityRecordService.updateConsolidatedVersion(entityRecord, consolidatedEntity);

    	validateMinimalConstraints(entityRecord.getExternalProxy().getEntity());
        emEntityFieldCleaner.cleanAndNormalize(entityRecord.getExternalProxy().getEntity());
        emEntityFieldCleaner.cleanAndNormalize(entityRecord.getEuropeanaProxy().getEntity());
        entityRecordService.mergeEntity(entityRecord);
        entityRecordService.performReferentialIntegrity(entityRecord.getEntity());
        validateCompleteConstraints(entityRecord.getEntity());
   
        return entityRecord;
    }

    private void validateCompleteConstraints(Entity entity) throws EntityValidationException  {
        Set<ConstraintViolation<Entity>> violations = emValidatorFactory.getValidator().validate(entity, EntityFieldsCompleteValidatorGroup.class);
        if (!violations.isEmpty()) {
            throw new EntityValidationException("The consolidated entity contains invalid data!", violations);
        }
    }
    
    private void validateMinimalConstraints(Entity entity) throws EntityValidationException  {
        Set<ConstraintViolation<Entity>> violations = emValidatorFactory.getValidator().validate(entity, EntityFieldsMinimalValidatorGroup.class);
        if (!violations.isEmpty()) {
            throw new EntityValidationException("The entity from the external source contains invalid data!", violations);
        }
    }
    
    
}
