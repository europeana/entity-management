package eu.europeana.entitymanagement.exception.ingestion;

import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.definitions.model.Entity;
import java.util.Set;
import javax.validation.ConstraintViolation;

public class EntityValidationException extends EuropeanaApiException {

  /** */
  private static final long serialVersionUID = 4490232069167863982L;

  private final transient Set<ConstraintViolation<Entity>> validationErrors;

  public EntityValidationException(
      String message, Set<ConstraintViolation<Entity>> validationErrors) {
    super(message);
    this.validationErrors = validationErrors;
  }

  @Override
  public String toString() {
    return buildErrorMessage(super.getMessage());
  }

  String buildErrorMessage(String errorMessage) {
    StringBuilder message = new StringBuilder(errorMessage);
    return getConstrainsValidationMessage(message);
  }

  public String getConstrainsValidationMessage(StringBuilder message) {
    if (validationErrors != null) {
      message.append("Constraint violations: ");
      for (ConstraintViolation<Entity> violation : validationErrors) {
        message.append("\n").append(violation.getMessage());
      }
    }
    return message.toString();
  }

  @Override
  public String getMessage() {
    // ensure that constraint violations are included in the error message
    return buildErrorMessage(super.getMessage());
  }
}
