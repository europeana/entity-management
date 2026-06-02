package eu.europeana.entitymanagement.testutils;

import java.io.IOException;
import java.util.List;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.zoho.crm.api.record.Record;

import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.*;

/** Helper class to deserialize JSON into Zoho {@link Record} to make testing easier */
public class ZohoRecordTestDeserializer extends BaseZohoRecordDeserializer<Record> {

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

  public ZohoRecordTestDeserializer() {
    this(null);
  }

  public ZohoRecordTestDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public Record deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    return deserializeSingleRecord(p.getCodec().readTree(p));
  }
}
