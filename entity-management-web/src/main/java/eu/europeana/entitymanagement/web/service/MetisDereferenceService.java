package eu.europeana.entitymanagement.web.service;

import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.exception.FunctionalRuntimeException;
import eu.europeana.entitymanagement.exception.HttpBadRequestException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import eu.europeana.entitymanagement.exception.MetisNotKnownException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

import java.time.Duration;
import java.time.Instant;

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

	/**
	 * Create a separate JAXB unmarshaller for each thread
	 */
	private ThreadLocal<Unmarshaller> unmarshaller;

	@Autowired
	public MetisDereferenceService(EntityManagementConfiguration configuration,
			Builder webClientBuilder, JAXBContext jaxbContext) {
		this.jaxbContext = jaxbContext;
		this.metisWebClient = webClientBuilder.baseUrl(configuration.getMetisBaseUrl()).build();
	}

	@Override
	public void afterPropertiesSet()  {
		unmarshaller = ThreadLocal.withInitial(() -> {
			try {
				return jaxbContext.createUnmarshaller();
			} catch (JAXBException e) {
				throw new FunctionalRuntimeException("Error creating JAXB unmarshaller ", e);
			}
		});
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
		Entity metisResponse = parseMetisResponse(unmarshaller.get(), id, metisResponseBody);
		if(metisResponse == null){
			throw new MetisNotKnownException("Unsuccessful Metis dereferenciation for externalId=" + id);
		}
		return metisResponse;
    }


    String fetchMetisResponse(String externalId) {
    	Instant start= Instant.now();
		logger.info("De-referencing externalId={} from Metis", externalId);

	String metisResponseBody = metisWebClient.get()
		.uri(uriBuilder -> uriBuilder.path(METIS_DEREF_PATH).queryParam("uri", externalId).build())
		.accept(MediaType.APPLICATION_XML).retrieve()
		// return 400 for 4xx responses from Metis
		.onStatus(HttpStatus::is4xxClientError,
			response -> response.bodyToMono(String.class).map(HttpBadRequestException::new))
		// return 500 for everything else
		.onStatus(HttpStatus::isError,
			response -> response.bodyToMono(String.class).map(EuropeanaApiException::new))
		.onStatus(HttpStatus::is5xxServerError,
				response -> response.bodyToMono(String.class).map(EuropeanaApiException::new))
		.bodyToMono(String.class).block();

	long duration = Duration.between(start, Instant.now()).toMillis();
	logger.info("Received Metis response for externalId={}. Duration={}ms", externalId, duration);
	if(logger.isDebugEnabled()){
		logger.debug("Metis response for externalId={}: {}", externalId, metisResponseBody);
	}
	return metisResponseBody;
    }
}
