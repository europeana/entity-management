package eu.europeana.entitymanagement.exception;


public class EntityRetrievalException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -167560566275881316L;

	public EntityRetrievalException(String message, Throwable th) {
		super(message, th);
	}

	public EntityRetrievalException(String message) {
		super(message);
	}
	
	
}
