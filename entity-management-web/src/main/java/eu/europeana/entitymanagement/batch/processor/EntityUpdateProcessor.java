package eu.europeana.entitymanagement.batch.processor;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.normalization.EntityFieldsCleaner;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;
import java.util.Set;

/**
 * This {@link ItemProcessor} updates Entity metadata.
 */
@Component
public class EntityUpdateProcessor implements ItemProcessor<EntityRecord, EntityRecord> {
    private final EntityRecordService entityRecordService;
    private final ValidatorFactory emValidatorFactory;

    private final EntityFieldsCleaner emEntityFieldCleaner;

    private static final Logger logger = LogManager.getLogger(EntityUpdateProcessor.class);

    public EntityUpdateProcessor(EntityRecordService entityRecordService,
        ValidatorFactory emValidatorFactory, 
        EntityFieldsCleaner emEntityFieldCleaner) {
        this.entityRecordService = entityRecordService;
        this.emValidatorFactory = emValidatorFactory;
        this.emEntityFieldCleaner = emEntityFieldCleaner;
    }

    @Override
    public EntityRecord process(@NonNull EntityRecord entityRecord) throws Exception {
        //TODO: Validate entity metadata from Proxy Data Source
        emEntityFieldCleaner.cleanAndNormalize(entityRecord.getExternalProxy().getEntity());
	      entityRecordService.mergeEntity(entityRecord);
        validateConstraints(entityRecord);
        entityRecordService.performReferentialIntegrity(entityRecord.getEntity());

        return entityRecord;
    }

    private void validateConstraints(EntityRecord entityRecord)  {
        Set<ConstraintViolation<Entity>> violations = emValidatorFactory.getValidator().validate(entityRecord.getEntity());
        if (!violations.isEmpty()) {
            //TODO: throw exception here when the implementation is stable and correct
            logger.debug("The record with the following id has constraint validation errors: " + entityRecord.getEntityId());
        }
    }
}
