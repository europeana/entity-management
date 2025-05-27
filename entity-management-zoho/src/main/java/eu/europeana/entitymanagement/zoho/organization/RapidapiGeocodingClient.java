package eu.europeana.entitymanagement.zoho.organization;

import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.model.Address;

@Service
public class RapidapiGeocodingClient {
  private static final Logger logger = LogManager.getLogger(RapidapiGeocodingClient.class);

  private String baseUrl="https://forward-reverse-geocoding.p.rapidapi.com/v1/forward"; 
  private String formatResults="json";
  private String acceptLanguage="en";
  private String limitNumResults="1";
  private String rapidApiHost="forward-reverse-geocoding.p.rapidapi.com";
  private String rapidApiKey;
  
  @Autowired
  public RapidapiGeocodingClient(EntityManagementConfiguration config) {
    rapidApiKey=config.getRapidapiKey();
  }

  public String getGeoURI(Address address) throws EuropeanaApiException {
    String rapidApiRespJson=getRapidapiResponse(address);
    if(rapidApiRespJson==null) {
      return null;
    }
    try {
      JSONArray rapidApiRespArray = new JSONArray(rapidApiRespJson);
      JSONObject rapidApiRespObj = (JSONObject) rapidApiRespArray.get(0);
      return "geo:" + rapidApiRespObj.getString("lat") + "," + rapidApiRespObj.getString("lon");
    } catch (JSONException ex) {
      logger.error("Error during processing json from the rapidapi geocoding response: {}", ex.getMessage());
      throw new EuropeanaApiException("Error during processing json from the rapidapi geocoding response.", ex);
    }
  }

  private String getRapidapiResponse(Address address) throws EuropeanaApiException {
    //according to the rapidapi docs, at least one of: street, city, postcode, county or country is required
    if(StringUtils.isBlank(address.getVcardStreetAddress()) && StringUtils.isBlank(address.getVcardPostalCode())
        && StringUtils.isBlank(address.getVcardCountryName())) {
      return null;
    }
    
    URIBuilder uriBuilder = null;
    try {
      uriBuilder = new URIBuilder(baseUrl);
    } catch (URISyntaxException e) {
      logger.error("Invalid geocoding rapidapi input uri: {}", baseUrl);
      throw new EuropeanaApiException("Invalid geocoding rapidapi input uri: " + baseUrl, e);
    }
    uriBuilder.addParameter("format", formatResults);
    if(!StringUtils.isBlank(address.getVcardStreetAddress())) {
      uriBuilder.addParameter("street", address.getVcardStreetAddress());
    }
    if(!StringUtils.isBlank(address.getVcardPostalCode())) {
      uriBuilder.addParameter("postalcode", address.getVcardPostalCode());
    }
    if(!StringUtils.isBlank(address.getVcardCountryName())) {
      uriBuilder.addParameter("country", address.getVcardCountryName());
    }
    uriBuilder.addParameter("accept-language", acceptLanguage);
    uriBuilder.addParameter("limit", limitNumResults);
    
    String uriString = null;
    try {
      uriString=uriBuilder.build().toString();
    } catch (URISyntaxException e) {
      logger.error("Invalid geocoding rapidapi input uri.");
      throw new EuropeanaApiException("Invalid geocoding rapidapi input uri.", e);
    }

    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      HttpGet request = new HttpGet(uriString);
      request.addHeader("x-rapidapi-host", rapidApiHost);
      request.addHeader("x-rapidapi-key", rapidApiKey);
      try (CloseableHttpResponse response = httpClient.execute(request)) {
        if (response.getStatusLine().getStatusCode() != 200) {
          return null;
        }
        HttpEntity entity = response.getEntity();
        if (entity != null) {
          return EntityUtils.toString(entity);
        }
      }
    } catch (IOException e) {
      logger.error("Error during executing the request to the geocoding rapidapi: {}", uriString);
      throw new EuropeanaApiException("Error executing the request to the rapidapi geocoding service "
          + "for the uri: " + uriString, e);
    }
    return null;
  }

}
