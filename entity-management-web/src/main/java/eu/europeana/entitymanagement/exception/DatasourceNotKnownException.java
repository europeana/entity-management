package eu.europeana.entitymanagement.exception;

import eu.europeana.api.commons.error.EuropeanaApiException;

/**
 * Exception thrown during de-referenciation, if datasource returns an empty response
 */
public class DatasourceNotKnownException extends EuropeanaApiException {
    public DatasourceNotKnownException(String message) {
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
