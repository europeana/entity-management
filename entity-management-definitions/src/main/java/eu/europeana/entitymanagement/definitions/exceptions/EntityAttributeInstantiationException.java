package eu.europeana.entitymanagement.definitions.exceptions;

/**
 * This class represents validation errors for the grouping
 *
 * @author Roman Graf
 */
public class EntityAttributeInstantiationException extends RuntimeException {

  /** */
  private static final long serialVersionUID = 3684388420062409641L;

  public static final String BASE_MESSAGE = "Cannot instantiate grouping attribute. ";
  public static final String DEFAULT_MESSAGE = "Cannot instantiate grouping attribute for type: ";
  public static final String MESSAGE_UNKNOWN_TYPE =
      "Unknown/unsurported property. Cannot instantiate grouping attribute for type: ";
  public static final String MESSAGE_UNKNOWN_KEYWORD =
      "Unknown/unsurported keyword. Cannot instantiate value of the grouping attribute using the keyword: ";
  public static final String MESSAGE_ID_NOT_URL = "ID value must be a valid URL";

  String propertyName;
  String propertyValue;

  public EntityAttributeInstantiationException(String propertyName) {
    this(propertyName, DEFAULT_MESSAGE);
  }

  public EntityAttributeInstantiationException(String propertyName, String message) {
    this(propertyName, null, message);
  }

  public EntityAttributeInstantiationException(
      String propertyName, String propertyValue, String message) {
    this(propertyName, propertyValue, message, null);
  }

  public EntityAttributeInstantiationException(
      String propertyName, String propertyValue, String message, Throwable th) {
    super(message + propertyName, th);
    this.propertyName = propertyName;
    this.propertyValue = propertyValue;
  }
}
