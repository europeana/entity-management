package eu.europeana.entitymanagement.service;

import eu.europeana.entitymanagement.model.DereferenceResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Handles de-referencing entities from Metis.
 */
@Service
public class MetisDereferenceService {


    /**
     * Dereferences the entity with the given id value.
     *
     * @param id external ID for entity
     * @return An optional containing the de-referenced entity, or an empty optional if no match found.
     */
    public Optional<DereferenceResponse> dereferenceEntityById(String id) {
        //TODO: implement functionality

        return Optional.empty();
    }
}
