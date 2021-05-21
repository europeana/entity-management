package eu.europeana.entitymanagement.exception;

import eu.europeana.api.commons.error.EuropeanaApiException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown for all errors that occur while creating or saving an entity to the database
 */
public class EntityCreationException extends EuropeanaApiException {
    /**
     * 
     */
    private static final long serialVersionUID = -3644250114112153984L;

    public EntityCreationException(String msg) {
        super(msg);
    }
    
    public EntityCreationException(String msg, Throwable th) {
        super(msg);
    }

    /**
     * @return boolean indicating whether this type of exception should be logged or not
     */
    @Override
    public boolean doLog() {
        return true;
    }

    public boolean logStacktrace() {
        return true;
    }

    @Override
    public HttpStatus getResponseStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
