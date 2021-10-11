package eu.europeana.entitymanagement.definitions.exceptions;

/**
 * This class is used represent validation errors for the grouping class hierarchy
 *  
 * @author GrafR 
 *
 */
public class EntityFieldAccessException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6895963160368650224L;

	public EntityFieldAccessException(String message){
		super(message);
	}
	
	public EntityFieldAccessException(String message, Throwable th){
		super(message, th);
	}
	
	
}
