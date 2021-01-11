package eu.europeana.entitymanagement.exception;

import eu.europeana.api.commons.error.EuropeanaGlobalExceptionHandler;
import io.micrometer.core.instrument.util.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Add a class with the @ControllerAdvice annotation that extends the EuropeanaGlobalExceptionHandler
 * Basic error processing (whether to log and error or not, plus handle ConstraintViolations) is done in the
 * EuropeanaGlobalExceptionHandler, but you can add more error handling here
 */
@ControllerAdvice
public class MyGlobalExceptionHandler extends EuropeanaGlobalExceptionHandler {

    // error handling inherited from parent

}
