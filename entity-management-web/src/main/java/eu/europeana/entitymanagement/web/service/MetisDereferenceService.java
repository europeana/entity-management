package eu.europeana.entitymanagement.web.service;

import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.exception.HttpBadRequestException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.METIS_DEREF_PATH;
import static eu.europeana.entitymanagement.web.MetisDereferenceUtils.parseMetisResponse;

/**
 * Handles de-referencing entities from Metis.
 */
@Service(AppConfig.BEAN_METIS_DEREF_SERVICE)
public class MetisDereferenceService implements InitializingBean {
    private static final Logger logger = LogManager.getLogger(MetisDereferenceService.class);

	private final WebClient metisWebClient;
	private final JAXBContext jaxbContext;

	private Unmarshaller unmarshaller;

	@Autowired
	public MetisDereferenceService(EntityManagementConfiguration configuration,
			Builder webClientBuilder, JAXBContext jaxbContext) {
		this.jaxbContext = jaxbContext;
		this.metisWebClient = webClientBuilder.baseUrl(configuration.getMetisBaseUrl()).build();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		unmarshaller = jaxbContext.createUnmarshaller();
	}
	/**
     * Dereferences the entity with the given id value.
     *
     * @param id external ID for entity
     * @return An optional containing the de-referenced entity, or an empty optional
     *         if no match found.
     * @throws EuropeanaApiException on error
     */
    public Entity dereferenceEntityById(String id) throws EuropeanaApiException {
	String metisResponseBody = fetchMetisResponse(id);
	return parseMetisResponse(unmarshaller, id, metisResponseBody);
    }


    String fetchMetisResponse(String entityId) {
	logger.debug("De-referencing entityId={} from Metis", entityId);

	String metisResponseBody = metisWebClient.get()
		.uri(uriBuilder -> uriBuilder.path(METIS_DEREF_PATH).queryParam("uri", entityId).build())
		.accept(MediaType.APPLICATION_XML).retrieve()
		// return 400 for 4xx responses from Metis
		.onStatus(HttpStatus::is4xxClientError,
			response -> response.bodyToMono(String.class).map(HttpBadRequestException::new))
		// return 500 for everything else
		.onStatus(HttpStatus::isError,
			response -> response.bodyToMono(String.class).map(EuropeanaApiException::new))
		.bodyToMono(String.class).block();

	logger.debug("Metis dereference response for entityId={} - {} ", entityId, metisResponseBody);
	return metisResponseBody;
    }
}
