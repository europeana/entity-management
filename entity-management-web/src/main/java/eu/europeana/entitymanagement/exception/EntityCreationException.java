package eu.europeana.entitymanagement.exception;

import eu.europeana.api.commons.error.EuropeanaApiException;

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
}
