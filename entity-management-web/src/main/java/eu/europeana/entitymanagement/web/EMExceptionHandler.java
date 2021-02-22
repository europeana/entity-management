package eu.europeana.entitymanagement.web;

import org.springframework.web.bind.annotation.ControllerAdvice;

import eu.europeana.api.commons.error.EuropeanaGlobalExceptionHandler;

@ControllerAdvice
public class EMExceptionHandler extends EuropeanaGlobalExceptionHandler {
    // exception handling inherited from parent
}
