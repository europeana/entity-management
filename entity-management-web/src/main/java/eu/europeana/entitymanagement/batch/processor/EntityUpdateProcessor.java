package eu.europeana.entitymanagement.batch.processor;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.exception.ingestion.EntityValidationException;
import eu.europeana.entitymanagement.normalization.EntityFieldsCleaner;
import eu.europeana.entitymanagement.normalization.EntityFieldsCompleteValidatorGroup;
import eu.europeana.entitymanagement.normalization.EntityFieldsMinimalValidatorGroup;
import eu.europeana.entitymanagement.utils.EntityObjectFactory;
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

        EntityProxy europeanaProxy = entityRecord.getEuropeanaProxy();
        EntityProxy externalProxy = entityRecord.getExternalProxy();
        if (europeanaProxy == null || externalProxy == null) {
            throw new EuropeanaApiException(String.format(
                    "Unable to process entity record with id: %s. Europeana proxy or external proxy is not available in the record!",
                    entityRecord.getEntityId()));
        }

        Entity europeanaEntity = europeanaProxy.getEntity();
        Entity externalEntity = externalProxy.getEntity();

        validateMinimalConstraints(externalEntity);
        //data processing must be performed on a deep clone
        Entity normalizedEntity = EntityObjectFactory.createNewEntity(externalEntity);
        emEntityFieldCleaner.cleanAndNormalize(normalizedEntity);
        Entity consolidatedEntity = entityRecordService.mergeEntities(europeanaEntity, externalEntity);
        entityRecordService.performReferentialIntegrity(consolidatedEntity);
        validateCompleteConstraints(consolidatedEntity);
        
        entityRecordService.updateConsolidatedVersion(entityRecord, consolidatedEntity);

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
