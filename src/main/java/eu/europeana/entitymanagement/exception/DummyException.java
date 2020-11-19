package eu.europeana.entitymanagement.exception;

import eu.europeana.api.commons.error.EuropeanaApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * An exception to demonstrate error handling with ResponseStatus annotation
 * It's recommended that all exceptions created in the API extends the EuropeanaApiException
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DummyException extends EuropeanaApiException {

    /**
     * Initialise a new exception for which there is no root cause
     * @param msg error message
     */
    public DummyException(String msg) {
        super(msg);
    }

    /**
     * Initialise a new exception for which there is no root cause
     * @param msg error message
     * @param errorCode error code
     */
    public DummyException(String msg, String errorCode) {
        super(msg, errorCode);
    }

    /**
     * We don't want to log the stack trace for this exception
     * @return false
     */
    @Override
    public boolean doLogStacktrace() {
        return false;
    }

}
