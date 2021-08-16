package eu.europeana.entitymanagement.wikidata;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.exceptions.EntityCreationException;
import eu.europeana.entitymanagement.definitions.model.Address;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.web.xml.model.WikidataOrganization;
import eu.europeana.entitymanagement.web.xml.model.XmlOrganizationImpl;
import eu.europeana.entitymanagement.zoho.utils.Constants;
import eu.europeana.entitymanagement.zoho.utils.WikidataAccessException;
import eu.europeana.entitymanagement.zoho.utils.ZohoUtils;

/**
 * Wikidata Access Service class
 *
 * @author Srishti Singh (srishti.singh@europeana.eu)
 * @since 2021-07-06
 */
public class WikidataAccessService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WikidataAccessService.class);
    private final WikidataAccessDao wikidataAccessDao;

    public WikidataAccessService(WikidataAccessDao wikidataAccessDao) {
        this.wikidataAccessDao = wikidataAccessDao;
    }

    protected WikidataAccessDao getWikidataAccessDao() {
        return this.wikidataAccessDao;
    }

    public URI buildOrganizationUri(String organizationId) {
        String contactsSearchUrl = String.format("%s%s", Constants.WIKIDATA_BASE_URL, organizationId);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(contactsSearchUrl);
        return builder.build().encode().toUri();
    }

    public XmlOrganizationImpl dereference(String wikidataUri) throws WikidataAccessException, EntityCreationException {
        StringBuilder wikidataXml = null;
        WikidataOrganization wikidataOrganization = null;
        try {
            wikidataXml = this.getWikidataAccessDao().getEntity(wikidataUri);
            wikidataOrganization = this.getWikidataAccessDao().parse(wikidataXml.toString());
        } catch (JAXBException var5) {
            LOGGER.debug("Cannot parse wikidata response: {}", wikidataXml);
            throw new WikidataAccessException("Cannot parse wikidata xml response for uri: " + wikidataUri, var5);
        }

        return wikidataOrganization == null ? null : wikidataOrganization.getOrganization();
    }

    public WikidataOrganization parseWikidataOrganization(File inputFile) throws JAXBException {
        return this.wikidataAccessDao.parseWikidataOrganization(inputFile);
    }

    public void saveXmlToFile(String xml, File contentFile) throws WikidataAccessException {
        try {
            boolean wasFileCreated = contentFile.createNewFile();
            if (!wasFileCreated) {
                LOGGER.warn("Content file existed, it will be overwritten: {}", contentFile.getAbsolutePath());
            }
            FileUtils.write(contentFile, xml, StandardCharsets.UTF_8.name());
        } catch (IOException var4) {
            throw new WikidataAccessException("XML could not be written to a file.", var4);
        }
    }

    public void mergePropsFromWikidata(Organization organization, XmlOrganizationImpl wikidataOrganization) throws EntityCreationException {
		Organization wikidataOrganizationEntity = wikidataOrganization.toEntityModel();
	    Map<String, List<String>> addToAltLabelMap = new HashMap<String, List<String>>();
	
	    /*
	     * TODO: for the prefLabel field decide which values to take
	     */
	    if (wikidataOrganizationEntity.getAltLabel() != null) {
	        Map<String, List<String>> allWikidataAltLabels = ZohoUtils.mergeMapsWithLists(wikidataOrganizationEntity.getAltLabel(), addToAltLabelMap);
	        Map<String, List<String>> mergedAltLabelMap = ZohoUtils.mergeMapsWithLists(allWikidataAltLabels, organization.getAltLabel());
	        organization.setAltLabel(mergedAltLabelMap);
	    }
	    if (wikidataOrganizationEntity.getAcronym() != null) {
	        Map<String, List<String>> acronyms = ZohoUtils.mergeMapsWithLists(organization.getAcronym(), wikidataOrganizationEntity.getAcronym());
	        organization.setAcronym(acronyms);
	    }
	    if (StringUtils.isEmpty(organization.getLogo())) {
	    	organization.setLogo(wikidataOrganizationEntity.getLogo());
	    }
	    if (organization.getDepiction() == null) {
	    	organization.setDepiction(wikidataOrganizationEntity.getDepiction());
	    }
	    if (StringUtils.isEmpty(organization.getHomepage())) {
	        organization.setHomepage(wikidataOrganizationEntity.getHomepage());
	    }
	    List<String> phoneList = ZohoUtils.mergeStringLists(organization.getPhone(), wikidataOrganizationEntity.getPhone());
	    organization.setPhone(phoneList);
	    List<String> mbox = ZohoUtils.mergeStringLists(organization.getMbox(), wikidataOrganizationEntity.getMbox());
	    organization.setMbox(mbox);
	    List<String> sameAs = this.buildSameAs(organization, wikidataOrganizationEntity);
	    organization.setSameAs(sameAs);
	    organization.setDescription(wikidataOrganizationEntity.getDescription());
	    mergeAddress(organization, wikidataOrganizationEntity);
    }

	private List<String> buildSameAs(Organization organization, Organization wikidataOrganization) {
	    List<String> mergedSameAs = organization.getOwlSameAs();
	    for (int i = 0; i < wikidataOrganization.getOwlSameAs().size(); i++) {
	      mergedSameAs.add(wikidataOrganization.getOwlSameAs().get(i));
	    }
	    String wikidataResourceUri = wikidataOrganization.getAbout();
	    if (!mergedSameAs.contains(wikidataResourceUri)) {
	        mergedSameAs.add(wikidataResourceUri);
	    }
	    return mergedSameAs;
	}
	
	private void mergeAddress(Organization baseOrganization, Organization addOrganization) {
	    if (addOrganization.getAddress() != null) {
	        if (baseOrganization.getAddress() == null) {
	            baseOrganization.setAddress(new Address());
	        }	
	        Address baseAddress = baseOrganization.getAddress();
	        Address addAddress = addOrganization.getAddress();
	        if (StringUtils.isEmpty(baseAddress.getVcardCountryName()) && StringUtils.isNotEmpty(addAddress.getVcardCountryName())) {
	            baseAddress.setVcardCountryName(addAddress.getVcardCountryName());
	        }	
	        if (StringUtils.isEmpty(baseAddress.getVcardHasGeo())) {
	            baseAddress.setVcardHasGeo(addAddress.getVcardHasGeo());
	        }
	
	    }
	}
}
