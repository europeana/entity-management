package eu.europeana.entitymanagement.definitions.exceptions;

// TODO: This class exists also in the set-api, xonsider moving to the api-commons
public class EMProfileValidationException extends Exception {

  private static final long serialVersionUID = -7879758994043610997L;
  public static final String ERROR_INVALID_PROFILE = "Invalid value for requested profile!";
  private String requestedProfile;

  public String getRequestedProfile() {
    return requestedProfile;
  }

  public void setRequestedProfile(String requestedProfile) {
    this.requestedProfile = requestedProfile;
  }

  public EMProfileValidationException(String requestedProfile) {
    this(ERROR_INVALID_PROFILE, requestedProfile, null);
  }

  public EMProfileValidationException(String message, String requestedProfile, Throwable th) {
    super(message, th);
    this.requestedProfile = requestedProfile;
  }
}
