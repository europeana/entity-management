package eu.europeana.entitymanagement.testutils;

import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.ACCOUNT_NAME_FIELD;
import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.ACRONYM_FIELD;
import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.ALTERNATIVE_FIELD;
import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.CITY_FIELD;
import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.COUNTRY_FIELD;
import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.HIDDEN_LABEL1_FIELD;
import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.HIDDEN_LABEL2_FIELD;
import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.HIDDEN_LABEL3_FIELD;
import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.HIDDEN_LABEL4_FIELD;
import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.HIDDEN_LABEL_FIELD;
import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.ID_FIELD;
import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.INDUSTRY_FIELD;
import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.LANGUAGE_CODE_LENGTH;
import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.LANG_ACRONYM_FIELD;
import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.LANG_ALTERNATIVE_FIELD;
import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.LANG_ORGANIZATION_NAME_FIELD;
import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.LATITUDE_FIELD;
import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.LOGO_LINK_TO_WIKIMEDIACOMMONS_FIELD;
import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.LONGITUDE_FIELD;
import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.OFFICIAL_LANGUAGE_FIELD;
import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.ORGANIZATION_COUNTRY_FIELD;
import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.ORGANIZATION_ROLE_FIELD;
import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.PO_BOX_FIELD;
import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.SAME_AS_CODE_LENGTH;
import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.SAME_AS_FIELD;
import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.WEBSITE_FIELD;
import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.ZIP_CODE_FIELD;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.zoho.crm.api.record.Record;
import com.zoho.crm.api.util.Choice;

/** Helper class to deserialize JSON into Zoho {@link Record} to make testing easier */
public class ZohoRecordTestDeserializer extends StdDeserializer<Record> {

  /** */
  private static final long serialVersionUID = 7519475270154623735L;
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
          ORGANIZATION_COUNTRY_FIELD,
          CITY_FIELD,
          COUNTRY_FIELD,
          ZIP_CODE_FIELD,
          PO_BOX_FIELD,
          OFFICIAL_LANGUAGE_FIELD,
          LATITUDE_FIELD,
          LONGITUDE_FIELD,
          HIDDEN_LABEL1_FIELD,
          HIDDEN_LABEL2_FIELD,
          HIDDEN_LABEL3_FIELD,
          HIDDEN_LABEL4_FIELD,
          HIDDEN_LABEL_FIELD,
          INDUSTRY_FIELD);

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

      // JSON contains strings, arrays
      if (currentNode.isTextual()) {
        record.addKeyValue(key, currentNode.asText());
      } else if (currentNode.isArray()) {
        List<Choice<?>> values = new ArrayList<Choice<?>>();
        currentNode.elements().forEachRemaining(v -> values.add(new Choice<String>(v.asText())));
        record.addKeyValue(key, values);
      }else if (currentNode.isContainerNode()){
        System.out.println("container node: " + key);
      }else if(currentNode.isPojo()) {
        System.out.println("pojo node: " + key);
      }else if(currentNode.isObject()) {
        System.out.println("object node: " + key);
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
