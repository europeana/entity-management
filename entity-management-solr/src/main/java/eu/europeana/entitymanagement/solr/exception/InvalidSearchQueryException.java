package eu.europeana.entitymanagement.solr.exception;

import org.springframework.http.HttpStatus;

/** Exception thrown when a search request contains unknown fields or invalid syntax */
public class InvalidSearchQueryException extends SolrServiceException {
  public InvalidSearchQueryException(String message) {
    super(message);
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
