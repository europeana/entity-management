package eu.europeana.entitymanagement.web;

import eu.europeana.api.commons.error.EuropeanaApiErrorResponse;
import eu.europeana.api.commons.error.EuropeanaGlobalExceptionHandler;
import eu.europeana.api.commons.web.exception.HttpException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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


    /**
     * Exception thrown by Spring when RequestBody validation fails.
     */
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

    /**
     * Customise the response for {@link org.springframework.web.HttpRequestMethodNotSupportedException}
     * TODO: move to api-commons
     */
    //TODO: check alignment with api-commons method
//    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
//    public ResponseEntity<EuropeanaApiErrorResponse> handleHttpRequestMethodNotSupported(
//            HttpRequestMethodNotSupportedException e, HttpServletRequest httpRequest) {
//
//        EuropeanaApiErrorResponse response = new EuropeanaApiErrorResponse.Builder(httpRequest, e, stackTraceEnabled())
//                .setStatus(HttpStatus.METHOD_NOT_ALLOWED.value())
//                .setMessage(e.getMessage())
//                .setError("Invalid method for request path")
//                .build();
//
//        Set<HttpMethod> supportedMethods = e.getSupportedHttpMethods();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        if (supportedMethods != null) {
//            headers.setAllow(supportedMethods);
//        }
//        return new ResponseEntity<>(response, headers, HttpStatus.METHOD_NOT_ALLOWED);
//    }
   
}
