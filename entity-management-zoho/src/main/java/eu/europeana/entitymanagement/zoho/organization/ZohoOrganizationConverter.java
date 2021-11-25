package eu.europeana.entitymanagement.zoho.organization;

import static eu.europeana.entitymanagement.zoho.utils.ZohoUtils.toIsoLanguage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import com.zoho.crm.api.record.Record;
import eu.europeana.entitymanagement.definitions.model.Address;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.zoho.utils.ZohoConstants;
import eu.europeana.entitymanagement.zoho.utils.ZohoUtils;

public class ZohoOrganizationConverter {

  private static final String POSITION_SEPARATOR = "_";

  private ZohoOrganizationConverter() {
    // private constructor to prevent instantiation
  }

  public static Organization convertToOrganizationEntity(Record record) {
    Organization org = new Organization();
    org.setAbout(ZohoConstants.URL_ORGANIZATION_PREFFIX + record.getId());
    org.setIdentifier(ZohoUtils.stringListSupplier(List.of(Long.toString(record.getId()))));

    // extract language maps
    Map<String, List<String>> allLabels = getAllRecordLabels(record);
    Map<String, String> prefLabel = getPrefLabel(allLabels);
    Map<String, List<String>> altLabel = getAltLabel(allLabels);

    // set labels to organization
    org.setPrefLabel(prefLabel);
    if (!altLabel.isEmpty()) {
      org.setAltLabel(altLabel);
    }

    String acronym = getStringFieldValue(record, ZohoConstants.ACRONYM_FIELD);
    String langAcronym = getStringFieldValue(record, ZohoConstants.LANG_ACRONYM_FIELD);
    org.setAcronym(ZohoUtils.createLanguageMapOfStringList(langAcronym, acronym));
    org.setLogo(getStringFieldValue(record, ZohoConstants.LOGO_LINK_TO_WIKIMEDIACOMMONS_FIELD));
    org.setHomepage(getStringFieldValue(record, ZohoConstants.WEBSITE_FIELD));
    List<String> organizationRoleStringList =
        ZohoUtils.stringListSupplier(record.getKeyValue(ZohoConstants.ORGANIZATION_ROLE_FIELD));
    if (!organizationRoleStringList.isEmpty()) {
      org.setEuropeanaRole(
          ZohoUtils.createLanguageMapOfStringList(
              Locale.ENGLISH.getLanguage(), organizationRoleStringList));
    }
    org.setOrganizationDomain(
        ZohoUtils.createMapWithLists(
            Locale.ENGLISH.getLanguage(),
            ZohoUtils.stringFieldSupplier(record.getKeyValue(ZohoConstants.DOMAIN_FIELD))));

    List<String> geographicLevel =
        ZohoUtils.stringListSupplier(record.getKeyValue(ZohoConstants.GEOGRAPHIC_LEVEL_FIELD));
    if (!geographicLevel.isEmpty()) {
      org.setGeographicLevel(
          ZohoUtils.createMap(Locale.ENGLISH.getLanguage(), geographicLevel.get(0)));
    }

    String organizationCountry =
        toEdmCountry(getStringFieldValue(record, ZohoConstants.ORGANIZATION_COUNTRY_FIELD));
    org.setCountry(organizationCountry);
    List<String> sameAs = getAllSameAs(record);
    if (!sameAs.isEmpty()) {
      org.setSameReferenceLinks(sameAs);
    }

    Address address = new Address();
    address.setAbout(org.getAbout() + ZohoConstants.ADDRESS_ABOUT);
    address.setVcardStreetAddress(
        ZohoUtils.stringFieldSupplier(record.getKeyValue(ZohoConstants.STREET_FIELD)));
    address.setVcardLocality(
        ZohoUtils.stringFieldSupplier(record.getKeyValue(ZohoConstants.CITY_FIELD)));
    address.setVcardCountryName(
        ZohoUtils.stringFieldSupplier(record.getKeyValue(ZohoConstants.COUNTRY_FIELD)));
    address.setVcardPostalCode(
        ZohoUtils.stringFieldSupplier(record.getKeyValue(ZohoConstants.ZIP_CODE_FIELD)));
    address.setVcardPostOfficeBox(
        ZohoUtils.stringFieldSupplier(record.getKeyValue(ZohoConstants.PO_BOX_FIELD)));
    org.setAddress(address);

    return org;
  }

  private static Map<String, String> getPrefLabel(Map<String, List<String>> allLabels) {
    Map<String, String> prefLabel = new LinkedHashMap<String, String>(allLabels.size());
    // first label for each language goes to prefLabel map
    allLabels.forEach((key, value) -> prefLabel.put(key, value.get(0)));
    return prefLabel;
  }

  private static Map<String, List<String>> getAltLabel(Map<String, List<String>> allLabels) {
    Map<String, List<String>> altLabel = new LinkedHashMap<String, List<String>>();
    for (Map.Entry<String, List<String>> entry : allLabels.entrySet()) {
      int size = entry.getValue().size();
      // starting with second entry for each language, everything goes to altLabel map
      if (size > 1) {
        altLabel.put(entry.getKey(), entry.getValue().subList(1, size));
      }
    }
    return altLabel;
  }

  private static Map<String, List<String>> getAllRecordLabels(Record record) {
    Map<String, List<String>> allLabels = new LinkedHashMap<String, List<String>>();

    // read account name first
    addLabel(
        record,
        allLabels,
        ZohoConstants.LANG_ORGANIZATION_NAME_FIELD,
        ZohoConstants.ACCOUNT_NAME_FIELD);
    // read alternative zoho labels
    for (int i = 1; i <= ZohoConstants.LANGUAGE_CODE_LENGTH; i++) {
      addLabel(
          record,
          allLabels,
          ZohoConstants.LANG_ALTERNATIVE_FIELD + POSITION_SEPARATOR + i,
          ZohoConstants.ALTERNATIVE_FIELD + POSITION_SEPARATOR + i);
    }
    return allLabels;
  }

  static void addLabel(
      Record record,
      Map<String, List<String>> allLabels,
      String langFieldName,
      String labelFiedName) {
    String isoLanguage = getIsoLanguage(record, langFieldName);
    String label = getStringFieldValue(record, labelFiedName);
    if (label != null) {
      addLabel(allLabels, isoLanguage, label);
    }
  }

  static String getStringFieldValue(Record record, String zohoFieldName) {
    return ZohoUtils.stringFieldSupplier(record.getKeyValue(zohoFieldName));
  }

  static String getIsoLanguage(Record record, String zohoLangFieldName) {
    return toIsoLanguage(getStringFieldValue(record, zohoLangFieldName));
  }

  static void addLabel(Map<String, List<String>> allLabels, String isoLanguage, String label) {
    if (!allLabels.containsKey(isoLanguage)) {
      allLabels.put(isoLanguage, new ArrayList<String>());
    }
    allLabels.get(isoLanguage).add(label);
  }

  private static List<String> getAllSameAs(Record record) {
    List<String> sameAsList = new ArrayList<>();
    for (int i = 1; i <= ZohoConstants.SAME_AS_CODE_LENGTH; i++) {
      String sameAs = getStringFieldValue(record, ZohoConstants.SAME_AS_FIELD + "_" + i);
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
}
