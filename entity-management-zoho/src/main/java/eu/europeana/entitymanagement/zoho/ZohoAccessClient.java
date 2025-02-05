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
import org.springframework.lang.NonNull;
import com.zoho.api.authenticator.OAuthToken;
import com.zoho.api.authenticator.OAuthToken.TokenType;
import com.zoho.api.authenticator.Token;
import com.zoho.api.authenticator.store.TokenStore;
import com.zoho.crm.api.HeaderMap;
import com.zoho.crm.api.Initializer;
import com.zoho.crm.api.Param;
import com.zoho.crm.api.ParameterMap;
import com.zoho.crm.api.SDKConfig;
import com.zoho.crm.api.UserSignature;
import com.zoho.crm.api.dc.DataCenter.Environment;
import com.zoho.crm.api.dc.EUDataCenter;
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
import com.zoho.crm.api.record.SuccessResponse;
import com.zoho.crm.api.relatedrecords.RelatedRecordsOperations;
import com.zoho.crm.api.util.APIResponse;
import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import eu.europeana.entitymanagement.zoho.organization.ZohoOrganizationConverter;
import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.*;
import eu.europeana.entitymanagement.zoho.utils.ZohoException;

public class ZohoAccessClient {

  private static final Logger LOGGER = LogManager.getLogger(ZohoAccessClient.class);

  /**
   * Constructor with all parameters.
   *
   * <p>
   * It will try to initialize the connection with the Zoho service. Uses the grant token for the
   * initial setup with
   * {@link Initializer#initialize(UserSignature, Environment, Token, TokenStore, SDKConfig, String)}.
   * This process does <b>NOT</b> generate any refresh/access tokens. A call to one of the methods
   * that accesses Zoho should be used after creation of an instance of this class to generate
   * refresh/access tokens using the provided grant token(grant tokens have a very short TTL that is
   * imposed when the grant token is requested from the Zoho api console web page). If the grant
   * token was already used once before, then an extra call is not required and the refresh and/or
   * access tokens should be already present in the token store.
   *
   * @param tokenStore the token store to be used
   * @param zohoEmail the zoho email
   * @param clientId the zoho client id
   * @param clientSecret the zoho client secret
   * @param refreshToken the zoho initial refresh token
   * @param redirectUrl the registered zoho redirect url
   */
  public ZohoAccessClient(TokenStore tokenStore, String zohoEmail, String clientId,
      String clientSecret, String refreshToken, String redirectUrl) throws ZohoException {

    try {
      UserSignature userSignature = new UserSignature(zohoEmail);
      Token token =
          new OAuthToken(clientId, clientSecret, refreshToken, TokenType.REFRESH, redirectUrl);
      SDKConfig sdkConfig =
          new SDKConfig.Builder().setAutoRefreshFields(false).setPickListValidation(true).build();
      // Environment environment = USDataCenter.PRODUCTION;
      Environment environment = EUDataCenter.PRODUCTION;
      String resourcePath = SystemUtils.getUserHome().getAbsolutePath();
      // Does not generate any tokens, we'll need to execute a command to do so
      Initializer.initialize(userSignature, environment, token, tokenStore, sdkConfig,
          resourcePath);
    } catch (SDKException e) {
      throw new ZohoException("Error initializing ZohoAccessClient", e);
    }
  }

  /**
   * Retrieve Zoho Organization by its zoho URL
   * 
   * @param zohoUrl the zoho url for the Organization
   * @return the retrieved zoho records
   * @throws ZohoException wrapping the original SDK exception
   */
  public Optional<Record> getZohoOrganizationByUrl(String zohoUrl) throws ZohoException {
    String zohoId = EntityRecordUtils.getIdentifierFromUrl(zohoUrl);
    return getZohoOrganizationByRecordId(zohoId);
  }

