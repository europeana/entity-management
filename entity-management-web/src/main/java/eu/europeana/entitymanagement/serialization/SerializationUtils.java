package eu.europeana.entitymanagement.serialization;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.xml.stream.XMLStreamWriter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import eu.europeana.entitymanagement.definitions.exceptions.EntityManagementRuntimeException;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;

public class SerializationUtils {




  public static void serializeInternalXml(XMLStreamWriter xmlWriter, XmlMapper xmlMapper, EntityRecord record)
      throws IOException {
	  JsonNode result = getInternalJsonNode(xmlMapper, record);
	  xmlMapper.writeValue(xmlWriter, result);
  }

  public static void serializeInternalJson(Writer writer, ObjectMapper mapper, EntityRecord record)
      throws IOException {
    JsonNode result = getInternalJsonNode(mapper, record);
    mapper.writeValue(writer, result);
  }

  public static void serializeExternalXml(XMLStreamWriter xmlWriter, XmlMapper xmlMapper, EntityRecord record)
      throws IOException {
    JsonNode result = getExternalJsonNode(xmlMapper, record);
    xmlMapper.writeValue(xmlWriter, result);
  }

  public static void serializeExternalJson(Writer writer, ObjectMapper mapper, EntityRecord record)
      throws IOException {
    ObjectNode result = getExternalJsonNode(mapper, record);
    // Entity isAggregatedBy should be included in external profile
    //result.remove(WebEntityFields.IS_AGGREGATED_BY);
    mapper.writeValue(writer, result);
  }


  private static ObjectNode getExternalJsonNode(ObjectMapper mapper, EntityRecord record)
      throws EntityManagementRuntimeException {
      return mapper.valueToTree(record.getEntity());
  }

  private static JsonNode getInternalJsonNode(ObjectMapper mapper, EntityRecord record){
    Entity recordEntity = record.getEntity();
    List<EntityProxy> recordProxies = record.getProxies();
    ObjectNode entityNode = mapper.valueToTree(recordEntity);

    ArrayNode proxyNode = mapper.createArrayNode();

    for(EntityProxy proxy: recordProxies){
      Entity proxyEntity = proxy.getEntity();
      ObjectNode proxyEntityNode = mapper.valueToTree(proxyEntity);

      // Entity @context shouldn't appear in proxy metadata
      proxyEntityNode.remove(WebEntityFields.CONTEXT);
      // Entity ID shouldn't overwrite proxyId
      proxyEntityNode.remove(WebEntityFields.ID);

      ObjectNode embeddedProxyNode = mapper.valueToTree(proxy);
      JsonNode mergedNode = SerializationUtils
          .mergeProxyAndEntity(mapper, embeddedProxyNode, proxyEntityNode);
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
