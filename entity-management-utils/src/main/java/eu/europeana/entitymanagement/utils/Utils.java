package eu.europeana.entitymanagement.utils;

public class Utils {

	public static boolean isUri (String value) {
		if(value.startsWith("http://") || value.startsWith("https://")) {
			return true;
		}
		else {
			return false;
		}
	}
}
