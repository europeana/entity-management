package eu.europeana.entitymanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Setup CORS for all requests and setup default Content-type */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

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
    configurer.favorPathExtension(true);

    // use application/ld+json if no Content-Type is specified
    configurer.defaultContentType(
        MediaType.valueOf(eu.europeana.api.commons.web.http.HttpHeaders.CONTENT_TYPE_JSONLD));
  }
}
