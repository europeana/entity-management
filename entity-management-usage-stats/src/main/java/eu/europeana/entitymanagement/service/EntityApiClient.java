package eu.europeana.entitymanagement.service;

import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.entitymanagement.exception.EntityApiAccessException;
import eu.europeana.entitymanagement.model.EntityApiResponse;
import eu.europeana.entitymanagement.model.EntityCountType;
import eu.europeana.entitymanagement.vocabulary.UsageStatsFields;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.util.*;

/**
 * Entity Api Client Service
 *
 * @author Srishti Singh (srishti.singh@europeana.eu)
 * @since 2021-07-29
 */
public class EntityApiClient {

    /**
     * returns the Entity Api response
     *
     * @param lang
     * @param apikey
     * @return
     * @throws EntityApiAccessException
     */
    public EntityApiResponse getEntityApiResponse(String lang, String apikey) throws EntityApiAccessException {
    JSONObject jsonObject = getEntityFromURL(buildEntityUrl(lang, apikey));
    if (jsonObject != null) {
        List<EntityCountType> countPerType = extractValues(jsonObject);
        if (!countPerType.isEmpty()) {
            return new EntityApiResponse(lang, countPerType);
        }
    }
    return null;
    }

    /**
     * Extracts the values of count and label
     *
     * @param jsonObject
     * @return
     * @throws EntityApiAccessException
     */
    private List<EntityCountType> extractValues(JSONObject jsonObject) throws EntityApiAccessException {
    List<EntityCountType> countPerLabel = new ArrayList<>();
    try {
        JSONObject facets = (JSONObject) jsonObject.get("facets");
        JSONArray valuesArray = facets.getJSONArray("values");
        for(int i =0; i<valuesArray.length(); i++) {
            JSONObject jo = (JSONObject) valuesArray.get(i);
            countPerLabel.add(new EntityCountType(jo.getString("count"), jo.getString("label")));
        }
    } catch (JSONException e) {
        throw new EntityApiAccessException("Cannot parse entity API response." + e.getMessage());
    }
    return countPerLabel;
    }

    /**
     * Method to get the search response from entity api
     * GET : <https://api.europeana.eu/entity/search.json?...>
     *
     * @param urlToRead
     * @return
     * @throws EntityApiAccessException
     */
    public JSONObject getEntityFromURL(String urlToRead) throws EntityApiAccessException {
    try (CloseableHttpClient httpClient = HttpClients.createDefault()){
        HttpGet request = new HttpGet(urlToRead);
        CloseableHttpResponse response = httpClient.execute(request);
        try {
            if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 304) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    return new JSONObject(EntityUtils.toString(entity));
                }
            }
        } catch (JSONException e) {
            throw new EntityApiAccessException("Cannot parse entity API response." + e.getMessage());
        } finally {
            response.close();
        }
    } catch (IOException e) {
        throw new EntityApiAccessException("Error executing the request for uri "+urlToRead);
    }
    return null;
    }

    /**
     * Returns the entity api search url for the provided language
     * example : https://api.europeana.eu/entity/search.json?query=skos_prefLabel.fr:*&profile=facets&facet=type&pageSize=0&wskey=
     *
     * @param lang
     * @param apiKey
     * @return
     */
    private String buildEntityUrl(String lang, String apiKey) {
    StringBuilder url = new StringBuilder(UsageStatsFields.ENTITY_API_BASE_URL);
    url.append("?").append(UsageStatsFields.ENTITY_API_SEARCH_QUERY).append(lang);
    url.append(UsageStatsFields.ENTITY_API_SEARCH_PFPS);
    url.append('&').append(CommonApiConstants.PARAM_WSKEY).append('=').append(apiKey);
    return url.toString();
    }
}
