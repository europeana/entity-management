package eu.europeana.entitymanagement.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;

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
	public static List<Field> getAllFields(Class<?> type) {
		List<Field> entityFields = new ArrayList<Field>();
		getAllFieldsRecursively(entityFields, type);
	    return entityFields;
	} 
	
	private static void getAllFieldsRecursively (List<Field> fields, Class<?> type) {
		fields.addAll(Arrays.asList(type.getDeclaredFields()));
	    if (type.getSuperclass() != null) {
	    	getAllFieldsRecursively(fields, type.getSuperclass());
	    }	    
	}
	
	public static boolean isUri (String value) {
		if(value.startsWith("http://") || value.startsWith("https://")) {
			return true;
		}
		else {
			return false;
		}
	}

}
