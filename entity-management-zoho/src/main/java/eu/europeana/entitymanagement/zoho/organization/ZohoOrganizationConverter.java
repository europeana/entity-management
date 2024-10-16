package eu.europeana.entitymanagement.zoho.organization;

import static eu.europeana.entitymanagement.zoho.utils.ZohoUtils.toIsoLanguage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.NonNull;
import com.zoho.crm.api.record.Record;
import com.zoho.crm.api.users.User;
import eu.europeana.entitymanagement.definitions.model.Address;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.definitions.model.WebResource;
import eu.europeana.entitymanagement.definitions.model.ZohoLabelUriMapping;
import eu.europeana.entitymanagement.utils.EntityUtils;
import eu.europeana.entitymanagement.zoho.utils.ZohoConstants;
import eu.europeana.entitymanagement.zoho.utils.ZohoUtils;

public class ZohoOrganizationConverter {

  static final Logger logger = LogManager.getLogger(ZohoOrganizationConverter.class);

  private static final String POSITION_SEPARATOR = "_";
  
  public static Organization convertToOrganizationEntity(Record zohoRecord, String zohoBaseUrl,  @NonNull final Map<String, ZohoLabelUriMapping> countryMappings,
      @NonNull final Map<String, String> roleMappings) {
    Organization org = new Organization();
    Long zohoId = zohoRecord.getId();
    org.setAbout(ZohoUtils.buildZohoOrganizationId(zohoBaseUrl, zohoRecord.getId()));
    org.setIdentifier(List.of(Long.toString(zohoId)));

    // extract language maps
    Map<String, List<String>> allLabels = getAllRecordLabels(zohoRecord);
    Map<String, String> prefLabel = getPrefLabel(allLabels);
    Map<String, List<String>> altLabel = getAltLabel(allLabels);

    // set labels to organization
    org.setPrefLabel(prefLabel);
    if (!altLabel.isEmpty()) {
      org.setAltLabel(altLabel);
    }

    String acronym = getStringFieldValue(zohoRecord, ZohoConstants.ACRONYM_FIELD);
    String langAcronym = getStringFieldValue(zohoRecord, ZohoConstants.LANG_ACRONYM_FIELD);
    org.setAcronym(ZohoUtils.createLanguageMapOfStringList(langAcronym, acronym));
    String logoFieldName = ZohoConstants.LOGO_LINK_TO_WIKIMEDIACOMMONS_FIELD;
    org.setLogo(buildWebResource(zohoRecord, logoFieldName));
    org.setHomepage(getStringFieldValue(zohoRecord, ZohoConstants.WEBSITE_FIELD));
    
    List<String> orgRoleLabels =
        ZohoUtils.stringListSupplier(zohoRecord.getKeyValue(ZohoConstants.ORGANIZATION_ROLE_FIELD));
    if (!orgRoleLabels.isEmpty()) {
      List<String> orgRoleIds = new ArrayList<>();
      for(String roleLabel : orgRoleLabels) {
        if(roleMappings.containsKey(roleLabel.toLowerCase())) {
          orgRoleIds.add(roleMappings.get(roleLabel.toLowerCase()));
        }
      }
      if(! orgRoleIds.isEmpty()) {
        org.setEuropeanaRoleIds(orgRoleIds);
      }
    }

    //build address object
    Address address = new Address();
    address.setVcardStreetAddress(
        ZohoUtils.stringFieldSupplier(zohoRecord.getKeyValue(ZohoConstants.STREET_FIELD)));
    address.setVcardLocality(
        ZohoUtils.stringFieldSupplier(zohoRecord.getKeyValue(ZohoConstants.CITY_FIELD)));
    address.setVcardPostalCode(
        ZohoUtils.stringFieldSupplier(zohoRecord.getKeyValue(ZohoConstants.ZIP_CODE_FIELD)));

    String zohoCountryLabel = ZohoUtils.stringFieldSupplier(zohoRecord.getKeyValue(ZohoConstants.COUNTRY_FIELD));
    if(zohoCountryLabel != null) {
      //update address country
      address.setVcardCountryName(extractCountryName(zohoCountryLabel));
      
      //update organization country id
      if(countryMappings.containsKey(zohoCountryLabel)) {
        //get country ID from mappings
        ZohoLabelUriMapping zohoLabelUriMapping = countryMappings.get(zohoCountryLabel);
        org.setCountryId(zohoLabelUriMapping.getEntityUri());
        org.setCountryISO(zohoLabelUriMapping.getCountryISOCode());
      } else if(logger.isInfoEnabled()){
        logger.info("The mapping for the zoho country label: {}, to the europeana uri does not exist.", zohoCountryLabel);
      }
    }
    
    org.setSameReferenceLinks(getAllSameAs(zohoRecord));

    // only set address if it contains metadata properties.
    if (address.hasMetadataProperties()) {
      address.setAbout(org.getAbout() + ZohoConstants.ADDRESS_ABOUT);
      org.setAddress(address);
    }

    String edmLanguage =
        ZohoUtils.stringFieldSupplier(zohoRecord.getKeyValue(ZohoConstants.OFFICIAL_LANGUAGE_FIELD));
    if (edmLanguage!=null) {
      List<String> edmISOLanguage = Arrays.asList(ZohoUtils.toIsoLanguage(edmLanguage));
      org.setLanguage(edmISOLanguage);
    }

    // hidden labels 1-4
    List<String> hiddenLabels = new ArrayList<String>();
    String hiddenLabel1 = getStringFieldValue(zohoRecord, ZohoConstants.HIDDEN_LABEL1_FIELD);
    addValueToList(hiddenLabel1, hiddenLabels);
    String hiddenLabel2 = getStringFieldValue(zohoRecord, ZohoConstants.HIDDEN_LABEL2_FIELD);
    addValueToList(hiddenLabel2, hiddenLabels);
    String hiddenLabel3 = getStringFieldValue(zohoRecord, ZohoConstants.HIDDEN_LABEL3_FIELD);
    addValueToList(hiddenLabel3, hiddenLabels);
    String hiddenLabel4 = getStringFieldValue(zohoRecord, ZohoConstants.HIDDEN_LABEL4_FIELD);
    addValueToList(hiddenLabel4, hiddenLabels);
    // hidden labels text area
    hiddenLabels.addAll(getTextAreaFieldValues(zohoRecord, ZohoConstants.HIDDEN_LABEL_FIELD));
    if (hiddenLabels.size()>0) {
      org.setHiddenLabel(hiddenLabels);
    }

    return org;
  }

