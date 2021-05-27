package eu.europeana.entitymanagement.solr.exception;

public class SolrServiceException extends Exception {

	private static final long serialVersionUID = -167560566275881316L;

	public SolrServiceException(String message, Throwable th) {
		super(message, th);
	}

	public SolrServiceException(String message) {
		super(message);
	}
	
	
}
