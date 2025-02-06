package eu.europeana.entitymanagement.zoho.organization;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoho.crm.api.record.Record;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.exceptions.EntityModelCreationException;
import eu.europeana.entitymanagement.definitions.model.Aggregator;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.dereference.Dereferencer;
import eu.europeana.entitymanagement.utils.EntityObjectFactory;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.zoho.ZohoAccessClient;
import eu.europeana.entitymanagement.zoho.utils.ZohoConstants;
import eu.europeana.entitymanagement.zoho.utils.ZohoException;
import eu.europeana.entitymanagement.zoho.utils.ZohoUtils;

@Service
public class ZohoDereferenceService implements Dereferencer {

  private static final Logger LOGGER = LogManager.getLogger(ZohoAccessClient.class);

  private final ZohoConfiguration zohoConfiguration;
  private final EntityManagementConfiguration emConfig;

  @Autowired
  public ZohoDereferenceService(ZohoConfiguration zohoConfiguration,
      EntityManagementConfiguration emConfig) {
    this.zohoConfiguration = zohoConfiguration;
    this.emConfig = emConfig;
  }

  /**
   * Method to dereference organizations by zohoRecordId
   * 
   * @see #dereferenceEntityById(String)
   * @param zohoRecordId record id in zoho
   * @return the dereferenced organization/aggregators as option
   * @throws Exception if errors occur during dereferencing
   */
  public Optional<Entity> dereferenceOrganizationByZohoRecordId(@NonNull Long zohoRecordId)
      throws Exception {
    String url =
        ZohoUtils.buildZohoRecordUrl(zohoConfiguration.getZohoBaseUrlOrganizations(), zohoRecordId);
    return dereferenceEntityById(url);
  }


  @Override
  public Optional<Entity> dereferenceEntityById(@NonNull String url) throws Exception {

    Optional<Record> zohoOrganization =
        zohoConfiguration.getZohoAccessClient().getZohoOrganizationByUrl(url);

    // if the org is Aggregator, fetch additionally the Aggregator info
    Optional<Record> zohoAggregator =
        zohoConfiguration.getZohoAccessClient().getZohoAggregatorByOrgUrl(url);

    if (zohoOrganization.isEmpty()) {
      return Optional.empty();
    }

    // enable when you need to print the data for debuging purposes
    // System.out.println(serialize(zohoOrganization.get()));
    Organization org = createOrganizationFromZohoRecords(zohoOrganization.get(), zohoAggregator);
    fillAggregatedVia(org, zohoOrganization);
    return Optional.of(org);
  }

  public String serialize(Record zohoRecord) throws JsonProcessingException {
    ObjectMapper mapper = new Jackson2ObjectMapperBuilder().defaultUseWrapper(false)
        .dateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX"))
        .featuresToEnable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        .serializationInclusion(JsonInclude.Include.NON_NULL).build();
    mapper.findAndRegisterModules();
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(zohoRecord.getKeyValues());
  }

  private Organization createOrganizationFromZohoRecords(Record zohoOrganization,
      Optional<Record> zohoAggregator) throws EntityModelCreationException {

    if (zohoOrganization == null) {
      return null;
    }
    boolean isAggregator = zohoAggregator.isPresent();
    String type = isAggregator ? EntityTypes.Aggregator.getEntityType() : EntityTypes.Organization.getEntityType();
    Organization org = EntityObjectFactory.createProxyEntityObject(type);
    
    if (isAggregator) {
      // fill aggregator properties
      ZohoOrganizationConverter.fillAggregatorInfoFromZohoRecord((Aggregator) org,
          zohoAggregator.get(), zohoConfiguration.getZohoBaseUrlAggregators());
    }
    // fill common organization properties
    ZohoOrganizationConverter.fillOrganizationInfoFromZohoRecord(org, zohoOrganization,
        zohoConfiguration.getZohoBaseUrlOrganizations(), emConfig.getCountryMappings(),
        emConfig.getRoleMappings());

    return org;
  }

