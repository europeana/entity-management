package eu.europeana.entitymanagement.batch.processor;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.exception.EntityMismatchException;
import eu.europeana.entitymanagement.exception.MetisNotKnownException;
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

    private static final String MISMATCH_EXCEPTION_STRING = "Metis type %s does not match entity type %s for entityId=%s";
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
        String entityId = entityRecord.getEntityId();
        String proxyId = entityRecord.getExternalProxy().getProxyId();
        Entity metisResponse;
        try {
            metisResponse = dereferenceService.dereferenceEntityById(proxyId);
        } catch (MetisNotKnownException e) {
            // include entityId in exception message, then rethrow it
            throw new MetisNotKnownException("Unsuccessful Metis dereferenciation for externalId=" +
                    proxyId + "; entityId=" + entityId);
        }
        Entity entity = entityRecord.getEntity();

        String metisType = metisResponse.getType();
        String entityType = entity.getType();
        if (!metisType.equals(entityType)) {

            throw new EntityMismatchException(
                    String.format(MISMATCH_EXCEPTION_STRING,
                            metisType, entityType, entityId));
        }

        /*
         * Entity is newly created if its isAggregatedBy creation and last modified time are the same
         * It needs to be processed at least once, which would update the last modified time.
         * See EntityRecordService.mergeEntities()
         */
        boolean isEntityNew = entity.getIsAggregatedBy().getCreated().equals(entity.getIsAggregatedBy().getModified());

        if(isEntityNew || !metisResponseMatchesExternalProxy(entityRecord, metisResponse)){
            if(logger.isTraceEnabled()) {
                logger.trace("Storing de-referenced metadata in external proxy for entityId={}", entityId);
            }
            // replace external proxy with MetisResponse
            entityRecord.getExternalProxy().setEntity(metisResponse);
            entityRecord.getExternalProxy().getProxyIn().setModified(new Date());
            return entityRecord;
        }

        // Update could also be triggered after changes to Europeana proxy.
        if(isEuropeanaProxyUpdated(entityRecord)){
          return entityRecord;
        }

        // Otherwise stop processing
        return null;
    }


  /**
   * Checks if the Europeana proxy has been updated since the last time entity consolidation occurred.
   * This is the case when the update task is triggered after Europeana metadata is updated.
   *
   * See {@link eu.europeana.entitymanagement.web.service.EntityRecordService#replaceEuropeanaProxy(Entity, EntityRecord)}
   */
  private boolean isEuropeanaProxyUpdated(EntityRecord entityRecord){
      return
          entityRecord.getEuropeanaProxy().getProxyIn().getModified()
              .after(entityRecord.getEntity().getIsAggregatedBy().getModified());
    }


  /**
   * Checks if Metis response matches metadata in external proxy
   */
  private boolean metisResponseMatchesExternalProxy(EntityRecord entityRecord, Entity metisResponse){
      return entityComparator.compare(entityRecord.getExternalProxy().getEntity(), metisResponse) == 0;
    }
}
