package eu.europeana.entitymanagement.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import eu.europeana.api.commons.web.service.AbstractRequestPathMethodService;

/** This service is used to populate the Allow header in API responses. */
@Service(value = "requestPathMethodService")
@ConditionalOnWebApplication
public class RequestPathMethodService extends AbstractRequestPathMethodService {

  @Autowired
  public RequestPathMethodService(WebApplicationContext applicationContext) {
    super(applicationContext);
  }
}
