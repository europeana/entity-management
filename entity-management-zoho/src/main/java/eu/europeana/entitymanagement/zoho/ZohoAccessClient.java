package eu.europeana.entitymanagement.zoho;

import static eu.europeana.entitymanagement.zoho.utils.ZohoUtils.getZohoRecords;

import com.zoho.api.authenticator.OAuthToken;
import com.zoho.api.authenticator.OAuthToken.TokenType;
import com.zoho.api.authenticator.Token;
import com.zoho.api.authenticator.store.TokenStore;
import com.zoho.crm.api.Initializer;
import com.zoho.crm.api.ParameterMap;
import com.zoho.crm.api.SDKConfig;
import com.zoho.crm.api.UserSignature;
import com.zoho.crm.api.dc.DataCenter.Environment;
import com.zoho.crm.api.dc.USDataCenter;
import com.zoho.crm.api.exception.SDKException;
import com.zoho.crm.api.record.Record;
import com.zoho.crm.api.record.RecordOperations;
import com.zoho.crm.api.record.RecordOperations.SearchRecordsParam;
import com.zoho.crm.api.record.ResponseHandler;
import com.zoho.crm.api.util.APIResponse;
import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import eu.europeana.entitymanagement.zoho.utils.ZohoConstants;
import eu.europeana.entitymanagement.zoho.utils.ZohoException;
import java.util.Optional;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
   * @param refreshToken the zoho initial grant token
   * @param redirectUrl the registered zoho redirect url
   */
  public ZohoAccessClient(
      TokenStore tokenStore,
      String zohoEmail,
      String clientId,
      String clientSecret,
      String refreshToken,
      String redirectUrl) {
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
      LOGGER.warn("Exception during initialize", e);
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
}
