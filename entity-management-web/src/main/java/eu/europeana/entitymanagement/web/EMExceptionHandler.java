package eu.europeana.entitymanagement.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import eu.europeana.api.commons.error.EuropeanaApiErrorResponse;
import eu.europeana.api.commons.error.EuropeanaGlobalExceptionHandler;
import eu.europeana.api.commons.web.exception.HttpException;

@ControllerAdvice
public class EMExceptionHandler extends EuropeanaGlobalExceptionHandler {
    // exception handling inherited from parent
    
    /**
     * Default handler for EuropeanaApiException types
     *
     * @param e caught exception
     */
    @ExceptionHandler
    public ResponseEntity<EuropeanaApiErrorResponse> handleCommonHttpException(HttpException e, HttpServletRequest httpRequest) {
        
	//TODO: harmonize the use of HTTP Exceptions and EuropeanaAPIExceptions
	EuropeanaApiErrorResponse response = new EuropeanaApiErrorResponse.Builder(httpRequest, e, stackTraceEnabled())
                .setStatus(e.getStatus().value())
                .setError(e.getStatus().getReasonPhrase())
                .setMessage(e.getMessage())
                // code only included in JSON if a value is set in exception
                .setCode(e.getI18nKey())
                .build();

        return ResponseEntity
                .status(e.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }
}