  private static String extractCountryName(String zohoCountryLabel) {
    //get only the country name from zohoLabels (e.g France, FR)
    return StringUtils.substringBeforeLast(zohoCountryLabel, ",").trim();
  }

  private static WebResource buildWebResource(Record zohoRecord, String logoFieldName) {
    String id = getStringFieldValue(zohoRecord, logoFieldName);
    if (id == null) {
      return null;
    }
    WebResource resource = new WebResource();
    resource.setId(id);
    resource.setSource(EntityUtils.createWikimediaResourceString(id));
    return resource;
  }

  private static Map<String, String> getPrefLabel(Map<String, List<String>> allLabels) {
    Map<String, String> prefLabel = new LinkedHashMap<>(allLabels.size());
    // first label for each language goes to prefLabel map
    allLabels.forEach((key, value) -> prefLabel.put(key, value.get(0)));
    return prefLabel;
  }

  private static Map<String, List<String>> getAltLabel(Map<String, List<String>> allLabels) {
    Map<String, List<String>> altLabel = new LinkedHashMap<>();
    for (Map.Entry<String, List<String>> entry : allLabels.entrySet()) {
      int size = entry.getValue().size();
      // starting with second entry for each language, everything goes to altLabel map
      if (size > 1) {
        altLabel.put(entry.getKey(), entry.getValue().subList(1, size));
      }
    }
    return altLabel;
  }

