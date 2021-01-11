package eu.europeana.entitymanagement.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.exception.HttpBadRequestException;
import eu.europeana.entitymanagement.model.DereferenceResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

/**
 * Handles de-referencing entities from Metis.
 */
@Service
public class MetisDereferenceService {
    private static final Logger logger = LogManager.getLogger(MetisDereferenceService.class);

    private final WebClient webClient;

    private final ObjectMapper mapper;


    @Autowired
    public MetisDereferenceService(WebClient webClient, ObjectMapper mapper) {
        this.webClient = webClient;
        this.mapper = mapper;
    }

    /**
     * Dereferences the entity with the given id value.
     *
     * @param id external ID for entity
     * @return An optional containing the de-referenced entity, or an empty optional if no match found.
     */
    public Optional<DereferenceResponse> dereferenceEntityById(String id) {
        logger.info("De-referencing entity {} with Metis", id);

        DereferenceResponse metisResponse = webClient.get()
                .uri(uriBuilder ->
                        uriBuilder.path("/dereference")
                                .queryParam("uri", id).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                // return 400 for 4xx responses from Metis
                .onStatus(
                        HttpStatus::is4xxClientError,
                        response -> response.bodyToMono(String.class).map(HttpBadRequestException::new))
                // return 500 for everything else
                .onStatus(
                        HttpStatus::isError,
                        response -> response.bodyToMono(String.class).map(EuropeanaApiException::new))
                .bodyToMono(DereferenceResponse.class)
                .block();

        logger.info("Metis dereference response for entity {}: {} ", id, metisResponse);

        return Optional.ofNullable(metisResponse);
    }
}
