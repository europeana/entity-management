package eu.europeana.entitymanagement.batch.processor;

import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.dereference.Dereferencer;
import eu.europeana.entitymanagement.web.service.DereferenceServiceLocator;
import java.util.Date;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.exception.EntityMismatchException;
import eu.europeana.entitymanagement.exception.DatasourceNotKnownException;
import eu.europeana.entitymanagement.utils.EntityComparator;

/**
 * This {@link ItemProcessor} retrieves Entity metadata from all proxy datasources, and then overwrites the local
 * metadata if datasource response is different.
 */
@Component
public class EntityDereferenceProcessor implements ItemProcessor<EntityRecord, EntityRecord> {

    private static final String MISMATCH_EXCEPTION_STRING = "DataSource type %s does not match entity type %s for entityId=%s, proxyId=%s";
    private static final Logger logger = LogManager.getLogger(EntityDereferenceProcessor.class);
    private final DereferenceServiceLocator dereferenceServiceLocator;
    private final EntityComparator entityComparator;

    @Autowired
    public EntityDereferenceProcessor(DereferenceServiceLocator dereferenceServiceLocator) {
        this.dereferenceServiceLocator = dereferenceServiceLocator;
        this.entityComparator = new EntityComparator();
    }

    @Override
    public EntityRecord process(@NonNull EntityRecord entityRecord) throws Exception {
        String entityId = entityRecord.getEntityId();
      for (EntityProxy externalProxy : entityRecord.getExternalProxies()) {
        String proxyId = externalProxy.getProxyId();

        Dereferencer dereferencer = dereferenceServiceLocator.getDereferencer(proxyId,
            entityRecord.getEntity().getType());
        Optional<Entity> proxyResponseOptional = dereferencer.dereferenceEntityById(proxyId);
        if (proxyResponseOptional.isEmpty()) {
          throw new DatasourceNotKnownException("Unsuccessful dereferenciation for externalId=" +
              proxyId + "; entityId=" + entityId);
        }

        Entity proxyResponse = proxyResponseOptional.get();

        Entity entity = entityRecord.getEntity();

        String proxyResponseType = proxyResponse.getType();
        String entityType = entity.getType();
        if (!proxyResponseType.equals(entityType)) {
          throw new EntityMismatchException(
              String.format(MISMATCH_EXCEPTION_STRING,
                  proxyResponseType, entityType, entityId, proxyId));
        }

        /*
         * Entity is newly created if its isAggregatedBy creation and last modified time are the same
         * It needs to be processed at least once, which would update the last modified time.
         * See EntityRecordService.mergeEntities()
         */
        boolean isEntityNew = entity.getIsAggregatedBy().getCreated()
            .equals(entity.getIsAggregatedBy().getModified());

        if (isEntityNew || !datasourceResponseMatchesExternalProxy(externalProxy, proxyResponse)) {
          if (logger.isTraceEnabled()) {
            logger.trace("Storing de-referenced metadata in external proxy for entityId={}",
                entityId);
          }
          // replace external proxy with proxy response
          externalProxy.setEntity(proxyResponse);
          externalProxy.getProxyIn().setModified(new Date());
        }
      }

        return entityRecord;
    }





  /**
   * Checks if Datasource response matches metadata in external proxy
   */
  private boolean datasourceResponseMatchesExternalProxy(EntityProxy entityProxy, Entity metisResponse){
      return entityComparator.compare(entityProxy.getEntity(), metisResponse) == 0;
    }
}
