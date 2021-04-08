package eu.europeana.entitymanagement.batch.processor;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.web.service.MetisDereferenceService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * This {@link ItemProcessor} retrieves Entity metadata from Metis, and then overwrites the local
 * metadata with Metis' if different.
 */
@Component
public class EntityDereferenceProcessor implements ItemProcessor<EntityRecord, EntityRecord> {

    private static final Logger logger = LogManager.getLogger(EntityDereferenceProcessor.class);
    private final MetisDereferenceService dereferenceService;

    @Autowired
    public EntityDereferenceProcessor(MetisDereferenceService dereferenceService) {
        this.dereferenceService = dereferenceService;
    }

    @Override
    public EntityRecord process(@NonNull EntityRecord entityRecord) throws Exception {
        logger.debug("Calling Metis dereference service for entityId={}", entityRecord.getEntityId());
        Entity metisResponse = dereferenceService.dereferenceEntityById(entityRecord.getExternalProxy().getProxyId());

        if (entityRecord.getEntity().equals(metisResponse)) {
            logger.debug("Existing metadata for entityId={} matches Metis response. Stopping processing", entityRecord.getEntityId());
            // stop processing
            return null;
        }

        logger.debug("Storing de-referenced metadata for entityId={}", entityRecord.getEntityId());

        // copy over entityID and isAggregatedBy, and then save the de-referenced version
        metisResponse.copyShellFrom(entityRecord.getEntity());
        entityRecord.setEntity(metisResponse);
        return entityRecord;
    }
}
