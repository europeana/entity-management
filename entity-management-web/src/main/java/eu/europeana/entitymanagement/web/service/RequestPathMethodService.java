package eu.europeana.entitymanagement.web.service;

import eu.europeana.api.commons.web.service.AbstractRequestPathMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

/** This service is used to populate the Allow header in API responses. */
@Service
@ConditionalOnWebApplication
public class RequestPathMethodService extends AbstractRequestPathMethodService {

  @Autowired
  public RequestPathMethodService(WebApplicationContext applicationContext) {
    super(applicationContext);
  }
}
