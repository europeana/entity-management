package eu.europeana.entitymanagement.exception;


import eu.europeana.api.commons.error.EuropeanaApiException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested entity isn't found
 */
public class EntityNotFoundException extends EuropeanaApiException {

    private static final long serialVersionUID = -2506967519765835153L;

    public EntityNotFoundException(String entityUri) {
        super("No entity with uri '" + entityUri + "' found");
    }

    @Override
    public HttpStatus getResponseStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