  /**
   * Get Zoho Organization by Aggregator's Zoho (record) ID For performance reasons consider first
   * to use {@link #searchZohoOrganizationByName(String)}
   * 
   * @param aggregatorZohoId the Aggregator's Zoho (record) ID
   * @return Zoho Organization as optional
   * @throws ZohoException if the remote invocation of Zoho API fails
   */
  public Optional<Record> getZohoOrganizationForAggregator(String aggregatorZohoId)
      throws ZohoException {
    Optional<Record> zohoAggregator = getZohoAggregatorByRecordId(aggregatorZohoId);
    if (zohoAggregator.isEmpty()) {
      return Optional.empty();
    }
    System.out.println("test");
    HashMap<String, String> aggInstitution =
        ZohoOrganizationConverter.getPropertyMap(zohoAggregator.get(), INSTITUTION_FIELD);
    if (aggInstitution == null || !aggInstitution.containsKey(ID_FIELD)) {
      if(LOGGER.isDebugEnabled()) {
        LOGGER.debug("Cannot retrieve institution id from aggregator with id: {}", aggregatorZohoId);
      }
      return Optional.empty();
    }

    return getZohoOrganizationByRecordId(aggInstitution.get(ID_FIELD));
  }

  public Optional<Record> getZohoOrganizationByRecordId(String zohoId) throws ZohoException {
    try {
      RecordOperations recordOperations = new RecordOperations();
      APIResponse<ResponseHandler> response =
          recordOperations.getRecord(Long.valueOf(zohoId), ACCOUNTS_MODULE_API_NAME, null, null);
      Optional<Record> res = getZohoRecords(response).stream().findFirst();
      if (res.isEmpty() && LOGGER.isDebugEnabled()) {
        LOGGER.debug("Cannot retrieve aggregator by zoho record id: {}", zohoId);
      }
      return res;
    } catch (SDKException e) {
      throw convertToZohoException(e);
    }
  }

  public Optional<Record> getZohoAggregatorByRecordId(String zohoId) throws ZohoException {
    try {
      RecordOperations recordOperations = new RecordOperations();
      APIResponse<ResponseHandler> response =
          recordOperations.getRecord(Long.valueOf(zohoId), AGGREGATORS_API_MODULE_NAME, null, null);
      Optional<Record> res = getZohoRecords(response).stream().findFirst();
      if (res.isEmpty() && LOGGER.isDebugEnabled()) {
        LOGGER.debug("Cannot retrieve aggregator by zoho record id: {}", zohoId);
      }
      return res;
    } catch (SDKException e) {
      throw convertToZohoException(e);
    }
  }

  ZohoException convertToZohoException(SDKException e) {
    return new ZohoException("Zoho search organization by organization id threw an exception", e);
  }

  /**
   * Search orgnaizations in Zoho by name
   * 
   * @param orgName organization name
   * @return zoho record as optional
   * @throws ZohoException wrapping the original SDK exception
   */
  public Optional<Record> searchZohoOrganizationByName(@NonNull String orgName)
      throws ZohoException {
    try {
      RecordOperations recordOperations = new RecordOperations();
      ParameterMap paramInstance = new ParameterMap();
      paramInstance.add(SearchRecordsParam.CRITERIA, String.format(ZOHO_OPERATION_FORMAT_STRING,
          ACCOUNT_NAME_FIELD, EQUALS_OPERATION, orgName));

      APIResponse<ResponseHandler> response =
          recordOperations.searchRecords(ACCOUNTS_MODULE_API_NAME, paramInstance);
      List<Record> records = getZohoRecords(response);
      for (Record rec : records) {
        /*
         * since the equals operator in zoho behaves like contains
         * (https://www.zoho.com/crm/developer/docs/api/v7/search-records.html), we need to check
         * the exact name
         */
        String accountName = ZohoOrganizationConverter.getStringFieldValue(rec, ACCOUNT_NAME_FIELD);
        if (orgName.equals(accountName)) {
          return Optional.of(rec);
        }
      }
    } catch (SDKException e) {
      throw convertToZohoException(e);
    }
    return Optional.empty();
  }

