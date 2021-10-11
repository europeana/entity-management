package eu.europeana.entitymanagement.utils;

import org.apache.commons.lang3.StringUtils;

import eu.europeana.entitymanagement.vocabulary.WebEntityFields;

public class EntityRecordUtils {

	private EntityRecordUtils() {
		// private constructor to prevent instantiation
	}

	public static String buildEntityIdUri(String type, String identifier) {
	StringBuilder stringBuilder = new StringBuilder();

	stringBuilder.append(WebEntityFields.BASE_DATA_EUROPEANA_URI);
	if (StringUtils.isNotEmpty(type))
	    stringBuilder.append(type.toLowerCase()).append("/");
	if (StringUtils.isNotEmpty(identifier))
	    stringBuilder.append(identifier);

	return stringBuilder.toString();
    }

    public static String extractIdentifierFromEntityId(String entityId) {
	return entityId.replace(WebEntityFields.BASE_DATA_EUROPEANA_URI, "");
    }

	public static String getEuropeanaAggregationId(String entityId) {
		return entityId + "#aggr_europeana";
	}

	public static String getDatasourceAggregationId(String entityId) {
		return entityId + "#aggr_source_1";
	}

	public static String getIsAggregatedById(String entityId) {
		return entityId + "#aggregation";
	}

	public static String getEuropeanaProxyId(String entityId) {
		return entityId + "#proxy_europeana";
	}
	
	public static String getIdFromUrl (String url) {
	    if(!url.contains("/")) {
	        return url;
	    }else {
	        String[] uriParts = url.split("/");
	        return uriParts[uriParts.length -1];     
	    }
	}
}
