package eu.europeana.entitymanagement.zoho;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.zoho.api.authenticator.OAuthToken;
import com.zoho.api.authenticator.OAuthToken.TokenType;
import com.zoho.api.authenticator.Token;
import com.zoho.api.authenticator.store.TokenStore;
import com.zoho.crm.api.HeaderMap;
import com.zoho.crm.api.Initializer;
import com.zoho.crm.api.ParameterMap;
import com.zoho.crm.api.SDKConfig;
import com.zoho.crm.api.UserSignature;
import com.zoho.crm.api.dc.DataCenter.Environment;
import com.zoho.crm.api.dc.USDataCenter;
import com.zoho.crm.api.exception.SDKException;
import com.zoho.crm.api.record.APIException;
import com.zoho.crm.api.record.ActionHandler;
import com.zoho.crm.api.record.ActionResponse;
import com.zoho.crm.api.record.ActionWrapper;
import com.zoho.crm.api.record.BodyWrapper;
import com.zoho.crm.api.record.DeletedRecord;
import com.zoho.crm.api.record.DeletedRecordsHandler;
import com.zoho.crm.api.record.DeletedRecordsWrapper;
import com.zoho.crm.api.record.Record;
import com.zoho.crm.api.record.RecordOperations;
import com.zoho.crm.api.record.RecordOperations.GetDeletedRecordsParam;
import com.zoho.crm.api.record.RecordOperations.GetRecordsHeader;
import com.zoho.crm.api.record.RecordOperations.GetRecordsParam;
import com.zoho.crm.api.record.RecordOperations.SearchRecordsParam;
import com.zoho.crm.api.record.ResponseHandler;
import com.zoho.crm.api.record.ResponseWrapper;
import com.zoho.crm.api.record.SuccessResponse;
import com.zoho.crm.api.util.APIResponse;
import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import eu.europeana.entitymanagement.zoho.utils.ZohoConstants;
import eu.europeana.entitymanagement.zoho.utils.ZohoException;

public class ZohoAccessClient {

  private static final Logger LOGGER = LogManager.getLogger(ZohoAccessClient.class);

  /**
   * Constructor with all parameters.
   *
   * <p>It will try to initialize the connection with the Zoho service. Uses the grant token for the
   * initial setup with {@link Initializer#initialize(UserSignature, Environment, Token, TokenStore,
   * SDKConfig, String)}. This process does <b>NOT</b> generate any refresh/access tokens. A call to
   * one of the methods that accesses Zoho should be used after creation of an instance of this
   * class to generate refresh/access tokens using the provided grant token(grant tokens have a very
   * short TTL that is imposed when the grant token is requested from the Zoho api console web
   * page). If the grant token was already used once before, then an extra call is not required and
   * the refresh and/or access tokens should be already present in the token store.
   *
   * @param tokenStore the token store to be used
   * @param zohoEmail the zoho email
   * @param clientId the zoho client id
   * @param clientSecret the zoho client secret
   * @param refreshToken the zoho initial refresh token
   * @param redirectUrl the registered zoho redirect url
   */
  public ZohoAccessClient(
      TokenStore tokenStore,
      String zohoEmail,
      String clientId,
      String clientSecret,
      String refreshToken,
      String redirectUrl)
      throws ZohoException {
    try {
      UserSignature userSignature = new UserSignature(zohoEmail);
      Token token =
          new OAuthToken(clientId, clientSecret, refreshToken, TokenType.REFRESH, redirectUrl);
      SDKConfig sdkConfig =
          new SDKConfig.Builder().setAutoRefreshFields(false).setPickListValidation(true).build();
      Environment environment = USDataCenter.PRODUCTION;
      String resourcePath = SystemUtils.getUserHome().getAbsolutePath();
      // Does not generate any tokens, we'll need to execute a command to do so
      Initializer.initialize(
          userSignature, environment, token, tokenStore, sdkConfig, resourcePath);
    } catch (SDKException e) {
      throw new ZohoException("Error initializing ZohoAccessClient", e);
    }
  }

  public Optional<Record> getZohoRecordOrganizationById(String zohoUrl) throws ZohoException {
    String zohoId = EntityRecordUtils.getIdFromUrl(zohoUrl);
    try {
      RecordOperations recordOperations = new RecordOperations();
      ParameterMap paramInstance = new ParameterMap();
      paramInstance.add(
          SearchRecordsParam.CRITERIA,
          String.format(
              ZohoConstants.ZOHO_OPERATION_FORMAT_STRING,
              ZohoConstants.ID_FIELD,
              ZohoConstants.EQUALS_OPERATION,
              zohoId));

      APIResponse<ResponseHandler> response =
          recordOperations.searchRecords(ZohoConstants.ACCOUNTS_MODULE_NAME, paramInstance);
      return getZohoRecords(response).stream().findFirst();
    } catch (SDKException e) {
      throw new ZohoException("Zoho search organization by organization id threw an exception", e);
    }
  }
  