  /**
   * Zoho records can have additional information in the related records like e.g. products, notes,
   * attachments, aggregators, etc. In this case we use this method to get the aggregator
   * information.
   * 
   * @param zohoUrl the organization's URL in Zoho
   * @return zoho records as optional
   * @throws ZohoException wrapping the original SDK exception
   */
  public Optional<Record> getZohoAggregatorByOrgUrl(String zohoUrl) throws ZohoException {
    String zohoId = EntityRecordUtils.getIdentifierFromUrl(zohoUrl);
    try {
      // Get instance of RelatedRecordsOperations class that takes relatedListAPIName moduleAPIName
      // as parameter
      RelatedRecordsOperations relatedRecordsOperations = new RelatedRecordsOperations(
          RELATED_RECORDS_MODULE_API_NAME, Long.valueOf(zohoId), ACCOUNTS_MODULE_API_NAME);
      APIResponse<com.zoho.crm.api.relatedrecords.ResponseHandler> response =
          relatedRecordsOperations.getRelatedRecords(null, null);

      Optional<Record> res = getZohoRecords(response).stream().findFirst();
      if (res.isEmpty() && LOGGER.isDebugEnabled()) {
        LOGGER.debug("No aggregator found for zoho organization with id: {}", zohoId);
      }
      return res;
    } catch (SDKException e) {
      throw new ZohoException("Zoho get related records by organization id threw an exception", e);
    }
  }

  // /**
  // * Zoho records can have additional information in the related records like e.g. products,
  // notes,
  // * attachments, aggregators, etc. In this case we use this method to get the aggregator
  // * information.
  // *
  // * @param zohoUrl the organization's URL in Zoho
  // * @return zoho records as optional
  // * @throws ZohoException wrapping the original SDK exception
  // */
  // public Optional<Record> getZohoAggregatorId(String aggregatorRecordId) throws ZohoException {
  // try {
  // // Get instance of RelatedRecordsOperations class that takes relatedListAPIName moduleAPIName
  // // as parameter
  // RelatedRecordsOperations relatedRecordsOperations =
  // new RelatedRecordsOperations(RELATED_RECORDS_MODULE_API_NAME,
  // Long.valueOf(zohoId), ACCOUNTS_MODULE_API_NAME);
  // APIResponse<com.zoho.crm.api.relatedrecords.ResponseHandler> response =
  // relatedRecordsOperations.getRelatedRecords(null, null);
  //
  // return getZohoRecords(response).stream().findFirst();
  // } catch (SDKException e) {
  // throw new ZohoException("Zoho get related records by organization id threw an exception", e);
  // }
  // }

  /**
   * Get the Linking record from the module for the aggregated_via/from (name: LinkingModule1).
   * 
   * @param orgName the name of the organization
   * @return zoho aggregators subrecords from AggregatedVia module
   * @throws ZohoException if zoho access fails
   */
  public List<Record> searchZohoAggregatedViaModule(@NonNull String orgName) throws ZohoException {
    try {
      RecordOperations recordOperations = new RecordOperations();
      ParameterMap paramInstance = new ParameterMap();
      String criteria =
          String.format(ZOHO_OPERATION_FORMAT_STRING, AGGREGATING_FROM, EQUALS_OPERATION, orgName);
      paramInstance.add(SearchRecordsParam.CRITERIA, criteria);

      APIResponse<ResponseHandler> response =
          recordOperations.searchRecords(AGGREGATED_VIA_FROM_MODULE_API_NAME, paramInstance);

      List<Record> records = getZohoRecords(response);
      List<Record> res = new ArrayList<>(records.size());

      /*
       * since the equals operator in zoho behaves like contains
       * (https://www.zoho.com/crm/developer/docs/api/v7/search-records.html), we need to check the
       * exact values
       */
      for (Record rec : records) {
        Record aggregatingFrom = ZohoOrganizationConverter.getSubRecord(rec, AGGREGATING_FROM);
        if (aggregatingFrom == null) {
          continue;
        }
        String aggregatingFromName =
            ZohoOrganizationConverter.getStringFieldValue(aggregatingFrom, NAME_FIELD);
        if (orgName.equals(aggregatingFromName)) {
          res.add(ZohoOrganizationConverter.getSubRecord(rec, AGGREGATORS));
        }
      }
      return res;
    } catch (SDKException e) {
      throw convertToZohoException(e);
    }
  }

