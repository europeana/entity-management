package eu.europeana.entitymanagement.exception;

import eu.europeana.api.commons.error.EuropeanaApiException;

/**
 * Exception thrown when trying to combine metadata of different entity types
 */
public class EntityMismatchException extends EuropeanaApiException {

    public EntityMismatchException(String message) {
        super(message);
    }

    @Override
    public boolean doLog() {
        return true;
    }

    @Override
    public boolean doLogStacktrace() {
        return true;
    }

    @Override
    public boolean doExposeMessage() {
        return false;
    }
}
