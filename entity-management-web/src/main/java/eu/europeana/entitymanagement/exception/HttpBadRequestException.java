package eu.europeana.entitymanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import eu.europeana.api.commons.error.EuropeanaApiException;

/**
 * Exception thrown when an error occurs due to bad user input.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class HttpBadRequestException extends EuropeanaApiException {

    /**
     * 
     */
    private static final long serialVersionUID = -4536642651964523519L;

    /**
     * Initialise a new exception for which there is no root cause
     *
     * @param msg error message
     */
    public HttpBadRequestException(String msg) {
        super(msg);
    }

    /**
     * Initialise a new exception with the message and root cause
     *
     * @param msg error message
     */
    public HttpBadRequestException(String msg, Throwable th) {
        super(msg, th);
    }
    
    
    /**
     * Initialise a new exception for which there is no root cause
     *
     * @param msg       error message
     * @param errorCode error code
     */
    public HttpBadRequestException(String msg, String errorCode) {
        super(msg, errorCode);
    }

    /**
     * We don't want to log the stack trace for this exception
     *
     * @return false
     */
    @Override
    public boolean doLogStacktrace() {
        return false;
    }

    @Override
    public HttpStatus getResponseStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}