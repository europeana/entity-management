package eu.europeana.entitymanagement.web.model;

import static eu.europeana.entitymanagement.web.model.ZohoSyncReportFields.ERROR;
import static eu.europeana.entitymanagement.web.model.ZohoSyncReportFields.ID;
import static eu.europeana.entitymanagement.web.model.ZohoSyncReportFields.MESSAGE;
import static eu.europeana.entitymanagement.web.model.ZohoSyncReportFields.TRACE;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

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
  
  public FailedOperation(String zohoId, String error, String message, String trace) {
    this.zohoId = zohoId;
    this.error = error;
    this.message = message;
    this.trace = trace;
  }
  
  @JsonProperty(ID)  
  public String getZohoId() {
    return zohoId;
  }
  public void setZohoId(String zohoId) {
    this.zohoId = zohoId;
  }
  @JsonProperty(ERROR)
  public String getError() {
    return error;
  }
  public void setError(String error) {
    this.error = error;
  }
  @JsonProperty(MESSAGE)
  public String getMessage() {
    return message;
  }
  public void setMessage(String message) {
    this.message = message;
  }
  @JsonProperty(TRACE)
  public String getTrace() {
    return trace;
  }
  public void setTrace(String trace) {
    this.trace = trace;
  }
  
  
}
