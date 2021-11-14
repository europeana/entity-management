package eu.europeana.entitymanagement.web.service;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.METIS_DEREF_PATH;
import static eu.europeana.entitymanagement.web.MetisDereferenceUtils.parseMetisResponse;

import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.dereference.Dereferencer;
import eu.europeana.entitymanagement.exception.DatasourceNotReachableException;
import eu.europeana.entitymanagement.exception.DatasourceUpstreamServerError;
import eu.europeana.entitymanagement.exception.FunctionalRuntimeException;
import eu.europeana.entitymanagement.exception.HttpBadRequestException;
import eu.europeana.entitymanagement.web.xml.model.XmlBaseEntityImpl;
import eu.europeana.entitymanagement.zoho.utils.ZohoException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.Exceptions;

/** Handles de-referencing entities from Metis. */
@Service(AppConfig.BEAN_METIS_DEREF_SERVICE)
public class MetisDereferenceService implements InitializingBean, Dereferencer {
  private static final Logger logger = LogManager.getLogger(MetisDereferenceService.class);

  private WebClient metisWebClient;
  private final JAXBContext jaxbContext;

  private final EntityManagementConfiguration config;

  /** Create a separate JAXB unmarshaller for each thread */
  private ThreadLocal<Unmarshaller> unmarshaller;

  @Autowired
  public MetisDereferenceService(
      EntityManagementConfiguration configuration, JAXBContext jaxbContext) {
    this.jaxbContext = jaxbContext;
    this.config = configuration;
  }

  @Override
  public void afterPropertiesSet() throws MalformedURLException {
    configureMetisWebClient();
    configureJaxb();
  }

  /**
   * Dereferences the entity with the given id value.
   *
   * @param id external ID for entity
   * @return An optional containing the de-referenced entity, or an empty optional if no match
   *     found.
   * @throws Exception
   * @throws ZohoException
   */
  @Override
  public Optional<Entity> dereferenceEntityById(String id) throws Exception {
    String metisResponseBody = fetchMetisResponse(id);
    XmlBaseEntityImpl<?> metisResponse =
        parseMetisResponse(unmarshaller.get(), id, metisResponseBody);
    if (metisResponse == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(metisResponse.toEntityModel());
  }

  String fetchMetisResponse(String externalId) throws Exception {
    Instant start = Instant.now();
    logger.info("De-referencing externalId={} from Metis", externalId);

    String metisResponseBody;
    try {
      metisResponseBody =
          metisWebClient
              .get()
              .uri(
                  uriBuilder ->
                      uriBuilder.path(METIS_DEREF_PATH).queryParam("uri", externalId).build())
              .accept(MediaType.APPLICATION_XML)
              .retrieve()
              // 404 response received if Metis dereference service is down
              .onStatus(
                  HttpStatus.NOT_FOUND::equals,
                  response ->
                      response.bodyToMono(String.class).map(DatasourceNotReachableException::new))
              // return 400 for other 4xx responses from Metis
              .onStatus(
                  HttpStatus::is4xxClientError,
                  response -> response.bodyToMono(String.class).map(HttpBadRequestException::new))
              // return 500 for everything else
              .onStatus(
                  HttpStatus::isError,
                  response -> response.bodyToMono(String.class).map(DatasourceUpstreamServerError::new))
              .bodyToMono(String.class)
              .block();
    }
    // thrown if DNS lookup for Metis dereference url fails
    catch (WebClientRequestException we) {
      throw new DatasourceNotReachableException(we.getMessage());
    } catch (Exception e) {
      /*
       * Spring WebFlux wraps exceptions in ReactiveError (see Exceptions.propagate())
       * We cannot handle that exception in @ControllerAdvice classes, so we unwrap the underlying
       * exception and rethrow it here.
       *
       * We check for all exceptions (instead of just EuropeanaApiException) because all its subclasses
       * return a different value for getResponseStatus()
       */

      Throwable t = Exceptions.unwrap(e);

      if (t instanceof DatasourceNotReachableException) {
        throw new DatasourceNotReachableException(t.getMessage());
      }
      if (t instanceof HttpBadRequestException) {
        throw new HttpBadRequestException(t.getMessage());
      }

      if (t instanceof DatasourceUpstreamServerError) {
        throw new DatasourceUpstreamServerError(t.getMessage());
      }

      throw new Exception(t);
    }

    long duration = Duration.between(start, Instant.now()).toMillis();
    logger.info("Received Metis response for externalId={}. Duration={}ms", externalId, duration);
    if (logger.isDebugEnabled()) {
      logger.debug("Metis response for externalId={}: {}", externalId, metisResponseBody);
    }
    return metisResponseBody;
  }

  private void configureJaxb() {
    unmarshaller =
        ThreadLocal.withInitial(
            () -> {
              try {
                return jaxbContext.createUnmarshaller();
              } catch (JAXBException e) {
                throw new FunctionalRuntimeException("Error creating JAXB unmarshaller ", e);
              }
            });
  }

  private void configureMetisWebClient() throws MalformedURLException {
    WebClient.Builder webClientBuilder = WebClient.builder();
    if (config.useMetisProxy()) {
      String defaultHostHeader = new URL(config.getMetisBaseUrl()).getHost();
      String proxyUrl = ensureNoTrailingSlash(config.getMetisProxyUrl());

      webClientBuilder
          .defaultHeader(HttpHeaders.HOST, defaultHostHeader)
          // ensure that baseUrl has a trailing slash
          .baseUrl(proxyUrl);
      logger.info(
          "Using proxy for Metis dereferencing. defaultHostHeader={}; proxy={}",
          defaultHostHeader,
          proxyUrl);
    } else {
      webClientBuilder.baseUrl(ensureNoTrailingSlash(config.getMetisBaseUrl()));
    }

    logger.info("Metis baseUrl={}", config.getMetisBaseUrl());
    this.metisWebClient = webClientBuilder.build();
  }

  private String ensureNoTrailingSlash(String url) {
    return url.endsWith("/") ? StringUtils.substring(url, 0, url.length() - 1) : url;
  }
}