  private void fillAggregatedVia(Organization org, Optional<Record> zohoOrganization)
      throws ZohoException {
    if (org != null && zohoOrganization.isPresent()) {
      String aggregName = ZohoOrganizationConverter.getStringFieldValue(zohoOrganization.get(),
          ZohoConstants.AGGREGATORS);
      String orgName = ZohoOrganizationConverter.getStringFieldValue(zohoOrganization.get(),
          ZohoConstants.ACCOUNT_NAME_FIELD);
      if (StringUtils.isBlank(aggregName) || StringUtils.isBlank(orgName)) {
        // organization has no aggregator, nothing to do
        return;
      }
      
      // if the zoho organization has one Aggregator, try to fetch it by name 
      org.addAggregatedVia(getAggregatedViaByAggregatorName(org, aggregName));

      //Fallback (if multiple or not found by name), loss of performance: search the aggregatedVia/From zoho module for the aggregator id
      if (CollectionUtils.isEmpty(org.getAggregatedVia())) {
        org.addAggregatedVia(getAggregatedViaByZohoModule(org, orgName));
      }
    }
  }

  List<String> getAggregatedViaByZohoModule(Organization org, String orgName) throws ZohoException {
    List<Record> aggregatorRecords =
        zohoConfiguration.getZohoAccessClient().searchZohoAggregatedViaModule(orgName);

    if (aggregatorRecords == null || aggregatorRecords.isEmpty()) {
      return Collections.emptyList();
    }

    List<String> aggEuropeanaIDs = new ArrayList<>(aggregatorRecords.size());
    String europeanaId = null;

    for (Record record : aggregatorRecords) {
      String aggregName =
          ZohoOrganizationConverter.getStringFieldValue(record, ZohoConstants.NAME_FIELD);
      Optional<Record> zohoAggregatorOrg =
          zohoConfiguration.getZohoAccessClient().searchZohoOrganizationByName(aggregName);
      if (zohoAggregatorOrg.isPresent()) {
        europeanaId = ZohoOrganizationConverter.getStringFieldValue(zohoAggregatorOrg.get(),
            ZohoConstants.EUROPEANA_ID_FIELD);
        aggEuropeanaIDs.add(europeanaId);
      } else {
        String aggregatorZohoId =
            ZohoOrganizationConverter.getStringFieldValue(record, ZohoConstants.ID_FIELD);
        Optional<Record> organization = zohoConfiguration.getZohoAccessClient()
            .getZohoOrganizationForAggregator(aggregatorZohoId);
        if (organization.isPresent()) {
          europeanaId = ZohoOrganizationConverter.getStringFieldValue(organization.get(),
              ZohoConstants.EUROPEANA_ID_FIELD);
        }
        if (europeanaId != null) {
          aggEuropeanaIDs.add(europeanaId);
        } else if (LOGGER.isWarnEnabled()) {
          LOGGER.warn(
              "AggregatedVia is incomplete! Cannot retrieve Europeana OrgID for aggregator:{} ",
              aggregName);
        }
      }
    }
    //
    return aggEuropeanaIDs;
  }


  List<String> getAggregatedViaByAggregatorName(Organization org, String aggregName)
      throws ZohoException {
    if (containsMultipleAggregators(aggregName)) {
      // this method is used when the organization is aggregated though a single aggregator
      return Collections.emptyList();
    }
    Optional<Record> zohoAggregatorOrg =
        zohoConfiguration.getZohoAccessClient().searchZohoOrganizationByName(aggregName);
    if (zohoAggregatorOrg.isPresent()) {
      String europeanaId = ZohoOrganizationConverter.getStringFieldValue(zohoAggregatorOrg.get(),
          ZohoConstants.EUROPEANA_ID_FIELD);
      if (europeanaId != null) {
        return List.of(europeanaId);
      }
    }
    return Collections.emptyList();
  }


  boolean containsMultipleAggregators(String aggregName) {
    return aggregName.indexOf(',') > 0;
  }
}
