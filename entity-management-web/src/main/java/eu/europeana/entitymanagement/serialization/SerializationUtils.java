package eu.europeana.entitymanagement.serialization;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.europeana.entitymanagement.definitions.exceptions.EntityManagementRuntimeException;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.schemaorg.model.SchemaOrgEntity;
import eu.europeana.entitymanagement.utils.EntityObjectFactory;
import eu.europeana.entitymanagement.vocabulary.FormatTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;

public class SerializationUtils {

  public static void serializeInternalJson(Writer writer, ObjectMapper mapper, EntityRecord record, FormatTypes format)
      throws IOException {
    JsonNode result = getInternalJsonNode(mapper, record, format);
    mapper.writeValue(writer, result);
  }

  public static void serializeExternalJson(Writer writer, ObjectMapper mapper, EntityRecord record, FormatTypes format)
      throws IOException {
	  mapper.writeValue(writer, getExternalJsonNode(mapper, record, format));
  }

  private static ObjectNode getExternalJsonNode(ObjectMapper mapper, EntityRecord record, FormatTypes format)
      throws EntityManagementRuntimeException {
	  if (format.equals(FormatTypes.schema)) {
          SchemaOrgEntity<?> schemaOrgEntity = EntityObjectFactory.createSchemaOrgEntity(record.getEntity());
          return mapper.valueToTree(schemaOrgEntity.get());
	  }
	  else {
		  return mapper.valueToTree(record.getEntity());
	  }
  }

  private static JsonNode getInternalJsonNode(ObjectMapper mapper, EntityRecord record, FormatTypes format){
	ObjectNode entityNode = null;
      if (format.equals(FormatTypes.schema)) {
          SchemaOrgEntity<?> schemaOrgEntity = EntityObjectFactory.createSchemaOrgEntity(record.getEntity());
          entityNode = mapper.valueToTree(schemaOrgEntity.get());
      } else {
          entityNode = mapper.valueToTree(record.getEntity());
      }

      List<EntityProxy> recordProxies = record.getProxies();

    ArrayNode proxyNode = mapper.createArrayNode();

    for(EntityProxy proxy: recordProxies){
    	ObjectNode proxyEntityNode=null;    	
    	if(format.equals(FormatTypes.schema)) {  		
            SchemaOrgEntity<?> schemaOrgProxyEntity = EntityObjectFactory.createSchemaOrgEntity(proxy.getEntity());
            proxyEntityNode = mapper.valueToTree(schemaOrgProxyEntity.get());
	        // Entity @context shouldn't appear in proxy metadata
	        proxyEntityNode.remove(WebEntityFields.CONTEXT);
	        // Entity ID shouldn't overwrite proxyId
	        proxyEntityNode.remove(WebEntityFields.ID_SCHEMA);

    	}
    	else {
    		proxyEntityNode = mapper.valueToTree(proxy.getEntity());	
	        // Entity @context shouldn't appear in proxy metadata
	        proxyEntityNode.remove(WebEntityFields.CONTEXT);
	        // Entity ID shouldn't overwrite proxyId
	        proxyEntityNode.remove(WebEntityFields.ID);
    	}

        ObjectNode embeddedProxyNode = mapper.valueToTree(proxy);
        JsonNode mergedNode = SerializationUtils.mergeProxyAndEntity(mapper, embeddedProxyNode, proxyEntityNode);
        proxyNode.add(mergedNode.deepCopy());
    }

    return SerializationUtils
        .combineNestedNode(mapper, entityNode, proxyNode, "proxies");

  }

  /**
   * Merges the fields of an EntityProxy and its Entity metadata. Expects all field names to be unique
   * @return
   * */

  private static JsonNode mergeProxyAndEntity(ObjectMapper mapper, ObjectNode proxyNode, ObjectNode entityNode) {
    ObjectNode result = mapper.createObjectNode();
    // manually add fields based on order in Spec
    result.set(WebEntityFields.ID, proxyNode.get(WebEntityFields.ID));
    result.set(WebEntityFields.TYPE, proxyNode.get(WebEntityFields.TYPE));

    result.setAll(entityNode);

    result.set(WebEntityFields.PROXY_FOR, proxyNode.get(WebEntityFields.PROXY_FOR));
    result.set(WebEntityFields.PROXY_IN, proxyNode.get(WebEntityFields.PROXY_IN));
    return result;
  }

  /**
   * Combines two JsonNodes. The second node is nested within the first, with the specified field name.
   */
  private static JsonNode combineNestedNode(ObjectMapper mapper, ObjectNode parentNode, JsonNode nestedNode, String nestedNodeFieldName) {
    ObjectNode result = mapper.createObjectNode();
    result.setAll(parentNode);
    result.set(nestedNodeFieldName, nestedNode);
    return result;
  }

}
