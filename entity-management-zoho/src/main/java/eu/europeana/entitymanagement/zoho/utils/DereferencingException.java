package eu.europeana.entitymanagement.zoho.utils;

import eu.europeana.api.commons.error.EuropeanaApiException;

/** Base exception class for dereferencing errors. */
public class DereferencingException extends EuropeanaApiException {

  private static final long serialVersionUID = -3332292346834265370L;

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message. The detail message is saved for later retrieval by the
   *     {@link #getMessage()} method.
   */
  public DereferencingException(String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   *
   * @param message the detail message (which is saved for later retrieval by the {@link
   *     #getMessage()} method).
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
   *     (A <tt>null</tt> value is permitted, and indicates that the cause is nonexistent or
   *     unknown.)
   */
  public DereferencingException(String message, Throwable cause) {
    super(message, cause);
  }
}
