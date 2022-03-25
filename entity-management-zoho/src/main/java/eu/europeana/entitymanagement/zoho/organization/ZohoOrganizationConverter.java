package eu.europeana.entitymanagement.zoho.organization;

import static eu.europeana.entitymanagement.zoho.utils.ZohoUtils.toIsoLanguage;

import com.zoho.crm.api.record.Record;
import com.zoho.crm.api.users.User;
import eu.europeana.entitymanagement.definitions.model.Address;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.definitions.model.WebResource;
import eu.europeana.entitymanagement.utils.EntityUtils;
import eu.europeana.entitymanagement.zoho.utils.ZohoConstants;
import eu.europeana.entitymanagement.zoho.utils.ZohoUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class ZohoOrganizationConverter {

  private static final String POSITION_SEPARATOR = "_";

  private ZohoOrganizationConverter() {
    // private constructor to prevent instantiation
  }

  public static Organization convertToOrganizationEntity(Record zohoRecord) {
    Organization org = new Organization();
    Long zohoId = zohoRecord.getId();
    org.setAbout(ZohoUtils.buildZohoOrganizationId(zohoRecord.getId()));
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
    org.setOrganizationDomain(
        ZohoUtils.createMapWithLists(
            Locale.ENGLISH.getLanguage(),
            ZohoUtils.stringFieldSupplier(zohoRecord.getKeyValue(ZohoConstants.DOMAIN_FIELD))));

    List<String> geographicLevel =
        ZohoUtils.stringListSupplier(zohoRecord.getKeyValue(ZohoConstants.GEOGRAPHIC_LEVEL_FIELD));
    if (!geographicLevel.isEmpty()) {
      org.setGeographicLevel(
          ZohoUtils.createMap(Locale.ENGLISH.getLanguage(), geographicLevel.get(0)));
    }

    String organizationCountry =
        toEdmCountry(getStringFieldValue(zohoRecord, ZohoConstants.ORGANIZATION_COUNTRY_FIELD));
    org.setCountry(organizationCountry);
    org.setSameReferenceLinks(getAllSameAs(zohoRecord));

    Address address = new Address();
    address.setVcardStreetAddress(
        ZohoUtils.stringFieldSupplier(zohoRecord.getKeyValue(ZohoConstants.STREET_FIELD)));
    address.setVcardLocality(
        ZohoUtils.stringFieldSupplier(zohoRecord.getKeyValue(ZohoConstants.CITY_FIELD)));
    address.setVcardCountryName(
        ZohoUtils.stringFieldSupplier(zohoRecord.getKeyValue(ZohoConstants.COUNTRY_FIELD)));
    address.setVcardPostalCode(
        ZohoUtils.stringFieldSupplier(zohoRecord.getKeyValue(ZohoConstants.ZIP_CODE_FIELD)));
    address.setVcardPostOfficeBox(
        ZohoUtils.stringFieldSupplier(zohoRecord.getKeyValue(ZohoConstants.PO_BOX_FIELD)));

    String lat =
        ZohoUtils.stringFieldSupplier(zohoRecord.getKeyValue(ZohoConstants.LATITUDE_FIELD));
    String lon =
        ZohoUtils.stringFieldSupplier(zohoRecord.getKeyValue(ZohoConstants.LONGITUDE_FIELD));
    if (lat != null && lon != null) {
      address.setVcardHasGeo(EntityUtils.toGeoUri(lat, lon));
    }

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

    String hiddenLabel1 = getStringFieldValue(zohoRecord, ZohoConstants.HIDDEN_LABEL1_FIELD);
    String hiddenLabel2 = getStringFieldValue(zohoRecord, ZohoConstants.HIDDEN_LABEL2_FIELD);
    String hiddenLabel3 = getStringFieldValue(zohoRecord, ZohoConstants.HIDDEN_LABEL3_FIELD);
    String hiddenLabel4 = getStringFieldValue(zohoRecord, ZohoConstants.HIDDEN_LABEL4_FIELD);

    if (hiddenLabel1 != null
        || hiddenLabel2 != null
        || hiddenLabel3 != null
        || hiddenLabel4 != null) {

      List<String> hiddenLabels = new ArrayList<String>();
      addValueToList(hiddenLabel1, hiddenLabels);
      addValueToList(hiddenLabel2, hiddenLabels);
      addValueToList(hiddenLabel3, hiddenLabels);
      addValueToList(hiddenLabel4, hiddenLabels);

      org.setHiddenLabel(hiddenLabels);
    }

    List<String> industry =
        ZohoUtils.stringListSupplier(zohoRecord.getKeyValue(ZohoConstants.INDUSTRY_FIELD));
    if (!industry.isEmpty()) {
      Map<String, List<String>> orgDomain = new HashMap<String, List<String>>();
      orgDomain.put("en", industry);
      org.setOrganizationDomain(orgDomain);
    }

    return org;
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

  static String getStringFieldValue(Record zohoRecord, String zohoFieldName) {
    return ZohoUtils.stringFieldSupplier(zohoRecord.getKeyValue(zohoFieldName));
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

  private static String toEdmCountry(String organizationCountry) {
    if (StringUtils.isBlank(organizationCountry)) {
      return null;
    } else {
      String isoCode = null;
      int commaSeparatorPos = organizationCountry.indexOf(44);
      int bracketSeparatorPos = organizationCountry.indexOf(40);
      if (commaSeparatorPos > 0) {
        isoCode = organizationCountry.substring(commaSeparatorPos + 1).trim();
      } else if (bracketSeparatorPos > 0) {
        isoCode = organizationCountry.substring(0, bracketSeparatorPos).trim();
      }

      return isoCode;
    }
  }

  /**
   * The method is to process the ZOHO_OWNER_FIELD name value
   *
   * @param recordOrganization
   * @return
   */
  public static String getOwnerName(Record recordOrganization) {
    return ((User) recordOrganization.getKeyValue(ZohoConstants.ZOHO_OWNER_FIELD)).getName();
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
