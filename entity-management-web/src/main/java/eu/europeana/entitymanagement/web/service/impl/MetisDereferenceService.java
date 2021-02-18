package eu.europeana.entitymanagement.web.service.impl;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.model.impl.BaseEntity;
import eu.europeana.entitymanagement.exception.HttpBadRequestException;
import eu.europeana.entitymanagement.web.model.metis.EnrichmentResultList;

/**
 * Handles de-referencing entities from Metis.
 */
@Service(AppConfig.BEAN_METIS_DEREF_SERVICE)
public class MetisDereferenceService {
    private static final Logger logger = LogManager.getLogger(MetisDereferenceService.class);

    private WebClient metisWebClient;
    
    @Resource(name = AppConfig.BEAN_EM_CONFIGURATION)
    private EntityManagementConfiguration configuration;


//    @Autowired
//    public MetisDereferenceService(WebClient webClient) {
//        this.metisWebClient = webClient;
//    }

    /**
     * Dereferences the entity with the given id value.
     *
     * @param id external ID for entity
     * @return An optional containing the de-referenced entity, or an empty optional if no match found.
     */
    public BaseEntity dereferenceEntityById(String id) throws HttpBadRequestException {
        logger.info("De-referencing entity {} with Metis", id);

        EnrichmentResultList metisResponse = getMetisWebClient().get()
                .uri(uriBuilder ->
                        uriBuilder.path("/dereference")
                                .queryParam("uri", id).build())
                .accept(MediaType.APPLICATION_XML)
                .retrieve()
                // return 400 for 4xx responses from Metis
                .onStatus(
                        HttpStatus::is4xxClientError,
                        response -> response.bodyToMono(String.class).map(HttpBadRequestException::new))
                // return 500 for everything else
                .onStatus(
                        HttpStatus::isError,
                        response -> response.bodyToMono(String.class).map(EuropeanaApiException::new))
                .bodyToMono(EnrichmentResultList.class)
                .block();



        if (metisResponse.getEnrichmentBaseResultWrapperList().isEmpty() || metisResponse.getEnrichmentBaseResultWrapperList().get(0).getEnrichmentBaseList().isEmpty()){
            // Metis returns an empty XML response if de-referencing is unsuccessful, instead of throwing an error
            logger.warn("Metis de-referencing unsuccessful for entity {}", id);
            throw new HttpBadRequestException("Metis de-referencing unsuccessful for entity " + id);
        }

        // see test/resources/metis-deref/response.xml for XML structure of metis response.
        BaseEntity xmlBaseEntity = metisResponse
                .getEnrichmentBaseResultWrapperList()
                .get(0)
                .getEnrichmentBaseList()
                .get(0);

        logger.info("Metis dereference response for entity {}: {} ", id, xmlBaseEntity);

        return xmlBaseEntity;
    }
    
//    @Bean
    public WebClient getMetisWebClient() {
	if(metisWebClient == null) {
	    metisWebClient = WebClient.builder().baseUrl(configuration.getMetisBaseUrl()).build();
	}
	return metisWebClient;
    }
}
