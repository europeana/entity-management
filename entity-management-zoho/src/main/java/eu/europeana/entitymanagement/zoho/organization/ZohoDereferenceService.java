package eu.europeana.entitymanagement.zoho.organization;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoho.crm.api.record.Record;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.model.Aggregator;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.dereference.Dereferencer;
import eu.europeana.entitymanagement.zoho.utils.ZohoConstants;
import eu.europeana.entitymanagement.zoho.utils.ZohoException;
import eu.europeana.entitymanagement.zoho.utils.ZohoUtils;

@Service
public class ZohoDereferenceService implements Dereferencer {

  private final ZohoConfiguration zohoConfiguration;
  private final EntityManagementConfiguration emConfig;

  @Autowired
  public ZohoDereferenceService(ZohoConfiguration zohoConfiguration, EntityManagementConfiguration emConfig) {
    this.zohoConfiguration = zohoConfiguration;
    this.emConfig = emConfig;
  }
  /**
   * Method to dereference organizations by zohoRecordId
   * @see #dereferenceEntityById(String)
   * @param zohoRecordId record id in zoho
   * @return the dereferenced organization/aggregators as option
   * @throws Exception if errors occur during dereferencing
   */
  public Optional<Entity> dereferenceOrganizationByZohoRecordId(@NonNull Long zohoRecordId) throws Exception {
    String url = ZohoUtils.buildZohoRecordUrl(zohoConfiguration.getZohoBaseUrlOrganizations(), zohoRecordId);
    return dereferenceEntityById(url);
  }  
  

  @Override
  public Optional<Entity> dereferenceEntityById(@NonNull String url) throws Exception {

    Optional<Record> zohoOrganization =
        zohoConfiguration.getZohoAccessClient().getZohoOrganizationByUrl(url);

    //if the org is Aggregator, fetch additionally the Aggregator info
    Optional<Record> zohoAggregator =
        zohoConfiguration.getZohoAccessClient().getZohoAggregatorByOrgUrl(url);

    if(zohoOrganization.isEmpty()){
      return Optional.empty();
    }
    
    //enable when you need to print the data for debuging purposes System.out.println(serialize(zohoOrganization.get())); 
    Organization org = createOrganizationFromZohoRecords(zohoOrganization.get(), zohoAggregator);
    fillAggregatedVia(org, zohoOrganization);
    return Optional.of(org);
  }  
  
  public String serialize(Record zohoRecord) throws JsonProcessingException {
    ObjectMapper mapper =
        new Jackson2ObjectMapperBuilder()
            .defaultUseWrapper(false)
            .dateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX"))
            .featuresToEnable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .build();
    mapper.findAndRegisterModules();
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(zohoRecord.getKeyValues());
  }
  
  private Organization createOrganizationFromZohoRecords(Record zohoOrganization, 
      Optional<Record> zohoAggregator) {
    
    if(zohoOrganization == null){
      return null;
    }
    boolean isAggregator = zohoAggregator.isPresent();
    Organization org= isAggregator? new Aggregator() : new Organization();
    if(isAggregator) {
        //fill aggregator properties
        ZohoOrganizationConverter.fillAggregatorInfoFromZohoRecord((Aggregator)org, zohoAggregator.get(), zohoConfiguration.getZohoBaseUrlAggregators());
    }
    //fill common organization properties
    ZohoOrganizationConverter.fillOrganizationInfoFromZohoRecord(org, zohoOrganization,
          zohoConfiguration.getZohoBaseUrlOrganizations(), emConfig.getCountryMappings(), emConfig.getRoleMappings());
    
    return org;
  }
  
  private void fillAggregatedVia(Organization org, Optional<Record> zohoOrganization) throws ZohoException {
    if(org!=null && zohoOrganization.isPresent()) {
      String aggregName = ZohoOrganizationConverter.getStringFieldValue(zohoOrganization.get(), ZohoConstants.AGGREGATORS);
      String orgName = ZohoOrganizationConverter.getStringFieldValue(zohoOrganization.get(), ZohoConstants.ACCOUNT_NAME_FIELD);
      if(StringUtils.isBlank(aggregName) || StringUtils.isBlank(orgName)) {
        //organization has no aggregator, nothing to do
        return;
      }
        
      //if the organization has aggregator
        /*
         * search zoho organization that has the same name as the aggregator,
         * and get its europeana id, to store in the aggregatedVia
         */
        fillAggregatedViaByAggregatorName(org, aggregName);
        
        //if the aggregatedVia field is still not set
        if(org.getAggregatedVia()==null) {
          //search the aggregatedVia/From zoho module for the aggregator id
          fillAggregatedViaByZohoModule(org, aggregName, orgName);
        }
    }  
  }
  void fillAggregatedViaByZohoModule(Organization org, String aggregName, String orgName)
      throws ZohoException {
    String aggregatorUrl=null;
    Optional<Record> zohoLinking =
        zohoConfiguration.getZohoAccessClient().searchZohoAggregatedViaModule(orgName, aggregName);
    if(zohoLinking.isPresent()) {
      Record aggregRecord = ZohoOrganizationConverter.getSubRecord(zohoLinking.get(), ZohoConstants.AGGREGATORS);
      if(aggregRecord != null) {
        aggregatorUrl = ZohoUtils.buildZohoRecordUrl(zohoConfiguration.getZohoBaseUrlAggregators(), aggregRecord.getId());
      }
    }
    if(aggregatorUrl==null) {
      /*
       * in this case since the organization is aggregator and we cannot set the aggregatedVia field,
       * we throw an exception in order to execute the org update task later on again
       */
      throw new ZohoException("Could not set the aggregatedVia field for the Zoho organization which has its aggregator.");
    } 
    org.setAggregatedViaAggregatorUrls(List.of(aggregatorUrl));
  }
  
  void fillAggregatedViaByAggregatorName(Organization org, String aggregName) throws ZohoException {
    Optional<Record> zohoAggregatorOrg =
        zohoConfiguration.getZohoAccessClient().searchZohoOrganizationByName(aggregName);
    if(zohoAggregatorOrg.isPresent()) {
      String europeanaId = ZohoOrganizationConverter.getStringFieldValue(zohoAggregatorOrg.get(), ZohoConstants.EUROPEANA_ID_FIELD);
      if(europeanaId!=null) {
        List<String> aggregatedVia = new ArrayList<String>();
        aggregatedVia.add(europeanaId);
        org.setAggregatedVia(aggregatedVia);
      }
    }
  }
}
