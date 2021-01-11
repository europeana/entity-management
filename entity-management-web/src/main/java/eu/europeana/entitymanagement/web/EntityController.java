package eu.europeana.entitymanagement.web;

import eu.europeana.entity.edm.internal.Entity;
import eu.europeana.entity.utils.Constants;
import eu.europeana.entitymanagement.model.EntityRequest;
import eu.europeana.entitymanagement.service.EntityService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

/**
 * Handles requests for creating, updating and deleting entities.
 */
@RestController
public class EntityController {

    @Autowired
    private EntityService entityService;


    @PostMapping(value = "/entity", produces = Constants.MEDIA_TYPE_JSONLD)
    public ResponseEntity<Entity> createEntity(@RequestBody EntityRequest entity) {
        // check if id is already being used, if so return a 301
        Optional<Entity> existingEntity = entityService.checkEntityExists(entity.getId());
        if (existingEntity.isPresent()) {
            // return 301 redirect
            return ResponseEntity
                    .status(HttpStatus.MOVED_PERMANENTLY)
                    .location(UriComponentsBuilder.newInstance()
                            .path("/entity/{type}/{identitifier}.{format}")
                            .buildAndExpand(existingEntity.get().getType().toLowerCase(),
                                    FilenameUtils.getBaseName(existingEntity.get().getId()),
                                    Constants.JSONLD_FORMAT).toUri())
                    .build();

        }


        // return 400 error if ID does not match a configured datasource
        if (!entityService.checkSourceExists(entity.getId())) {
            return ResponseEntity.badRequest().build();
        }

        existingEntity = entityService.checkCoreferencedEntity(entity.getId());
        // dereference using Metis. return HTTP 400 for HTTP4XX responses and HTTP 504 for other error responses
        if (existingEntity.isPresent()) {
            // return 301 redirect
            return ResponseEntity
                    .status(HttpStatus.MOVED_PERMANENTLY)
                    .location(UriComponentsBuilder.newInstance()
                            .path("/entity/{type}/{identitifier}.{format}")
                            .buildAndExpand(existingEntity.get().getType().toLowerCase(),
                                    FilenameUtils.getBaseName(existingEntity.get().getId()),
                                    Constants.JSONLD_FORMAT).toUri())
                    .build();

        }

        Entity newEntity = entityService.createNewEntity(entity);
        return ResponseEntity.accepted().body(newEntity);
    }
}
