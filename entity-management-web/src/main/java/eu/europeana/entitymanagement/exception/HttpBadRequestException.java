package eu.europeana.entitymanagement.exception;


import eu.europeana.api.commons.error.EuropeanaApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a web request cannot be properly handled.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class HttpBadRequestException extends EuropeanaApiException {

    /**
     * Initialise a new exception for which there is no root cause
     *
     * @param msg error message
     */
    public HttpBadRequestException(String msg) {
        super(msg);
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
}