  /**
   * Method for updating one field in zoho
   * 
   * @param zohoUrl the URL of the organization in Zoho
   * @param fieldName the name of the field to update
   * @param fieldValue the new value
   * @throws ZohoException wrapping the original SDK exception
   */
  public boolean updateZohoRecordOrganizationStringField(String zohoUrl, String fieldName,
      String fieldValue) throws ZohoException {

    String zohoId = EntityRecordUtils.getIdentifierFromUrl(zohoUrl);
    try {
      RecordOperations recordOperations = new RecordOperations();
      BodyWrapper request = buildUpdateRequest(fieldName, fieldValue);

      // Call updateRecord method that takes recordId, ModuleAPIName and BodyWrapper instance as
      // parameter.
      APIResponse<ActionHandler> response =
          recordOperations.updateRecord(Long.valueOf(zohoId), ACCOUNTS_MODULE_API_NAME, request);
      // check if the update was successful
      validateZohoUpdateResponse(response);
    } catch (SDKException e) {
      throw new ZohoException("Zoho update the organization field threw an exception.", e);
    }
    return true;
  }

  BodyWrapper buildUpdateRequest(String fieldName, String fieldValue) {
    BodyWrapper request = new BodyWrapper();
    List<Record> records = new ArrayList<Record>();
    Record record1 = new Record();
    record1.addKeyValue(fieldName, fieldValue);
    records.add(record1);
    request.setData(records);
    return request;
  }

  /**
   * Source: https://www.zoho.com/crm/developer/docs/java-sdk/v2/record-samples.html
   * 
   * @param response zoho response
   * @throws ZohoException wrapping SDK exception
   */
  private void validateZohoUpdateResponse(APIResponse<ActionHandler> response)
      throws ZohoException {
    if (response == null || !response.isExpected()) {
      // response is expected, if empty the update operation is not confirmed
      throw new ZohoException("Unexpected response during updating a field in Zoho."
          + response.getStatusCode() + response.getObject());
    } else {
      // Get object from response
      ActionHandler actionHandler = response.getObject();
      if (actionHandler instanceof APIException) {
        // Convert api errors to ZohoExceptions Check if the request returned an exception
        throw new ZohoException(extractErrorMessage((APIException) actionHandler));
      } else if (actionHandler instanceof ActionWrapper) {
        verifyZohoConfirmationResponse(actionHandler);
      }
    }
  }

  void verifyZohoConfirmationResponse(ActionHandler actionHandler) throws ZohoException {
    // Get the received ResponseWrapper instance
    ActionWrapper actionWrapper = (ActionWrapper) actionHandler;
    // Get the list of obtained ActionResponse instances
    List<ActionResponse> actionResponses = actionWrapper.getData();
    for (ActionResponse actionResponse : actionResponses) {
      // Check if the request is successful
      if (actionResponse instanceof SuccessResponse) {
        // Get the received SuccessResponse instance
        // SuccessResponse successResponse = (SuccessResponse)actionResponse;
        // status, code, and message can be taken with: successResponse.getStatus().getValue(),
        // successResponse.getCode().getValue(), and successResponse.getMessage().getValue()
        continue;
      }
      // Check if the request returned an exception
      else if (actionResponse instanceof APIException) {
        // Get the received APIException instance
        String message = extractErrorMessage((APIException) actionResponse);
        throw new ZohoException(message);
      } else {
        //
        throw new ZohoException(
            "Cannot process Zoho API Response, unknown response type: " + actionResponse);
      }

    }
  }

