package eu.europeana.entitymanagement.utils;

import eu.europeana.entitymanagement.definitions.model.vocabulary.WebEntityConstants;

public class EntityUtils {

	public static String createWikimediaResourceString(String wikimediaCommonsId) {
		assert wikimediaCommonsId.contains("Special:FilePath/");
		return wikimediaCommonsId.replace("Special:FilePath/", "File:");
	}
	
	public static String toGeoUri(String latLon){
		return WebEntityConstants.PROTOCOL_GEO + latLon;
	}
    
}