  private static Map<String, List<String>> getAllRecordLabels(Record zohoRecord) {
    Map<String, List<String>> allLabels = new LinkedHashMap<>();

    // read account name first
    addLabel(
        zohoRecord,
        allLabels,
        ZohoConstants.LANG_ORGANIZATION_NAME_FIELD,
        ZohoConstants.ACCOUNT_NAME_FIELD);
    // read alternative zoho labels
    for (int i = 1; i <= ZohoConstants.LANGUAGE_CODE_LENGTH; i++) {
      addLabel(
          zohoRecord,
          allLabels,
          ZohoConstants.LANG_ALTERNATIVE_FIELD + POSITION_SEPARATOR + i,
          ZohoConstants.ALTERNATIVE_FIELD + POSITION_SEPARATOR + i);
    }
    return allLabels;
  }

  static void addLabel(
      Record zohoRecord,
      Map<String, List<String>> allLabels,
      String langFieldName,
      String labelFiedName) {
    String isoLanguage = getIsoLanguage(zohoRecord, langFieldName);
    String label = getStringFieldValue(zohoRecord, labelFiedName);
    if (label != null) {
      addLabel(allLabels, isoLanguage, label);
    }
  }

  public static String getStringFieldValue(Record zohoRecord, String zohoFieldName) {
    return ZohoUtils.stringFieldSupplier(zohoRecord.getKeyValue(zohoFieldName));
  }

  public static String getEuropeanaIdFieldValue(Record zohoRecord) {
    return getStringFieldValue(zohoRecord, ZohoConstants.EUROPEANA_ID_FIELD);
  }
  
  static List<String> getTextAreaFieldValues(Record zohoRecord, String zohoFieldName) {
    String textArea = ZohoUtils.stringFieldSupplier(zohoRecord.getKeyValue(zohoFieldName));
    if (StringUtils.isBlank(textArea)) {
      return Collections.emptyList();
    }

    //regex to split on any new line system variations ("\n", "\r\n", or "\r") 
    return List.of(textArea.split("\\r?\\n|\\r"));
  }

  static String getIsoLanguage(Record zohoRecord, String zohoLangFieldName) {
    return toIsoLanguage(getStringFieldValue(zohoRecord, zohoLangFieldName));
  }

  static void addValueToList(String value, List<String> list) {
    if (value != null) {
      list.add(value);
    }
  }

  static void addLabel(Map<String, List<String>> allLabels, String isoLanguage, String label) {
    allLabels.computeIfAbsent(isoLanguage, k -> new ArrayList<>());
    allLabels.get(isoLanguage).add(label);
  }

  private static List<String> getAllSameAs(Record zohoRecord) {
    List<String> sameAsList = new ArrayList<>();
    for (int i = 1; i <= ZohoConstants.SAME_AS_CODE_LENGTH; i++) {
      String sameAs = getStringFieldValue(zohoRecord, ZohoConstants.SAME_AS_FIELD + "_" + i);
      if (sameAs != null) {
        sameAsList.add(sameAs);
      }
    }
    return sameAsList;
  }

  /**
   * The method is to process the ZOHO_OWNER_FIELD name value
   *
   * @param recordOrganization the zoho record
   * @return the name of the owner
   */
  public static String getOwnerName(Record recordOrganization) {
    Object keyValue = recordOrganization.getKeyValue(ZohoConstants.ZOHO_OWNER_FIELD);
    return ((User) keyValue).getName();
  }
  
  /**
   * The method is to process the ZOHO_MODIFIED_BY_FIELD name value
   *
   * @param recordOrganization the zoho record
   * @return the name of the user that last modified the record
   */
  public static String getModifiedByName(Record recordOrganization) {
    return ((User) recordOrganization.getKeyValue(ZohoConstants.ZOHO_MODIFIED_BY_FIELD)).getName();
  }

  public static boolean isMarkedForDeletion(Record recordOrganization) {
    Object scheduledDeletion =
        recordOrganization.getKeyValue(ZohoConstants.ZOHO_SCHEDULED_DELETION);
    if (scheduledDeletion == null) {
      return false;
    } else {
      return ((Boolean) scheduledDeletion).booleanValue();
    }
  }
  
}
