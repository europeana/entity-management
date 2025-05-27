package eu.europeana.entitymanagement.zoho.organization;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.definitions.model.Address;

@Service
public class RapidapiGeocodingClient {
  private static final Logger logger = LogManager.getLogger(RapidapiGeocodingClient.class);

  private String baseUrl="https://forward-reverse-geocoding.p.rapidapi.com/v1/forward"; 
  private String formatResults="json";
  private String acceptLanguage="en";
  private String limitNumResults="1";
  private String rapidApiHost="forward-reverse-geocoding.p.rapidapi.com";
  private String rapidApiKey="3ed04b4a52msh8f13bc729419234p1a3dc3jsndde2203fd7d0";
  
  public RapidapiGeocodingClient() {
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
    String uri = baseUrl;
    uri += "?";
    uri += "format=" + formatResults;
    try {
      if(!StringUtils.isBlank(address.getVcardStreetAddress())) {
        uri += "&street=" + URLEncoder.encode(address.getVcardStreetAddress(), "UTF-8");
      }
      if(!StringUtils.isBlank(address.getVcardPostalCode())) {
        uri += "&postalcode=" + URLEncoder.encode(address.getVcardPostalCode(), "UTF-8");
      }
      if(!StringUtils.isBlank(address.getVcardCountryName())) {
        uri += "&country=" + URLEncoder.encode(address.getVcardCountryName(), "UTF-8");
      }
    } catch (UnsupportedEncodingException e) {
      logger.error("Error during character encoding in the uri: {}, for the rapidapi geocoding service.", uri);
      throw new EuropeanaApiException("Error during character encoding in the uri for the rapidapi geocoding service: " 
          + uri, e);
    }
    
    uri += "&accept-language=" + acceptLanguage;
    uri += "&limit=" + limitNumResults;

    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      HttpGet request = new HttpGet(uri);
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
      logger.error("Error during executing the request to the geocoding rapidapi: {}", uri);
      throw new EuropeanaApiException("Error executing the request to the rapidapi geocoding service "
          + "for the uri: " + uri, e);
    }
    return null;
  }

}
