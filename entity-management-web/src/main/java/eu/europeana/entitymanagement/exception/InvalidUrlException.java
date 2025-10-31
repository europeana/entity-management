package eu.europeana.entitymanagement.exception;

import eu.europeana.api.commons.error.EuropeanaApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Exception thrown when requested url is not found. */

@ResponseStatus(HttpStatus.NOT_FOUND)
public class InvalidUrlException extends EuropeanaApiException {

    /** */
    private static final long serialVersionUID = -4536642651964523519L;

    /**
     * Initialise a new exception for which there is no root cause
     *
     * @param msg error message
     */
    public InvalidUrlException(String msg) {
        super(msg);
    }

    /**
     * Initialise a new exception with the message and root cause
     *
     * @param msg error message
     */
    public InvalidUrlException(String msg, Throwable th) {
        super(msg, th);
    }

    /**
     * Initialise a new exception for which there is no root cause
     *
     * @param msg error message
     * @param errorCode error code
     */
    public InvalidUrlException(String msg, String errorCode) {
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
        return HttpStatus.NOT_FOUND;
    }
}

