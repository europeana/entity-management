package eu.europeana.entitymanagement.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.api.commons.auth.AuthenticationHandler;
import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.api.commons.http.HttpConnection;
import eu.europeana.api.commons.http.HttpResponseHandler;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.model.WebResource;
import eu.europeana.entitymanagement.exception.ParamValidationException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.net.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URISyntaxException;

public class DepictionGeneratorService {

  @Resource
  EntityManagementConfiguration configuration;

  private final HttpConnection httpConnection;
  ObjectMapper mapper;
  AuthenticationHandler auth;

  /**
   * Constructor
   * @param auth authentication for accessing SR api
   */
  public DepictionGeneratorService(AuthenticationHandler auth) {
    this.auth = auth;
    httpConnection = new HttpConnection(true);
    mapper = new ObjectMapper();
  }

  public WebResource generateIsShownBy(String entityUri) throws EuropeanaApiException {
    String uri = buildSearchRequestUrl(entityUri);
    String response = null;
    try {
      HttpResponseHandler httpResponse = httpConnection.get(uri, "application/json", auth);
      if (httpResponse.getStatus() == HttpStatus.SC_OK) {
        response = httpResponse.getResponse();
      } else {
        throw new EuropeanaApiException(
                "Unable to get the valid response from the Search and Record API - "
                        +getErrorMessage(httpResponse.getStatus(), httpResponse.getResponse()));
      }
    } catch (IOException e) {
      throw new EuropeanaApiException(
          "Unable to get the valid response from the Search and Record API. - " +e.getMessage(), e);
    }
    if (response == null) return null;

    JSONObject responseJson = new JSONObject(response);
    String edmIsShownBy = null;
    String itemId = null;
    String edmPreview = null;

    if (!responseJson.has("items")) {
      return null;
    }

    JSONArray itemsList = responseJson.getJSONArray("items");
    if (itemsList.length() <= 0) {
      return null;
    }

    JSONObject item = (JSONObject) itemsList.get(0);
    edmIsShownBy = getIsShownBy(item);
    if (item.has("id")) {
      itemId = item.getString("id");
    }
    edmPreview = getEdmPreview(item);

    return new WebResource(edmIsShownBy, configuration.getItemDataEndpoint() + itemId, edmPreview);
  }

  String getEdmPreview(JSONObject item) {
    if (item.has("edmPreview")) {
      JSONArray edmPreviewList = item.getJSONArray("edmPreview");
      if (edmPreviewList.length() > 0) {
        return edmPreviewList.getString(0);
      }
    }
    return null;
  }

  String getIsShownBy(JSONObject item) {
    if (item.has("edmIsShownBy")) {
      JSONArray edmIsShownByList = item.getJSONArray("edmIsShownBy");
      if (edmIsShownByList.length() > 0) {
        return edmIsShownByList.getString(0);
      }
    }
    return null;
  }


  /**
   * Build the serach api retrieval url with entity id
   * @param entityUri id
   * @return URL
   * @throws ParamValidationException
   */
  private String buildSearchRequestUrl(String entityUri) throws ParamValidationException {
    try {
      return new URIBuilder(configuration.getSearchApiUrlPrefix())
              .addParameter("query",
                      "\"" +entityUri + "\" AND provider_aggregation_edm_isShownBy:*&sort=contentTier+desc,metadataTier+desc&profile=minimal&rows=1")
              .build().toString();
    } catch (URISyntaxException e) {
      throw new ParamValidationException("Error building the search request url - " + e.getMessage(), e);
    }
  }

  private String getErrorMessage(int responseCode, String json) throws EuropeanaApiException {
    try {
      JsonNode node = mapper.readTree(json);
      if (node.has("message")) {
        return node.get("message").asText();
      }
      return "Error retrieving record : " + responseCode;
    } catch (JsonProcessingException e) {
      throw new EuropeanaApiException(" Error parsing the record response: " + e.getMessage(), e);
    }
  }
}
