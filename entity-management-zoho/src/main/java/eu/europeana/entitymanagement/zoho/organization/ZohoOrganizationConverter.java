package eu.europeana.entitymanagement.zoho.organization;

import static eu.europeana.entitymanagement.zoho.utils.ZohoUtils.toIsoLanguage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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

    private static final int LANGUAGE_CODE_LENGTH = 5;
    private static final int SAME_AS_CODE_LENGTH = 2;

    public Organization convertToOrganizationEntity(Record record) {
        Organization org = new Organization();
        org.setAbout(ZohoConstants.URL_ORGANIZATION_PREFFIX + Long.toString(record.getId()));
        org.setIdentifier(ZohoUtils.stringListSupplier(Arrays.asList(new String[]{Long.toString(record.getId())})));
        String isoLanguage = toIsoLanguage(ZohoUtils.stringFieldSupplier(record.getKeyValue(ZohoConstants.LANG_ORGANIZATION_NAME_FIELD)));
        org.setPrefLabel(ZohoUtils.createMap(isoLanguage, ZohoUtils.stringFieldSupplier(record.getKeyValue(ZohoConstants.ACCOUNT_NAME_FIELD))));
//        String isoLanguage1 = toIsoLanguage(ZohoUtils.stringFieldSupplier(record.getKeyValue(ZohoConstants.LANG_ALTERNATIVE_FIELD)));
//        org.setAltLabel(ZohoUtils.createMapWithLists(isoLanguage1, ZohoUtils.stringFieldSupplier(record.getKeyValue(ZohoConstants.ALTERNATIVE_FIELD))));
        org.setAltLabel(getAllAltLabel(record));

        String acronym = ZohoUtils.stringFieldSupplier(record.getKeyValue(ZohoConstants.ACRONYM_FIELD));
        String langAcronym = ZohoUtils.stringFieldSupplier(record.getKeyValue(ZohoConstants.LANG_ACRONYM_FIELD));
        org.setAcronym(ZohoUtils.createLanguageMapOfStringList(langAcronym, acronym));
        org.setLogo(ZohoUtils.stringFieldSupplier(record.getKeyValue(ZohoConstants.LOGO_LINK_TO_WIKIMEDIACOMMONS_FIELD)));
        org.setHomepage(ZohoUtils.stringFieldSupplier(record.getKeyValue(ZohoConstants.WEBSITE_FIELD)));
        List<String> organizationRoleStringList = ZohoUtils.stringListSupplier(record.getKeyValue(ZohoConstants.ORGANIZATION_ROLE_FIELD));
        if (!organizationRoleStringList.isEmpty()) {
            org.setEuropeanaRole(ZohoUtils.createLanguageMapOfStringList(Locale.ENGLISH.getLanguage(), organizationRoleStringList));
        }
        org.setOrganizationDomain(ZohoUtils.createMapWithLists(Locale.ENGLISH.getLanguage(), ZohoUtils.stringFieldSupplier(record.getKeyValue(ZohoConstants.DOMAIN_FIELD))));
        
        List<String> geographicLevel = ZohoUtils.stringListSupplier(record.getKeyValue(ZohoConstants.GEOGRAPHIC_LEVEL_FIELD));
        if(!geographicLevel.isEmpty()) {
                org.setGeographicLevel(ZohoUtils.createMap(Locale.ENGLISH.getLanguage(), geographicLevel.get(0)));
        }
        
        String organizationCountry = this.toEdmCountry(ZohoUtils.stringFieldSupplier(record.getKeyValue(ZohoConstants.ORGANIZATION_COUNTRY_FIELD)));
        org.setCountry(organizationCountry);
        List<String> sameAs = getAllSameAs(record);
        if (!sameAs.isEmpty()) {
            org.setSameAs(sameAs);

        }
        
        Address address = new Address();
        address.setAbout(org.getAbout() + ZohoConstants.ADDRESS_ABOUT);
        address.setVcardStreetAddress(ZohoUtils.stringFieldSupplier(record.getKeyValue(ZohoConstants.STREET_FIELD)));
        address.setVcardLocality(ZohoUtils.stringFieldSupplier(record.getKeyValue(ZohoConstants.CITY_FIELD)));
        address.setVcardCountryName(ZohoUtils.stringFieldSupplier(record.getKeyValue(ZohoConstants.ADDRESS_COUNTRY_FIELD)));
        address.setVcardPostalCode(ZohoUtils.stringFieldSupplier(record.getKeyValue(ZohoConstants.ZIP_CODE_FIELD)));
        address.setVcardPostOfficeBox(ZohoUtils.stringFieldSupplier(record.getKeyValue(ZohoConstants.PO_BOX_FIELD)));
        org.setAddress(address);

        return org;
    }
    
    private Map<String, List<String>> getAllAltLabel(Record record) {
        Map<String, List<String>> altLabelMap = new HashMap<>();
        for(int i = 0; i < LANGUAGE_CODE_LENGTH; i++) {
            String label = ZohoUtils.stringFieldSupplier(record.getKeyValue(ZohoConstants.ALTERNATIVE_FIELD+ "_" + i));
            if (label != null) {
                String lang = ZohoConstants.LANG_ALTERNATIVE_FIELD + "_" + i;
                String isoLanguage = toIsoLanguage(ZohoUtils.stringFieldSupplier(record.getKeyValue(lang)));
                 altLabelMap.put(isoLanguage, Collections.singletonList(label));
            }
        }
        return altLabelMap;
    }
    
    private List<String> getAllSameAs(Record record) {
        List<String> sameAsList = new ArrayList<>();
        for(int i = 0; i < SAME_AS_CODE_LENGTH; i++) {
            String sameAs = ZohoUtils.stringFieldSupplier(record.getKeyValue(ZohoConstants.SAME_AS_FIELD + "_" + i));
            if (sameAs != null) {
                sameAsList.add(sameAs);
            }
        }
        return sameAsList;
    }

    String toEdmCountry(String organizationCountry) {
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