package eu.europeana.entitymanagement.testutils;

import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.zoho.crm.api.record.Record;
import com.zoho.crm.api.util.Choice;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Helper class to deserialize JSON into Zoho {@link Record} to make testing easier */
public class ZohoRecordTestDeserializer extends StdDeserializer<Record> {

  /**
   * Contains list of fields to read from JSON file.
   *
   * <p>Fields with numeric suffixes (sameAs, lang code) and ID field are handled separately
   */
  private static final List<String> ZOHO_JSON_FIELDS =
      List.of(
          ID_FIELD,
          ACCOUNT_NAME_FIELD,
          ORGANIZATION_ROLE_FIELD,
          LANG_ORGANIZATION_NAME_FIELD,
          LANG_ACRONYM_FIELD,
          ACRONYM_FIELD,
          LOGO_LINK_TO_WIKIMEDIACOMMONS_FIELD,
          WEBSITE_FIELD,
          DOMAIN_FIELD,
          GEOGRAPHIC_LEVEL_FIELD,
          ORGANIZATION_COUNTRY_FIELD,
          CITY_FIELD,
          COUNTRY_FIELD,
          ZIP_CODE_FIELD,
          PO_BOX_FIELD,
          OFFICIAL_LANGUAGE_FIELD);

  public ZohoRecordTestDeserializer() {
    this(null);
  }

  public ZohoRecordTestDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public Record deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    JsonNode node = p.getCodec().readTree(p);
    Record record = new Record();

    for (String key : ZOHO_JSON_FIELDS) {
      JsonNode currentNode = node.get(key);

      if (currentNode == null) {
        continue;
      }

      // JSON only contains strings or arrays
      if (currentNode.isTextual()) {
        record.addKeyValue(key, currentNode.asText());
      } else if (currentNode.isArray()) {
        List<Choice<?>> values = new ArrayList<Choice<?>>();
        currentNode.elements().forEachRemaining(v -> values.add(new Choice<String>(v.asText())));
        record.addKeyValue(key, values);
      }
    }

    // add fields with numeric suffixes
    addMultiField(node, record, ALTERNATIVE_FIELD, LANGUAGE_CODE_LENGTH);
    addMultiField(node, record, LANG_ALTERNATIVE_FIELD, LANGUAGE_CODE_LENGTH);
    addMultiField(node, record, SAME_AS_FIELD, SAME_AS_CODE_LENGTH);

    // add ID
    record.setId(node.get(ID_FIELD).asLong());

    return record;
  }

  private void addMultiField(JsonNode node, Record record, String fieldName, int length) {
    for (int i = 1; i <= length; i++) {
      JsonNode currentNode = node.get(fieldName + "_" + i);
      if (currentNode != null && currentNode.isTextual()) {
        record.addKeyValue(fieldName + "_" + i, currentNode.asText());
      }
    }
  }
}
