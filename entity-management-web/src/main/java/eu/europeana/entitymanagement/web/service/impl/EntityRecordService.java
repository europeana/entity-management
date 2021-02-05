package eu.europeana.entitymanagement.web.service.impl;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.impl.BaseEntity;
import eu.europeana.entitymanagement.definitions.model.impl.BaseEntityRecord;
import eu.europeana.entitymanagement.exception.EntityCreationException;
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;
import eu.europeana.entitymanagement.util.BaseEntityFactory;
import eu.europeana.entitymanagement.web.model.EntityCreationRequest;

@Service
public class EntityRecordService {

    Logger logger = LogManager.getLogger(getClass());

    @Autowired
    private EntityRecordRepository entityRecordRepository;

    public Optional<EntityRecord> retrieveEntityRecordByUri(String entityUri) {
        return Optional.ofNullable(entityRecordRepository.findByEntityId(entityUri));
    }

    public long deleteEntityRecordByEntityId(String entityId) {
        return entityRecordRepository.deleteDataset(entityId);
    }

    public EntityRecord saveEntityRecord(EntityRecord er) {
        return entityRecordRepository.save(er);
    }

    /**
     * Creates an {@link EntityRecord} from an {@link EntityCreationRequest}, which is then persisted.
     * @param entityCreationRequest entity request object
     * @param metisResponse de-referenced XML response instance from Metis
     * @return Saved Entity record
     * @throws EntityCreationException if an error occurs
     */
    public EntityRecord createEntityFromRequest(EntityCreationRequest entityCreationRequest, BaseEntity metisResponse) throws EntityCreationException {
        BaseEntity entity = BaseEntityFactory.createEntityFromXmlType(metisResponse.getClass());

        entity.setPrefLabelStringMap(entityCreationRequest.getPrefLabel());
        entity.setAltLabel(entityCreationRequest.getAltLabel());

        // TODO: add proxies and aggregations

        EntityRecord entityRecord = new BaseEntityRecord();
        entityRecord.setEntity(entity);

        return entityRecordRepository.save(entityRecord);

    }

    /**
     * Checks if any of the resources in the SameAs field from Metis is alredy known.
     * @param rdfResources list of SameAs resources
     * @return Optional containing EntityRecord, or empty Optional if none found
     */
    public Optional<EntityRecord> retrieveMetisCoreferenceSameAs(String[] rdfResources) {
        for (String resource : rdfResources) {
            Optional<EntityRecord> entityRecordOptional = retrieveEntityRecordByUri(resource);
            if(entityRecordOptional.isPresent()){
                return entityRecordOptional;
            }
        }

        return Optional.empty();
    }
}
