package eu.europeana.entitymanagement.common.geocoding;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.net.URIBuilder;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.http.MediaType;
import eu.europeana.api.commons.http.HttpConnection;
import eu.europeana.api.commons.http.HttpResponseHandler;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.common.exception.HttpClientException;
import eu.europeana.entitymanagement.definitions.model.Address;

public class RapidApiGeocodingClient {

  private String baseUrl = "https://forward-reverse-geocoding.p.rapidapi.com/v1/forward";
  private String formatResults = "json";
  private String acceptLanguage = "en";
  private String limitNumResults = "1";
  private String rapidApiKey;
  private HttpConnection httpConnection;

  public RapidApiGeocodingClient(String baseUrl, String rapidApiKey) {
    if(StringUtils.isNotEmpty(baseUrl)) {
      this.baseUrl = baseUrl;
    }
    this.rapidApiKey = rapidApiKey;
    this.httpConnection = new HttpConnection(true);
  }

  public RapidApiGeocodingClient(EntityManagementConfiguration config) {
    this(config.getRapidApiBaseUrl(), config.getRapidApiKey());
  }

  public String getGeoURI(Address address) throws HttpClientException {
    String rapidApiRespJson = getRapidApiResponse(address);
    if (StringUtils.isEmpty(rapidApiRespJson)) {
      return null;
    }
    try {
      JSONArray rapidApiRespArray = new JSONArray(rapidApiRespJson);
      JSONObject rapidApiRespObj = (JSONObject) rapidApiRespArray.get(0);
      return "geo:" + rapidApiRespObj.getString("lat") + "," + rapidApiRespObj.getString("lon");
    } catch (JSONException ex) {
      throw new HttpClientException(
          "Error during processing json from the rapidapi geocoding response.", ex);
    }
  }

  private String getRapidApiResponse(Address address) throws HttpClientException {
    // according to the rapidapi docs, at least one of: street, city, postcode, county or country is
    // required
    if (StringUtils.isBlank(address.getVcardStreetAddress())
        && StringUtils.isBlank(address.getVcardPostalCode())
        && StringUtils.isBlank(address.getVcardCountryName())) {
      return null;
    }

    URIBuilder uriBuilder = null;
    try {
      uriBuilder = new URIBuilder(baseUrl);
      addRequestParams(address, uriBuilder);
      uriBuilder.build();
    } catch (URISyntaxException e) {
      throw new HttpClientException("Invalid geocoding rapidapi input uri.", e);
    }

    Map<String, String> headers =
        Map.of("x-rapidapi-host", uriBuilder.getHost(), 
            "x-rapidapi-key", rapidApiKey, 
            HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

    try {
      HttpResponseHandler responseHandler =
          getHttpConnection().get(uriBuilder.toString(), headers, null);

      if (responseHandler.getStatus() != 200) {
        throw new HttpClientException(
            "Cannot retrieve geocoding response. " + responseHandler.getResponse());
      }
      return responseHandler.getResponse();
    } catch (IOException e) {
      throw new HttpClientException("Error executing the request to the rapidapi geocoding service "
          + "for the uri: " + uriBuilder, e);
    }
  }

  void addRequestParams(Address address, URIBuilder uriBuilder) {
    uriBuilder.addParameter("format", formatResults);
    if (!StringUtils.isBlank(address.getVcardStreetAddress())) {
      uriBuilder.addParameter("street", address.getVcardStreetAddress());
    }
    if (!StringUtils.isBlank(address.getVcardPostalCode())) {
      uriBuilder.addParameter("postalcode", address.getVcardPostalCode());
    }
    if (!StringUtils.isBlank(address.getVcardCountryName())) {
      uriBuilder.addParameter("country", address.getVcardCountryName());
    }
    uriBuilder.addParameter("accept-language", acceptLanguage);
    uriBuilder.addParameter("limit", limitNumResults);
  }

  public HttpConnection getHttpConnection() {
    return httpConnection;
  }

}
