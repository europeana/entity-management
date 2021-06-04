package eu.europeana.entitymanagement.solr.exception;

import org.springframework.http.HttpStatus;

import eu.europeana.api.commons.error.EuropeanaApiException;

public class SolrServiceException extends EuropeanaApiException {

	private static final long serialVersionUID = -167560566275881316L;

	public SolrServiceException(String message, Throwable th) {
		super(message, th);
	}

	public SolrServiceException(String message) {
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

    @Override
    public boolean doExposeMessage() {
        return true;
    }

    @Override
    public HttpStatus getResponseStatus() {
        return HttpStatus.BAD_REQUEST;
    }
	
}
