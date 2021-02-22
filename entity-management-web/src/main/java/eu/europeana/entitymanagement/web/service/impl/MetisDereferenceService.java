package eu.europeana.entitymanagement.web.service.impl;

import java.io.StringReader;

import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.exception.EntityCreationException;
import eu.europeana.entitymanagement.exception.HttpBadRequestException;
import eu.europeana.entitymanagement.web.xml.model.XmlBaseEntityImpl;
import eu.europeana.entitymanagement.web.xml.model.metis.EnrichmentResultList;

/**
 * Handles de-referencing entities from Metis.
 */
@Service(AppConfig.BEAN_METIS_DEREF_SERVICE)
public class MetisDereferenceService {
    private static final Logger logger = LogManager.getLogger(MetisDereferenceService.class);

    private WebClient metisWebClient;

    private Unmarshaller jaxbDeserializer;

    @Resource(name = AppConfig.BEAN_EM_CONFIGURATION)
    private EntityManagementConfiguration configuration;


    /**
     * Dereferences the entity with the given id value.
     *
     * @param id external ID for entity
     * @return An optional containing the de-referenced entity, or an empty optional
     *         if no match found.
     * @throws EntityCreationException
     */
    public Entity dereferenceEntityById(String id) throws HttpBadRequestException, EntityCreationException {
	logger.trace("De-referencing entity {} with Metis", id);

	String metisResponseBody = getMetisWebClient().get()
		.uri(uriBuilder -> uriBuilder.path("/dereference").queryParam("uri", id).build())
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
	try {
	    derefResult = (EnrichmentResultList) getDeserializer().unmarshal(new StringReader(metisResponseBody));
	} catch (JAXBException | RuntimeException e) {
	    throw new HttpBadRequestException(
		    "Unexpected exception occured when parsing metis dereference response for entity:  " + id, e);
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

//    @Bean
    public WebClient getMetisWebClient() {
	if (metisWebClient == null) {
	    metisWebClient = WebClient.builder().baseUrl(configuration.getMetisBaseUrl()).build();
	}
	return metisWebClient;
    }

    protected Unmarshaller getDeserializer() throws JAXBException {
	if (jaxbDeserializer == null) {
	    JAXBContext jaxbContext = JAXBContext.newInstance(EnrichmentResultList.class);
	    jaxbDeserializer = jaxbContext.createUnmarshaller();
	}
	return jaxbDeserializer;
    }

}
