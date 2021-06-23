package eu.europeana.entitymanagement.exception;

import eu.europeana.api.commons.error.EuropeanaApiException;

/**
 * Exception thrown during de-referenciation, if Metis returns an empty response
 */
public class MetisNotKnownException extends EuropeanaApiException {
    public MetisNotKnownException(String message) {
        super(message);
    }

    @Override
    public boolean doLog() {
        return true;
    }

    @Override
    public boolean doLogStacktrace() {
        return false;
    }
}
