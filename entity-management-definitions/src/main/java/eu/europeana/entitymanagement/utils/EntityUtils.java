package eu.europeana.entitymanagement.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import eu.europeana.entitymanagement.definitions.model.vocabulary.WebEntityConstants;

public class EntityUtils {

	public static String createWikimediaResourceString(String wikimediaCommonsId) {
		assert wikimediaCommonsId.contains("Special:FilePath/");
		return wikimediaCommonsId.replace("Special:FilePath/", "File:");
	}
	
	public static String toGeoUri(String latLon){
		return WebEntityConstants.PROTOCOL_GEO + latLon;
	}
    
	/*
	 * getting all fields of the class including the ones from the parent classes using Java reflection
	 */
	public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
	    fields.addAll(Arrays.asList(type.getDeclaredFields()));

	    if (type.getSuperclass() != null) {
	        getAllFields(fields, type.getSuperclass());
	    }

	    return fields;
	}

}
