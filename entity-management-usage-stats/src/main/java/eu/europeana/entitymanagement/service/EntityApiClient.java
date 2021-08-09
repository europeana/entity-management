package eu.europeana.entitymanagement.service;

import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.entitymanagement.exception.EntityApiAccessException;
import eu.europeana.entitymanagement.exception.UsageStatsException;
import eu.europeana.entitymanagement.model.EntitiesPerLanguage;
import eu.europeana.entitymanagement.model.Entity;
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

/**
 * Entity Api Client Service
 *
 * @author Srishti Singh (srishti.singh@europeana.eu)
 * @since 2021-07-29
 */
public class EntityApiClient {

    /**
     * Return the entity with total values per type
     * @param apikey
     * @return
     * @throws EntityApiAccessException
     */
    public Entity getEntity(String apikey) throws EntityApiAccessException {
    JSONObject jsonObject = getEntityFromURL(buildEntityApiUrl(null, apikey));
    return exrtractFacetValues(jsonObject);
    }
    /**
     * returns the Entity Api response
     *
     * @param lang
     * @param apikey
     * @return
     * @throws EntityApiAccessException
     */
    public EntitiesPerLanguage getEntitiesForLanguage(String lang, String apikey, Entity entityTotal) throws EntityApiAccessException, UsageStatsException {
    JSONObject jsonObject = getEntityFromURL(buildEntityApiUrl(lang, apikey));
    if (jsonObject != null) {
        EntitiesPerLanguage entities = getEntityPerLangValues(jsonObject, entityTotal);
        entities.setLang(lang);
        return entities;
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
    private EntitiesPerLanguage getEntityPerLangValues(JSONObject jsonObject, Entity entityTotal) throws EntityApiAccessException, UsageStatsException {
    EntitiesPerLanguage entityPerLanguage = new EntitiesPerLanguage();
    Entity entity = exrtractFacetValues(jsonObject);
    calculatePercentageValues(entity, entityTotal, entityPerLanguage);
    return entityPerLanguage;
    }

    private void calculatePercentageValues(Entity entityPerLanguage, Entity entityTotal, EntitiesPerLanguage entities) throws UsageStatsException {
    try {
        entities.setTimespans(getPercentage(entityPerLanguage.getTimespans(), entityTotal.getTimespans()));
        entities.setPlaces(getPercentage(entityPerLanguage.getPlaces(), entityTotal.getPlaces()));
        entities.setConcepts(getPercentage(entityPerLanguage.getConcepts(), entityTotal.getConcepts()));
        entities.setAgents(getPercentage(entityPerLanguage.getAgents(), entityTotal.getAgents()));
        entities.setOrganisations(getPercentage(entityPerLanguage.getOrganisations(), entityTotal.getOrganisations()));
        entities.setTotal(getPercentage(entityPerLanguage.getTotal(), entityTotal.getTotal()));

    } catch (Exception e) {
        throw new UsageStatsException("Error calculating the percentage values." +e.getMessage());
    }
    }

    private int getPercentage(int value, int total) {
    return Math.round(((float) value / (float) total) * 100);
    }

    /**
     * Extracts facet values from Entity api response
     *
     * @param jsonObject
     * @return
     * @throws EntityApiAccessException
     */
    private Entity exrtractFacetValues(JSONObject jsonObject) throws EntityApiAccessException {
    Entity entity = new Entity();
    try {
        JSONObject facets = (JSONObject) jsonObject.get("facets");
        JSONArray valuesArray = facets.getJSONArray("values");
        for(int i =0; i<valuesArray.length(); i++) {
            JSONObject jo = (JSONObject) valuesArray.get(i);
            String label = jo.getString("label");
            int count = Integer.parseInt(jo.getString("count"));
            switch(label) {
                case "Agent" :
                    entity.setAgents(count);
                    break;
                case "Concept" :
                    entity.setConcepts(count);
                    break;
                case "Timespan" :
                    entity.setTimespans(count);
                    break;
                case "Place" :
                    entity.setPlaces(count);
                    break;
                case "Organization" :
                    entity.setOrganisations(count);
                    break;
            }
        }
        entity.setTotal(getTotal(entity));
        return entity;
    } catch (JSONException e) {
        throw new EntityApiAccessException("Cannot parse entity API response." + e.getMessage());
    }
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
     * Returns the entity api search urls :
     * 1) lang parameter is present : queries on lang and facets on type
     *     https://api.europeana.eu/entity/search.json?query=skos_prefLabel.fr:*&profile=facets&facet=type&pageSize=0&wskey=
     * 2) if lang parameter is not present : queries all and facets on type
     *     https://api.europeana.eu/entity/search.json?query=*&profile=facets&facet=type&pageSize=0&wskey=api2demo
     *
     * @param lang
     * @param apiKey
     * @return
     */
    private static String buildEntityApiUrl(String lang, String apiKey) {
    StringBuilder url = new StringBuilder(UsageStatsFields.ENTITY_API_BASE_URL);
    // adds : query=skos_prefLabel.<lang>:*
    if (lang != null && !lang.isBlank()) {
        url.append("?").append(UsageStatsFields.ENTITY_API_PREFLABEL_SEARCH_QUERY).append(lang);
        url.append(UsageStatsFields.ENTITY_API_PREFLABEL_QUERY_SEPERATOR);
    } else { // adds : query=*
        url.append("?").append(UsageStatsFields.ENTITY_API_SEARCH_ALL_QUERY);
    }
    url.append(UsageStatsFields.ENTITY_API_SEARCH_PFPS);
    url.append('&').append(CommonApiConstants.PARAM_WSKEY).append('=').append(apiKey);
    return url.toString();
    }

    private int getTotal(Entity entity) {
        return (entity.getAgents() + entity.getConcepts() + entity.getOrganisations() + entity.getPlaces() +entity.getTimespans());
    }
}
