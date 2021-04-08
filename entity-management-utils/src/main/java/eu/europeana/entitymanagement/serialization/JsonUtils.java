package eu.europeana.entitymanagement.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import java.util.Iterator;

public class JsonUtils {
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
	 * Merges the fields of an EntityProxy and its Entity metadata. Expects all field names to be unique
	 * @return
	 * */

	public static JsonNode mergeProxyAndEntity(ObjectMapper mapper, ObjectNode proxyNode, ObjectNode entityNode) {
		ObjectNode result = mapper.createObjectNode();
		// manually add fields based on order in Spec
		result.set(WebEntityFields.ID, proxyNode.get(WebEntityFields.ID));
		result.set(WebEntityFields.TYPE, proxyNode.get(WebEntityFields.TYPE));

		result.setAll(entityNode);

		result.set(WebEntityFields.PROXY_FOR, proxyNode.get(WebEntityFields.PROXY_FOR));
		result.set(WebEntityFields.PROXY_IN, proxyNode.get(WebEntityFields.PROXY_IN));
		return result;
	}

}
