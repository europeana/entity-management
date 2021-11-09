package eu.europeana.entitymanagement.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Setup CORS for all requests and setup default Content-type */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  MediaType jsonLdMediaType =
      MediaType.valueOf(eu.europeana.api.commons.web.http.HttpHeaders.CONTENT_TYPE_JSONLD);
  Map<String, MediaType> mediaTypesMaping = new HashMap<String, MediaType>();

  /** Setup CORS for all GET, HEAD and OPTIONS, requests. */
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/**")
        .allowedOrigins("*")
        .allowedMethods("*")
        .allowedHeaders("*")
        .allowCredentials(false)
        .exposedHeaders(HttpHeaders.ALLOW)
        .maxAge(1000L); // in seconds
  }

  /*
   * Enable content negotiation via path extension (as long as Spring supports it) and set default content type in
   * case we receive a request without an extension or Accept header
   */
  @Override
  public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    // Enable content negotiation via path extension. Note that this is deprecated with Spring
    // 5.2.4,
    // (see also https://github.com/spring-projects/spring-framework/issues/24179), so it may not
    // work in future
    // releases
    // TODO: replace with configurer.strategies(null)
    configurer.favorPathExtension(true);

    // use registered extensions instead of defaults
    configurer.useRegisteredExtensionsOnly(true);

    configurer.mediaTypes(getMediaTypesMapping());

    // use application/ld+json if no Content-Type is specified
    configurer.defaultContentType(
        MediaType.valueOf(eu.europeana.api.commons.web.http.HttpHeaders.CONTENT_TYPE_JSONLD));
  }

  private Map<String, MediaType> getMediaTypesMapping() {

    // json
    mediaTypesMaping.put("json", MediaType.APPLICATION_JSON);

    // jsonld covers also schema.jsonld
    mediaTypesMaping.put("jsonld", jsonLdMediaType);

    // xml
    mediaTypesMaping.put("xml", MediaType.APPLICATION_XML);

    // in case we want to support the .rdf extention later
    //      mediaTypesMaping.put("rdf", MediaType.APPLICATION_XML);

    return mediaTypesMaping;
  }
}