  public void updateZohoRecordOrganizationStringField(String zohoUrl, String fieldAPIName, String fieldValue) throws ZohoException {
    String zohoId = EntityRecordUtils.getIdFromUrl(zohoUrl);
    try {
      RecordOperations recordOperations = new RecordOperations();
      BodyWrapper request = new BodyWrapper();
      List<Record> records = new ArrayList<Record> ();
      Record record1 = new Record();
      record1.addKeyValue(fieldAPIName, fieldValue);
      records.add(record1);
      request.setData(records);
      List <String> trigger = new ArrayList<String> ();
      trigger.add("approval");
      trigger.add("workflow");
      trigger.add("blueprint");
      request.setTrigger(trigger);
      
      //Call updateRecord method that takes recordId, ModuleAPIName and BodyWrapper instance as parameter.
      APIResponse<ActionHandler> response = recordOperations.updateRecord(Long.valueOf(zohoId), ZohoConstants.ACCOUNTS_MODULE_NAME, request);
      validateZohoUpdateResponse(response);
    } catch (SDKException e) {
      throw new ZohoException("Zoho update the organization field threw an exception.", e);
    }
  }

  /**
   * Source: https://www.zoho.com/crm/developer/docs/java-sdk/v2/record-samples.html
   * @throws ZohoException 
   */
  private void validateZohoUpdateResponse(APIResponse<ActionHandler> response) throws ZohoException {
    if(response != null)
    {
      //Get the status code from response (response.getStatusCode())
      //Check if expected response is received
      if(response.isExpected())
      {
        //Get object from response
        ActionHandler actionHandler = response.getObject(); 
        if(actionHandler instanceof ActionWrapper)
        {
            //Get the received ResponseWrapper instance
            ActionWrapper actionWrapper = (ActionWrapper) actionHandler;  
            //Get the list of obtained ActionResponse instances
            List<ActionResponse> actionResponses = actionWrapper.getData();
            for(ActionResponse actionResponse : actionResponses)
            {
                //Check if the request is successful
                if(actionResponse instanceof SuccessResponse)
                {
                  //Get the received SuccessResponse instance
                  // SuccessResponse successResponse = (SuccessResponse)actionResponse;
                  // status, code, and message can be taken with: successResponse.getStatus().getValue(), successResponse.getCode().getValue(), and successResponse.getMessage().getValue()
                  continue;
                }
                //Check if the request returned an exception
                else if(actionResponse instanceof APIException)
                {
                  //Get the received APIException instance
                  APIException exception = (APIException) actionResponse;                        
                  throw new ZohoException("Exeption during updating a field in Zoho. Status: " + exception.getStatus().getValue() +
                      ", code: " + exception.getCode().getValue() + ", message: " + exception.getMessage().getValue());
                }
            }
        }
        //Check if the request returned an exception
        else if(actionHandler instanceof APIException)
        {
            //Get the received APIException instance
            APIException exception = (APIException) actionHandler;                
            throw new ZohoException("Exeption during updating a field in Zoho. Status: " + exception.getStatus().getValue() +
                ", code: " + exception.getCode().getValue() + ", message: " + exception.getMessage().getValue());
        }
      }
      else
      {//If response is not as expected            
        throw new ZohoException("Unexpected response during updating a field in Zoho.");
      }
    }
    
  }

  /**
   * Get organization items paged, filtering by modifiedDate date and searchCriteria.
   *
   * @param page first index starts with 1
   * @param pageSize the number of entries to be returned, Zoho will have an upper limit.
   * @param modifiedDate the date of last modification to check
   * @return the list of Zoho Records (Organizations)
   * @throws ZohoException if an error occurred during accessing Zoho
   */
  public List<Record> getZcrmRecordOrganizations(
      int page, int pageSize, OffsetDateTime modifiedDate) throws ZohoException {

    if (page < 1 || pageSize < 1) {
      throw new ZohoException(
          "Invalid page or pageSize index. Index must be >= 1",
          new IllegalArgumentException(
              String.format("Provided page: %s, and pageSize: %s", page, pageSize)));
    }

    try {
      APIResponse<ResponseHandler> response;
      RecordOperations recordOperations = new RecordOperations();
      ParameterMap paramInstance = new ParameterMap();
      paramInstance.add(GetRecordsParam.PAGE, page);
      paramInstance.add(GetRecordsParam.PER_PAGE, pageSize);
      HeaderMap headerInstance = new HeaderMap();
      headerInstance.add(GetRecordsHeader.IF_MODIFIED_SINCE, modifiedDate);
      response =
          recordOperations.getRecords(
              ZohoConstants.ACCOUNTS_MODULE_NAME, paramInstance, headerInstance);

      return getZohoRecords(response);
    } catch (SDKException e) {
      throw new ZohoException(
          "Cannot get organization list page: " + page + " pageSize :" + pageSize, e);
    }
  }

