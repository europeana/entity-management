package eu.europeana.entitymanagement.exception;

public class ScoringComputationException extends RuntimeException {

  /** */
  private static final long serialVersionUID = -167560566275881316L;

  public ScoringComputationException(String message, Throwable th) {
    super(message, th);
  }

  public ScoringComputationException(String message) {
    super(message);
  }
}
