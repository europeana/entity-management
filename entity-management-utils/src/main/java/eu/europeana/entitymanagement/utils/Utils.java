package eu.europeana.entitymanagement.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Iterator;

public class Utils {

	public static boolean isUri (String value) {
		if(value.startsWith("http://") || value.startsWith("https://")) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Combines two JsonNodes. The second node is nested within the first, with the specified field name.
	 */
	public static JsonNode combineNestedNode(ObjectMapper mapper, ObjectNode parentNode, JsonNode nestedNode, String nestedNodeFieldName) {
		ObjectNode result = mapper.createObjectNode();
		result.setAll(parentNode);
		result.set(nestedNodeFieldName, nestedNode);
		return result;
	}

	/**
	 * Merges two JsonNodes. Expects all field names to be unique
	 * @return
	 * */

	public static JsonNode mergeNode(ObjectMapper mapper, ObjectNode firstNode, ObjectNode secondNode) {
		ObjectNode result = mapper.createObjectNode();
		result.setAll(firstNode);

		Iterator<String> secondNodeFieldNames = secondNode.fieldNames();
		while (secondNodeFieldNames.hasNext()) {
			String key = secondNodeFieldNames.next();
			JsonNode value = secondNode.get(key);

			// overwrites any matching keys in firstNode
			result.set(key, value);
			}


		return result;
	}

}
