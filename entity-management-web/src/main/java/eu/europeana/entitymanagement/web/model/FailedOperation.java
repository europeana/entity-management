package eu.europeana.entitymanagement.web.model;

import static eu.europeana.entitymanagement.web.model.ZohoSyncReportFields.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class used for serialization of zoho sync errors
 */
@JsonPropertyOrder({
  ID,
  ERROR,
  MESSAGE,
  TRACE
})
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class FailedOperation {

  private String zohoId;
  private String error;
  private String message;
  private String trace;
  
  /**
   * Main constructor
   * @param zohoId - the zoho URI
   * @param error - the label of the error
   * @param message - the explicit message indicating the failed operation
   * @param trace - the serialized stacktrace of the occured error
   */
  public FailedOperation(String zohoId, String error, String message, String trace) {
    this.zohoId = zohoId;
    this.error = error;
    this.message = message;
    this.trace = trace;
  }
  
  /**
   * Getter method
   * @return the zoho organization id
   */
  @JsonProperty(ID)
  public String getZohoId() {
    return zohoId;
  }
  /**
   * setter method
   * @param zohoId - the zoho organization id
   */
  public void setZohoId(String zohoId) {
    this.zohoId = zohoId;
  }
  
  /**
   * getter method
   * @return - the error label
   */
  @JsonProperty(ERROR)
  public String getError() {
    return error;
  }
  
  /**
   * setter method
   * @param error - the error label
   */
  public void setError(String error) {
    this.error = error;
  }
  
  /**
   * Getter method
   * @return the error message
   */
  @JsonProperty(MESSAGE)
  public String getMessage() {
    return message;
  }
  
  /**
   * setter method
   * @param message the error message
   */
  public void setMessage(String message) {
    this.message = message;
  }
  
  /**
   * getter method
   * @return the serialized stackTrace
   */
  @JsonProperty(TRACE)
  public String getTrace() {
    return trace;
  }
  
  /**
   * setter method
   * @param trace the serilized stacktrace
   */
  public void setTrace(String trace) {
    this.trace = trace;
  }
  
  
}
