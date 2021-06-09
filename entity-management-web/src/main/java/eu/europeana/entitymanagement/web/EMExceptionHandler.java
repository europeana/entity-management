package eu.europeana.entitymanagement.web;

import eu.europeana.api.commons.error.EuropeanaApiErrorResponse;
import eu.europeana.api.commons.error.EuropeanaGlobalExceptionHandler;
import eu.europeana.api.commons.web.exception.HttpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    @ExceptionHandler
    public ResponseEntity<EuropeanaApiErrorResponse> handleException(HttpMessageNotReadableException e, HttpServletRequest httpRequest) {
        EuropeanaApiErrorResponse response = new EuropeanaApiErrorResponse.Builder(httpRequest, e, stackTraceEnabled())
            .setStatus(HttpStatus.BAD_REQUEST.value())
            .setError("Error parsing request body")
            .setMessage("JSON is either malformed or missing required 'type' property")
            .build();

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST.value())
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<EuropeanaApiErrorResponse> handleMethodArgNotValidException(MethodArgumentNotValidException e, HttpServletRequest httpRequest) {
        BindingResult result = e.getBindingResult();
        String error ="";
        List<FieldError> fieldErrors = result.getFieldErrors();
       if(!fieldErrors.isEmpty()) {
           // just return the first error
           error = fieldErrors.get(0).getField() + " " + fieldErrors.get(0).getDefaultMessage();
       }
        EuropeanaApiErrorResponse response = new EuropeanaApiErrorResponse.Builder(httpRequest, e, stackTraceEnabled())
                .setStatus(HttpStatus.BAD_REQUEST.value())
                .setMessage("Invalid request body")
                .setError(error)
                .build();

        return ResponseEntity
                .status(response.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }
}
