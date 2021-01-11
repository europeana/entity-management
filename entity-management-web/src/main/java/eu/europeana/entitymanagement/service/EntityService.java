package eu.europeana.entitymanagement.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entity.edm.internal.Entity;
import eu.europeana.entitymanagement.config.DataSources;
import eu.europeana.entitymanagement.model.DereferenceResponse;
import eu.europeana.entitymanagement.model.EntityRequest;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static eu.europeana.entity.utils.Constants.ID_PREFIX_CONCEPT;
import static eu.europeana.entity.utils.Constants.TYPE_CONCEPT;

/**
 * This service co-ordinates the creation, update and deletion of entities.
 * It also triggers tasks for batch processing.
 */
@Service
public class EntityService {

    private final DataSources dataSources;
    private final MetisDereferenceService dereferenceService;

    public EntityService(DataSources dataSources, MetisDereferenceService dereferenceService) {
        this.dataSources = dataSources;
        this.dereferenceService = dereferenceService;
    }


    /**
     * Checks if a datasource is configured for the given entity ID
     *
     * @param id id to match
     * @return true if a Datasource match is configured, false otherwise.
     */
    public boolean checkSourceExists(String id) {
        return dataSources.getDatasources().stream().anyMatch(s -> id.contains(s.getUrl()));
    }

    /**
     * Retrieves an entity matching the specified id.
     *
     * @param id Entity id to dereference by
     * @return Optional containing entity, or empty Optional on no match
     */
    public Optional<Entity> checkEntityExists(String id) {
        //TODO: implement lookup here

        return Optional.empty();
    }

    /**
     * Creates a new Entity with the values provided in the {@link EntityRequest} argument.
     * <p>
     * * mints a new Europeana Entity identifier based on the entity type
     * * creates a shell record for the entity
     * * triggers an "Update Task" for the entity, if required
     *
     * @param entity
     * @return
     */
    public Entity createNewEntity(EntityRequest entity) {
        // TODO: mint new Europeana Entity ID

        // TODO: create shell record

        // TODO: replace with real entity
        return new Entity.Builder(ID_PREFIX_CONCEPT + "/" + FilenameUtils.getBaseName(entity.getId()), TYPE_CONCEPT)
                .addPrefLabel("en", Collections.singletonList("Bathtub"))
                .addAltLabel("en", Arrays.asList("bath", "tub"))
                .setDepiction("https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/1rm60rt0t_rtbcwh.jpg/330px-1rm60rt0t_rtbcwh.jpg")
                .addSameAs("http://www.wikidata.org/entity/Q152095")
                .build();


        // TODO: trigger update task
    }


    /**
     * This method de-references an entity with the given ID using Metis. It then checks if the co-reference already exists.
     *
     * @param id entity id
     * @return Optional containing co-reference, or empty optional if none exists.
     */
    public Optional<Entity> checkCoreferencedEntity(String id) {
        // dereference using Metis. return error if anything goes wrong
        Optional<DereferenceResponse> metisResponse = dereferenceService.dereferenceEntityById(id);
        if (metisResponse.isPresent()) {
            return checkEntityExists(metisResponse.get().getExactMatch());
        }
        return Optional.empty();
    }
}
