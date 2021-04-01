package eu.europeana.entitymanagement.web.service.impl;

import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.exception.EntityCreationException;
import eu.europeana.entitymanagement.exception.HttpBadRequestException;
import eu.europeana.entitymanagement.web.xml.model.XmlBaseEntityImpl;
import eu.europeana.entitymanagement.web.xml.model.metis.EnrichmentResultList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.METIS_DEREF_PATH;

/**
 * Handles de-referencing entities from Metis.
 */
@Service(AppConfig.BEAN_METIS_DEREF_SERVICE)
public class MetisDereferenceService {
    private static final Logger logger = LogManager.getLogger(MetisDereferenceService.class);

    private Unmarshaller jaxbDeserializer;
    private final Object deserializerLock = new Object();


	private final WebClient metisWebClient;

	@Autowired
	public MetisDereferenceService(EntityManagementConfiguration configuration, WebClient.Builder webClientBuilder) {
		this.metisWebClient = webClientBuilder.baseUrl(configuration.getMetisBaseUrl()).build();
	}

	/**
     * Dereferences the entity with the given id value.
     *
     * @param id external ID for entity
     * @return An optional containing the de-referenced entity, or an empty optional
     *         if no match found.
     * @throws EntityCreationException
     */
    public Entity dereferenceEntityById(String id) throws EuropeanaApiException {
	logger.trace("De-referencing entity {} with Metis", id);

	String metisResponseBody = metisWebClient.get()
		.uri(uriBuilder -> uriBuilder.path(METIS_DEREF_PATH).queryParam("uri", id).build())
		.accept(MediaType.APPLICATION_XML).retrieve()
		// return 400 for 4xx responses from Metis
		.onStatus(HttpStatus::is4xxClientError,
			response -> response.bodyToMono(String.class).map(HttpBadRequestException::new))
		// return 500 for everything else
		.onStatus(HttpStatus::isError,
			response -> response.bodyToMono(String.class).map(EuropeanaApiException::new))
		.bodyToMono(String.class).block();

	logger.debug("Metis dereference response for entity {}: {} ", id, metisResponseBody);
	
	EnrichmentResultList derefResult;
	// Prevent "FWK005 parse may not be called while parsing" error when this method is called by multiple threads
	synchronized (deserializerLock) {
		try {
			derefResult = (EnrichmentResultList) getDeserializer()
					.unmarshal(new StringReader(metisResponseBody));
		} catch (JAXBException | RuntimeException e) {
			throw new EuropeanaApiException(
					"Unexpected exception occurred when parsing metis dereference response for entity:  "
							+ id, e);
		}
	}

	if (derefResult== null || derefResult.getEnrichmentBaseResultWrapperList().isEmpty()
		|| derefResult.getEnrichmentBaseResultWrapperList().get(0).getEnrichmentBaseList().isEmpty()) {
	    // Metis returns an empty XML response if de-referencing is unsuccessful,
	    // instead of throwing an error
	    return null;
	}

		XmlBaseEntityImpl xmlBaseEntity = derefResult.getEnrichmentBaseResultWrapperList().get(0)
				.getEnrichmentBaseList().get(0);

		return xmlBaseEntity.toEntityModel();
    }


	private Unmarshaller getDeserializer() throws JAXBException {
		if (jaxbDeserializer == null) {
			JAXBContext jaxbContext = JAXBContext.newInstance(EnrichmentResultList.class);
			jaxbDeserializer = jaxbContext.createUnmarshaller();
		}
		return jaxbDeserializer;
	}

}
