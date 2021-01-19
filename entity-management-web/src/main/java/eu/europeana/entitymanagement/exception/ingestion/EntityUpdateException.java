package eu.europeana.entitymanagement.exception.ingestion;

public class EntityUpdateException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 6189347322304269972L;

    public EntityUpdateException(String message, Throwable th) {
	super(message, th);
    }

    public EntityUpdateException(String message) {
	super(message);
    }
}
