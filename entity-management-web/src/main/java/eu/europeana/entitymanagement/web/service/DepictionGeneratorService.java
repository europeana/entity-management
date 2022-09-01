package eu.europeana.entitymanagement.web.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.model.WebResource;

@Service
public class DepictionGeneratorService {

  private final WebClient webClient;
  EntityManagementConfiguration configuration;

  public DepictionGeneratorService(EntityManagementConfiguration configuration) {
    this.configuration = configuration;
    // searchApiUriPrefix = configuration.getSearchApiUrlPrefix();
    webClient = WebClient.builder().build();
  }


  public WebResource generateIsShownBy(String entityUri) throws EuropeanaApiException {
    String uri = buildSearchRequestUrl(entityUri);

    String response = null;
    try {
      response = webClient.get().uri(uri).accept(MediaType.APPLICATION_JSON).retrieve()
          .bodyToMono(String.class).block();
    } catch (Exception e) {
      throw new EuropeanaApiException(
          "Unable to get the valid response from the Search and Record API.", e);
    }
    if (response == null)
      return null;

    JSONObject responseJson = new JSONObject(response);
    String edmIsShownBy = null;
    String itemId = null;
    String edmPreview = null;

    if (!responseJson.has("items")) {
      return null;
    }


    JSONArray itemsList = responseJson.getJSONArray("items");
    if (itemsList.length() > 0) {
      JSONObject item = (JSONObject) itemsList.get(0);

      edmIsShownBy = getIsShownBy(item);
      if (item.has("id")) {
        itemId = item.getString("id");
      }
      edmPreview = getEdmPreview(item);
    }

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


  String buildSearchRequestUrl(String entityUri) {
    StringBuilder url = new StringBuilder(configuration.getSearchApiUrlPrefix());
    url.append("query=\"").append(entityUri).append("\" AND provider_aggregation_edm_isShownBy:*")
        .append("&sort=contentTier+desc,metadataTier+desc").append("&profile=minimal")
        // only first result is needed
        .append("&rows=1");
    return url.toString();
  }

}
