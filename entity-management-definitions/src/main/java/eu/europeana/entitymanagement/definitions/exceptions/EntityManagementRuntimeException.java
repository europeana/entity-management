package eu.europeana.entitymanagement.definitions.exceptions;

public class EntityManagementRuntimeException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -8560947199642673237L;

    public EntityManagementRuntimeException(String message) {
	super(message);
    }

    public EntityManagementRuntimeException(String message, Throwable th) {
	super(message, th);
    }
}
