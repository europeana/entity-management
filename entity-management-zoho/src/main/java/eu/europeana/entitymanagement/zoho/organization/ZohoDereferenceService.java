package eu.europeana.entitymanagement.zoho.organization;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Optional;
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
import eu.europeana.entitymanagement.definitions.model.CountryMapping;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.dereference.Dereferencer;

@Service
public class ZohoDereferenceService implements Dereferencer {

  private final ZohoConfiguration zohoConfiguration;
  private final EntityManagementConfiguration emConfig;

  @Autowired
  public ZohoDereferenceService(ZohoConfiguration zohoConfiguration, EntityManagementConfiguration emConfig) {
    this.zohoConfiguration = zohoConfiguration;
    this.emConfig = emConfig;
  }

  @Override
  public Optional<Entity> dereferenceEntityById(@NonNull String id) throws Exception {

    Optional<Record> zohoOrganization =
        zohoConfiguration.getZohoAccessClient().getZohoRecordOrganizationById(id);
    //    Gson resp = new Gson();
    //    System.out.println(resp.toJson(zohoOrganization.get().getKeyValues()));

    if(zohoOrganization.isPresent()) {
      return Optional.of(
           ZohoOrganizationConverter.convertToOrganizationEntity(
               zohoOrganization.get(), 
               zohoConfiguration.getZohoBaseUrl(),
               emConfig.getCountryMappings())); 
    } else {
      return Optional.empty();
    }
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
    return mapper.writeValueAsString(zohoRecord.getKeyValues());
  }

}
