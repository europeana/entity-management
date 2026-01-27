package eu.europeana.entitymanagement.zoho.utils;

/**
 * Exception class for indicating wikidata dereferencing issues
 */
public class WikidataAccessException extends DereferencingException {

  private static final long serialVersionUID = 7724261367420984595L;

  public WikidataAccessException(String msg, Throwable t) {
    super(msg, t);
  }

  public WikidataAccessException(String msg) {
    super(msg);
  }
}