  /**
   * Using the search criteria provided and the modifiedDate if available it will create the
   * criteria in the format that Zoho accepts. Result will be depicted as
   * "(field1:equals:valueA)OR(field1:equals:valueB)OR(field2:equals:valueC)" or "".
   *
   * @param searchCriteria the search criteria map provided, values can be comma separated per key
   * @param criteriaOperator the criteriaOperator used for each parameter, can be one of {@link
   *     ZohoConstants#EQUALS_OPERATION},{@link ZohoConstants#STARTS_WITH_OPERATION}. If not
   *     provided or wrong value, it will default to {@link ZohoConstants#EQUALS_OPERATION}.
   * @return the created criteria in the format Zoho accepts
   */
  String createZohoCriteriaString(Map<String, String> searchCriteria, String criteriaOperator) {
    if (isNullOrEmpty(searchCriteria)) {
      searchCriteria = new HashMap<>();
    }

    if (Objects.isNull(criteriaOperator)
        || (!ZohoConstants.EQUALS_OPERATION.equals(criteriaOperator)
            && !ZohoConstants.STARTS_WITH_OPERATION.equals(criteriaOperator))) {
      criteriaOperator = ZohoConstants.EQUALS_OPERATION;
    }

    String finalCriteriaOperator = criteriaOperator;
    return searchCriteria.entrySet().stream()
        .map(
            entry ->
                Arrays.stream(entry.getValue().split(ZohoConstants.DELIMITER_COMMA))
                    .map(
                        value ->
                            String.format(
                                ZohoConstants.ZOHO_OPERATION_FORMAT_STRING,
                                entry.getKey(),
                                finalCriteriaOperator,
                                value.trim()))
                    .collect(Collectors.joining(ZohoConstants.OR)))
        .collect(Collectors.joining(ZohoConstants.OR));
  }

  /**
   * Get deleted organization items paged.
   *
   * @param modifiedSince
   * @param startPage The number of the item from which the paging should start. First item is at
   *     number 1. Uses default number of items per page.
   * @return the list of deleted Zoho Organizations
   * @throws ZohoException if an error occurred during accessing Zoho
   */
  public List<DeletedRecord> getZohoDeletedRecordOrganizations(
      OffsetDateTime modifiedSince, int startPage, int pageSize) throws ZohoException {
    if (startPage < 1) {
      throw new ZohoException(
          "Invalid start page index. Index must be >= 1",
          new IllegalArgumentException("start page: " + startPage));
    }
    try {
      RecordOperations recordOperations = new RecordOperations();
      ParameterMap paramInstance = new ParameterMap();
      paramInstance.add(GetDeletedRecordsParam.TYPE, "all"); // all, recycle, permanent
      paramInstance.add(GetDeletedRecordsParam.PAGE, 1);
      paramInstance.add(GetDeletedRecordsParam.PER_PAGE, pageSize);
      HeaderMap headersMap = new HeaderMap();
      if (modifiedSince != null) {
        headersMap.add(GetRecordsHeader.IF_MODIFIED_SINCE, modifiedSince);
      }
      APIResponse<DeletedRecordsHandler> response =
          recordOperations.getDeletedRecords(
              ZohoConstants.ACCOUNTS_MODULE_NAME, paramInstance, headersMap);
      return getZohoDeletedRecords(response);
    } catch (SDKException e) {
      throw new ZohoException("Cannot get deleted organization list from: " + startPage, e);
    }
  }

  private List<DeletedRecord> getZohoDeletedRecords(APIResponse<DeletedRecordsHandler> response) {
    if (response != null && response.isExpected()) {
      // Get the object from response
      DeletedRecordsHandler deletedRecordsHandler = response.getObject();
      if (deletedRecordsHandler instanceof DeletedRecordsWrapper) {
        DeletedRecordsWrapper deletedRecordsWrapper = (DeletedRecordsWrapper) deletedRecordsHandler;
        return deletedRecordsWrapper.getData();
      }
    }
    return Collections.emptyList();
  }

  /**
   * Check map for nullity or emptiness
   *
   * @param m the map
   * @return true if null or empty, false otherwise
   */
  private static boolean isNullOrEmpty(final Map<?, ?> m) {
    return m == null || m.isEmpty();
  }

  /**
   * Extract records from results
   *
   * @param response the zoho response
   * @return the list of records available in results
   * @throws ZohoException if zoho response indicates error codes
   */
  public static List<Record> getZohoRecords(APIResponse<ResponseHandler> response)
      throws ZohoException {

    if (response == null) {
      return Collections.emptyList();
    }
    final int FIRST_ERROR_CODE = 400;
    if (response.getStatusCode() >= FIRST_ERROR_CODE) {
      // handle error responses
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(
            "Zoho Error. Response Status: {}, response Headers:{}",
            response.getStatusCode(),
            response.getHeaders());
      }
      throw new ZohoException("Zoho access error. Response code: " + response.getStatusCode());
    }

    if (response.isExpected()) {
      // Get the object from response
      ResponseHandler responseHandler = response.getObject();
      if (responseHandler instanceof ResponseWrapper) {
        ResponseWrapper responseWrapper = (ResponseWrapper) responseHandler;
        return responseWrapper.getData();
      }
    }
    return Collections.emptyList();
  }
}