  String extractErrorMessage(APIException errorResponse) {
    String message = "Exeption during updating a field in Zoho. Status: "
        + errorResponse.getStatus().getValue() + ", code: " + errorResponse.getCode().getValue()
        + ", message: " + errorResponse.getMessage().getValue();
    return message;
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
  public List<Record> getZcrmRecordOrganizations(int page, int pageSize,
      OffsetDateTime modifiedDate) throws ZohoException {

    if (page < 1 || pageSize < 1) {
      throw new ZohoException("Invalid page or pageSize index. Index must be >= 1",
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
          recordOperations.getRecords(ACCOUNTS_MODULE_API_NAME, paramInstance, headerInstance);

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
   * @param criteriaOperator the criteriaOperator used for each parameter, can be one of
   *        {@link ZohoConstants#EQUALS_OPERATION},{@link ZohoConstants#STARTS_WITH_OPERATION}. If
   *        not provided or wrong value, it will default to {@link ZohoConstants#EQUALS_OPERATION}.
   * @return the created criteria in the format Zoho accepts
   */
  String createZohoCriteriaString(Map<String, String> searchCriteria, String criteriaOperator) {
    if (isNullOrEmpty(searchCriteria)) {
      searchCriteria = new HashMap<>();
    }

    if (Objects.isNull(criteriaOperator) || (!EQUALS_OPERATION.equals(criteriaOperator)
        && !STARTS_WITH_OPERATION.equals(criteriaOperator))) {
      criteriaOperator = EQUALS_OPERATION;
    }

    String finalCriteriaOperator = criteriaOperator;
    return searchCriteria.entrySet().stream()
        .map(entry -> Arrays.stream(entry.getValue().split(DELIMITER_COMMA))
            .map(value -> String.format(ZOHO_OPERATION_FORMAT_STRING, entry.getKey(),
                finalCriteriaOperator, value.trim()))
            .collect(Collectors.joining(OR)))
        .collect(Collectors.joining(OR));
  }

  /**
   * Get deleted organization items paged.
   *
   * @param modifiedSince
   * @param startPage The number of the item from which the paging should start. First item is at
   *        number 1. Uses default number of items per page.
   * @return the list of deleted Zoho Organizations
   * @throws ZohoException if an error occurred during accessing Zoho
   */
  public List<DeletedRecord> getZohoDeletedRecordOrganizations(OffsetDateTime modifiedSince,
      int startPage, int pageSize) throws ZohoException {
    if (startPage < 1) {
      throw new ZohoException("Invalid start page index. Index must be >= 1",
          new IllegalArgumentException("start page: " + startPage));
    }
    try {
      RecordOperations recordOperations = new RecordOperations();
      ParameterMap paramInstance = new ParameterMap();
      paramInstance.add(GetDeletedRecordsParam.TYPE, "all"); // all, recycle, permanent
      paramInstance.add(GetDeletedRecordsParam.PAGE, 1);
      paramInstance.add(GetDeletedRecordsParam.PER_PAGE, pageSize);
      Param<String> scopeParam =
          new Param<String>("scope", "com.zoho.crm.api.Record.GetDeletedRecordsParam");
      paramInstance.add(scopeParam, "ZohoCRM.modules.ALL");

      HeaderMap headersMap = new HeaderMap();
      if (modifiedSince != null) {
        headersMap.add(GetRecordsHeader.IF_MODIFIED_SINCE, modifiedSince);
      }
      APIResponse<DeletedRecordsHandler> response =
          recordOperations.getDeletedRecords(ACCOUNTS_MODULE_API_NAME, paramInstance, headersMap);
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
   * Extract records form Zoho API Response
   * 
   * @param <T> the type of the record in API response
   * @param response the zoho api response
   * @return the list of extracted records
   * @throws ZohoException wrapping SDK exception
   */
  private static <T> List<Record> getZohoRecords(APIResponse<T> response) throws ZohoException {
    if (response == null) {
      return Collections.emptyList();
    }
    final int FIRST_ERROR_CODE = 400;
    if (response.getStatusCode() >= FIRST_ERROR_CODE) {
      // handle error responses
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Zoho Error. Response Status: {}, response Headers:{}",
            response.getStatusCode(), response.getHeaders());
      }
      throw new ZohoException("Zoho access error. Response code: " + response.getStatusCode());
    }

    if (response.isExpected()) {
      // Get the object from response
      T responseHandler = response.getObject();
      if (responseHandler instanceof com.zoho.crm.api.relatedrecords.ResponseWrapper) {
        com.zoho.crm.api.relatedrecords.ResponseWrapper responseWrapper =
            (com.zoho.crm.api.relatedrecords.ResponseWrapper) responseHandler;
        return responseWrapper.getData();
      } else if (responseHandler instanceof com.zoho.crm.api.record.ResponseWrapper) {
        com.zoho.crm.api.record.ResponseWrapper responseWrapper =
            (com.zoho.crm.api.record.ResponseWrapper) responseHandler;
        return responseWrapper.getData();
      }
    }
    return Collections.emptyList();
  }

}
