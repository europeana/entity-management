package eu.europeana.entitymanagement.testutils;

import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.*;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.zoho.crm.api.record.Record;
import com.zoho.crm.api.util.Choice;

/** Helper class to deserialize JSON into Zoho {@link Record} to make testing easier */
public abstract class BaseZohoRecordDeserializer<T> extends StdDeserializer<T> {

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
          LANG_ACRONYM_1_FIELD,
          ACRONYM_1_FIELD,
          LOGO_LINK_TO_THUMBNAIL_FIELD,
          WEBSITE_FIELD,
          STREET_FIELD,
          CITY_FIELD,
          COUNTRY_FIELD,
          COUNTRY_URI_FIELD,
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
          INDUSTRY_FIELD,
          HERITAGE_DOMAIN,
          PUBLIC_EMAIL,
          GEOGRAPHIC_SCOPE,
          MEDIA_TYPE,
          DATA_ACTIVITY,
          AUDIENCE_ENGAGEMENT_ACTIVITY,
          CAPACITY_BUILDING,
          AGGREGATORS,
          EUROPEANA_ID_FIELD,
          AGGREGATING_FROM);

  public BaseZohoRecordDeserializer() {
    this(null);
  }

  public BaseZohoRecordDeserializer(Class<?> vc) {
    super(vc);
  }

  Record deserializeSingleRecord(JsonNode node) {
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
      }else if(currentNode.isObject()) {
        System.out.println("object node: " + key);
        if(AGGREGATORS.equals(key) || AGGREGATING_FROM.equals(key)) {
          Record subRecord = new Record();  
          subRecord.setId(currentNode.get(ID_FIELD).asLong());
          subRecord.addKeyValue(NAME_FIELD, currentNode.get(NAME_FIELD).asText());
          record.addKeyValue(key, subRecord);
        }
      }else if (currentNode.isContainerNode()){
        System.out.println("container node: " + key);
      }else if(currentNode.isPojo()) {
        System.out.println("pojo node: " + key);
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
