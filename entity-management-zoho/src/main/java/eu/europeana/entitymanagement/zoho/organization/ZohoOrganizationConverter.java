package eu.europeana.entitymanagement.zoho.organization;

import static eu.europeana.entitymanagement.zoho.utils.ZohoUtils.toIsoLanguage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.zoho.crm.api.record.Record;
import com.zoho.crm.api.users.User;
import eu.europeana.entitymanagement.definitions.model.Address;
import eu.europeana.entitymanagement.definitions.model.Country;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.definitions.model.WebResource;
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;
import eu.europeana.entitymanagement.utils.EntityUtils;
import eu.europeana.entitymanagement.zoho.utils.ZohoConstants;
import eu.europeana.entitymanagement.zoho.utils.ZohoUtils;

@Component
public class ZohoOrganizationConverter {
  
  private final EntityRecordRepository entityRecordRepository;
  private final ZohoConfiguration zohoConfiguration;
  private Map<String, String> countryMapping;

  static final Logger logger = LogManager.getLogger(ZohoOrganizationConverter.class);

  private static final String POSITION_SEPARATOR = "_";
  
  @Autowired
  public ZohoOrganizationConverter(EntityRecordRepository entityRecordRepository, ZohoConfiguration zohoConfiguration) throws IOException {
    this.entityRecordRepository=entityRecordRepository;
    this.zohoConfiguration=zohoConfiguration;
    //reading the country mapping file
    countryMapping = new HashMap<>();
    String countryMappingFile = this.zohoConfiguration.getZohoCountryMappingFile();
    try (InputStream inputStream = getClass().getResourceAsStream(countryMappingFile)) {
      assert inputStream != null;
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
        String contents = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        if(! StringUtils.isBlank(contents)) {
          JSONObject contentsJson = new JSONObject(contents);
          contentsJson.keys().forEachRemaining(key -> {
            countryMapping.put(key, contentsJson.getString(key));
          });
        }
      }
    }

  }

  public Organization convertToOrganizationEntity(Record zohoRecord) {
    Organization org = new Organization();
    Long zohoId = zohoRecord.getId();
    org.setAbout(ZohoUtils.buildZohoOrganizationId(zohoConfiguration.getZohoBaseUrl(), zohoRecord.getId()));
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
    List<String> organizationRoleStringList =
        ZohoUtils.stringListSupplier(zohoRecord.getKeyValue(ZohoConstants.ORGANIZATION_ROLE_FIELD));
    if (!organizationRoleStringList.isEmpty()) {
      org.setEuropeanaRole(
          ZohoUtils.createLanguageMapOfStringList(
              Locale.ENGLISH.getLanguage(), organizationRoleStringList));
    }

    Address address = new Address();
    address.setVcardStreetAddress(
        ZohoUtils.stringFieldSupplier(zohoRecord.getKeyValue(ZohoConstants.STREET_FIELD)));
    address.setVcardLocality(
        ZohoUtils.stringFieldSupplier(zohoRecord.getKeyValue(ZohoConstants.CITY_FIELD)));
    String vcardCountryName = ZohoUtils.stringFieldSupplier(zohoRecord.getKeyValue(ZohoConstants.COUNTRY_FIELD));
    address.setVcardCountryName(vcardCountryName);
    address.setVcardPostalCode(
        ZohoUtils.stringFieldSupplier(zohoRecord.getKeyValue(ZohoConstants.ZIP_CODE_FIELD)));

    //set country
    String orgCountryUri = countryMapping.get(vcardCountryName);
    if(StringUtils.isBlank(orgCountryUri)) {
      logger.info("The mapping for the zoho country: {}, to the uri does not exist.", vcardCountryName);
    }
    else {
      Country orgCountry = new Country();
      orgCountry.setId(orgCountryUri);
      EntityRecord orgEntityRecord = entityRecordRepository.findByEntityId(orgCountryUri);
      if(orgEntityRecord==null) {
        logger.info("The entity record to be dereferenced for the zoho country with the id: {}, does not exist in the db.", orgCountryUri);
      }
      else {
        orgCountry.setPrefLabel(orgEntityRecord.getEntity().getPrefLabel());
      }
      org.setCountry(orgCountry);  
    }

    org.setSameReferenceLinks(getAllSameAs(zohoRecord));

    // only set address if it contains metadata properties.
    if (address.hasMetadataProperties()) {
      address.setAbout(org.getAbout() + ZohoConstants.ADDRESS_ABOUT);
      org.setAddress(address);
    }

    List<String> edmLanguage =
        ZohoUtils.stringListSupplier(zohoRecord.getKeyValue(ZohoConstants.OFFICIAL_LANGUAGE_FIELD));
    if (!edmLanguage.isEmpty()) {
      List<String> edmISOLanguage =
          edmLanguage.stream().map(ZohoUtils::toIsoLanguage).collect(Collectors.toList());
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

  private WebResource buildWebResource(Record zohoRecord, String logoFieldName) {
    String id = getStringFieldValue(zohoRecord, logoFieldName);
    if (id == null) {
      return null;
    }
    WebResource resource = new WebResource();
    resource.setId(id);
    resource.setSource(EntityUtils.createWikimediaResourceString(id));
    return resource;
  }

  private Map<String, String> getPrefLabel(Map<String, List<String>> allLabels) {
    Map<String, String> prefLabel = new LinkedHashMap<>(allLabels.size());
    // first label for each language goes to prefLabel map
    allLabels.forEach((key, value) -> prefLabel.put(key, value.get(0)));
    return prefLabel;
  }

  private Map<String, List<String>> getAltLabel(Map<String, List<String>> allLabels) {
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

  private Map<String, List<String>> getAllRecordLabels(Record zohoRecord) {
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

  void addLabel(
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

  public String getStringFieldValue(Record zohoRecord, String zohoFieldName) {
    return ZohoUtils.stringFieldSupplier(zohoRecord.getKeyValue(zohoFieldName));
  }

  public String getEuropeanaIdFieldValue(Record zohoRecord) {
    return getStringFieldValue(zohoRecord, ZohoConstants.EUROPEANA_ID_FIELD);
  }
  
  List<String> getTextAreaFieldValues(Record zohoRecord, String zohoFieldName) {
    String textArea = ZohoUtils.stringFieldSupplier(zohoRecord.getKeyValue(zohoFieldName));
    if (StringUtils.isBlank(textArea)) {
      return Collections.emptyList();
    }

    return List.of(StringUtils.split(textArea, "\n"));
  }

  String getIsoLanguage(Record zohoRecord, String zohoLangFieldName) {
    return toIsoLanguage(getStringFieldValue(zohoRecord, zohoLangFieldName));
  }

  void addValueToList(String value, List<String> list) {
    if (value != null) {
      list.add(value);
    }
  }

  void addLabel(Map<String, List<String>> allLabels, String isoLanguage, String label) {
    allLabels.computeIfAbsent(isoLanguage, k -> new ArrayList<>());
    allLabels.get(isoLanguage).add(label);
  }

  private List<String> getAllSameAs(Record zohoRecord) {
    List<String> sameAsList = new ArrayList<>();
    for (int i = 1; i <= ZohoConstants.SAME_AS_CODE_LENGTH; i++) {
      String sameAs = getStringFieldValue(zohoRecord, ZohoConstants.SAME_AS_FIELD + "_" + i);
      if (sameAs != null) {
        sameAsList.add(sameAs);
      }
    }
    return sameAsList;
  }

//  private static String toEdmCountry(String organizationCountry) {
//    if (StringUtils.isBlank(organizationCountry)) {
//      return null;
//    } else {
//      String isoCode = null;
//      int commaSeparatorPos = organizationCountry.indexOf(44);
//      int bracketSeparatorPos = organizationCountry.indexOf(40);
//      if (commaSeparatorPos > 0) {
//        isoCode = organizationCountry.substring(commaSeparatorPos + 1).trim();
//      } else if (bracketSeparatorPos > 0) {
//        isoCode = organizationCountry.substring(0, bracketSeparatorPos).trim();
//      }
//
//      return isoCode;
//    }
//  }

  /**
   * The method is to process the ZOHO_OWNER_FIELD name value
   *
   * @param recordOrganization
   * @return
   */
  public String getOwnerName(Record recordOrganization) {
    return ((User) recordOrganization.getKeyValue(ZohoConstants.ZOHO_OWNER_FIELD)).getName();
  }

  public boolean isMarkedForDeletion(Record recordOrganization) {
    Object scheduledDeletion =
        recordOrganization.getKeyValue(ZohoConstants.ZOHO_SCHEDULED_DELETION);
    if (scheduledDeletion == null) {
      return false;
    } else {
      return ((Boolean) scheduledDeletion).booleanValue();
    }
  }
  
}
