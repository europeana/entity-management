package eu.europeana.entitymanagement.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.europeana.entitymanagement.definitions.batch.model.FailedTask;
import eu.europeana.entitymanagement.definitions.exceptions.EntityManagementRuntimeException;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.vocabulary.FormatTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class SerializationUtils {

  private SerializationUtils() {
    // to hide implicit one
  }

  public static String serializeInternalJson(
      ObjectMapper mapper,
      EntityRecord record,
      boolean includeFailure,
      Optional<FailedTask> failure)
      throws IOException {

    final StringWriter buffer = new StringWriter();

    mapper.writeValue(buffer, getInternalJsonNode(mapper, record, includeFailure, failure));
    return buffer.toString();
  }

  public static String serializeExternalJson(
      ObjectMapper mapper,
      EntityRecord record,
      boolean includeFailure,
      Optional<FailedTask> failure)
      throws IOException {
    final StringWriter buffer = new StringWriter();
    mapper.writeValue(buffer, getExternalJsonNode(mapper, record, includeFailure, failure));

    return buffer.toString();
  }

  /**
   * Serialises the List of Entity Records
   *
   * @param mapper
   * @param entityRecords : list of entity records
   * @return serialised string results
   * @throws IOException
   */
  public static String serializeExternalJson(ObjectMapper mapper, List<EntityRecord> entityRecords)
      throws IOException {
    final StringWriter buffer = new StringWriter();
    ArrayNode entities = mapper.createArrayNode();
    entityRecords.stream()
        .forEach(
            entityRecord -> {
              ObjectNode entityNode = getExternalJsonNode(mapper, entityRecord, false, null);
              // Entity @context shouldn't appear in metadata
              entityNode.remove(WebEntityFields.CONTEXT);
              entities.add(entityNode);
            });

    ObjectNode result = mapper.createObjectNode();
    result.set(WebEntityFields.CONTEXT, mapper.valueToTree(WebEntityFields.ENTITY_CONTEXT));
    result.set(WebEntityFields.TYPE, mapper.valueToTree(WebEntityConstants.RESULT_PAGE));
    result.set(WebEntityFields.TOTAL, mapper.valueToTree(entityRecords.size()));
    result.set(WebEntityConstants.ITEMS, entities);

    mapper.writeValue(buffer, result);
    return buffer.toString();
  }

  public static void serializeDebugJson(
      Writer writer,
      ObjectMapper mapper,
      EntityRecord record,
      FormatTypes format,
      Optional<FailedTask> failedTask)
      throws IOException {
    JsonNode result = getDebugJsonNode(mapper, record, format, failedTask);
    mapper.writeValue(writer, result);
  }

  private static ObjectNode getExternalJsonNode(
      ObjectMapper mapper,
      EntityRecord record,
      boolean includeFailure,
      Optional<FailedTask> failure)
      throws EntityManagementRuntimeException {
    ObjectNode entityNode = mapper.valueToTree(record.getEntity());

    if (includeFailure) {
      addFailureToEntityNode(mapper, entityNode, failure);
    }

    return entityNode;
  }

  private static void addFailureToEntityNode(
      ObjectMapper mapper, ObjectNode entityNode, Optional<FailedTask> failedTask) {
    ObjectNode lastFailedEntityTaskNode =
        failedTask.isPresent() ? mapper.valueToTree(failedTask.get()) : mapper.createObjectNode();

    JsonNode isAggregatedByJsonNodeWithFailures =
        SerializationUtils.combineNestedNode(
            mapper,
            (ObjectNode) entityNode.get(WebEntityFields.IS_AGGREGATED_BY),
            lastFailedEntityTaskNode,
            WebEntityFields.FAILURES);

    entityNode.set(WebEntityFields.IS_AGGREGATED_BY, isAggregatedByJsonNodeWithFailures);
  }

  private static JsonNode getInternalJsonNode(
      ObjectMapper mapper,
      EntityRecord record,
      boolean includeFailure,
      Optional<FailedTask> failure) {
    ObjectNode entityNode = mapper.valueToTree(record.getEntity());

    if (includeFailure) {
      addFailureToEntityNode(mapper, entityNode, failure);
    }

    List<EntityProxy> recordProxies = record.getProxies();

    ArrayNode proxyNode = mapper.createArrayNode();

    for (EntityProxy proxy : recordProxies) {
      ObjectNode proxyEntityNode = null;
      proxyEntityNode = mapper.valueToTree(proxy.getEntity());
      // Entity @context shouldn't appear in proxy metadata
      proxyEntityNode.remove(WebEntityFields.CONTEXT);
      // Entity ID shouldn't overwrite proxyId
      proxyEntityNode.remove(WebEntityFields.ID);
      ObjectNode embeddedProxyNode = mapper.valueToTree(proxy);
      JsonNode mergedNode =
          SerializationUtils.mergeProxyAndEntity(mapper, embeddedProxyNode, proxyEntityNode);
      proxyNode.add(mergedNode.deepCopy());
    }

    return SerializationUtils.combineNestedNode(mapper, entityNode, proxyNode, "proxies");
  }

  private static JsonNode getDebugJsonNode(
      ObjectMapper mapper, EntityRecord record, FormatTypes format, Optional<FailedTask> failedTask)
      throws JsonMappingException, JsonProcessingException {

    ObjectNode result = mapper.createObjectNode();
    ObjectNode entityNode = mapper.valueToTree(record.getEntity());
    ObjectNode lastFailedEntityTaskNode =
        failedTask.isPresent() ? mapper.valueToTree(failedTask.get()) : mapper.createObjectNode();

    JsonNode isAggregatedByJsonNode = entityNode.get(WebEntityFields.IS_AGGREGATED_BY);
    JsonNode isAggregatedByJsonNodeWithFailures =
        SerializationUtils.combineNestedNode(
            mapper,
            (ObjectNode) isAggregatedByJsonNode,
            lastFailedEntityTaskNode,
            WebEntityFields.FAILURES);
    result.setAll(entityNode);
    result.set(WebEntityFields.IS_AGGREGATED_BY, isAggregatedByJsonNodeWithFailures);
    return result;
  }

  /**
   * Merges the fields of an EntityProxy and its Entity metadata. Expects all field names to be
   * unique
   *
   * @return
   */
  private static JsonNode mergeProxyAndEntity(
      ObjectMapper mapper, ObjectNode proxyNode, ObjectNode entityNode) {
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
   * Combines two JsonNodes. The second node is nested within the first, with the specified field
   * name.
   */
  private static JsonNode combineNestedNode(
      ObjectMapper mapper, ObjectNode parentNode, JsonNode nestedNode, String nestedNodeFieldName) {
    ObjectNode result = mapper.createObjectNode();
    result.setAll(parentNode);
    result.set(nestedNodeFieldName, nestedNode);
    return result;
  }
}
