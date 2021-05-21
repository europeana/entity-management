package eu.europeana.entitymanagement.exception;


import eu.europeana.api.commons.error.EuropeanaApiException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a entity already exists
 */
public class EntityAlreadyExistsException extends EuropeanaApiException {

    private static final long serialVersionUID = -2506967519765835153L;

    public EntityAlreadyExistsException(String entityUri) {
        super("Entity already exists for '" + entityUri + "' found");
    }

    @Override
    public boolean doLog() {
        return false;
    }


    @Override
    public HttpStatus getResponseStatus() {
        return HttpStatus.BAD_REQUEST;
    }


    @Override
    public boolean doLogStacktrace() {
        return false;
    }
}
