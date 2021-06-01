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
        //TODO: Validate entity metadata from Proxy Data Source
        emEntityFieldCleaner.cleanAndNormalize(entityRecord.getExternalProxy().getEntity());
	entityRecordService.mergeEntity(entityRecord);
        entityRecordService.performReferentialIntegrity(entityRecord.getEntity());
        validateConstraints(entityRecord.getEntity());
   
        return entityRecord;
    }

    private void validateConstraints(Entity entity) throws EntityValidationException  {
        Set<ConstraintViolation<Entity>> violations = emValidatorFactory.getValidator().validate(entity);
        if (!violations.isEmpty()) {
            throw new EntityValidationException("The consolidated entity contains invalid data!", violations);
        }
    }
}
