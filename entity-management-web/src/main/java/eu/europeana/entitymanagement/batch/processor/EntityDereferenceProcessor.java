package eu.europeana.entitymanagement.batch.processor;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.utils.EntityComparator;
import eu.europeana.entitymanagement.web.service.MetisDereferenceService;
import java.util.Date;
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
    private final EntityComparator entityComparator;

    @Autowired
    public EntityDereferenceProcessor(MetisDereferenceService dereferenceService) {
        this.dereferenceService = dereferenceService;
        this.entityComparator = new EntityComparator();
    }

    @Override
    public EntityRecord process(@NonNull EntityRecord entityRecord) throws Exception {
        logger.debug("Calling Metis dereference service for entityId={}", entityRecord.getEntityId());
        Entity entity = entityRecord.getEntity();
        Entity metisResponse = dereferenceService.dereferenceEntityById(entityRecord.getExternalProxy().getProxyId());

        // Entity is newly created if its isAggregatedBy creation and last modified time are the same
        boolean isEntityNew = entity.getIsAggregatedBy().getCreated().equals(entity.getIsAggregatedBy().getModified());

        /*
         *  Entity needs to be processed at least once.
         *  This updates the last modified time. See EntityRecordService.mergeEntities()
         */
        if (!isEntityNew && entityComparator.compare(entityRecord.getExternalProxy().getEntity(), metisResponse) == 0) {
            logger.debug("External proxy for entityId={} matches Metis response and entity has already been updated since creation. Stopping processing", entityRecord.getEntityId());
            // stop processing
            return null;
        }

        logger.debug("Storing de-referenced metadata in external proxy for entityId={}", entityRecord.getEntityId());
        // replace external proxy with MetisResponse
        entityRecord.getExternalProxy().setEntity(metisResponse);
        entityRecord.getExternalProxy().getProxyIn().setModified(new Date());

        return entityRecord;
    }
}
