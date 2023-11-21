package eu.europeana.entitymanagement.zoho.organization;

import java.util.Optional;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import com.zoho.crm.api.record.Record;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.dereference.Dereferencer;

@Service
public class ZohoDereferenceService implements Dereferencer {

  private final ZohoAccessConfiguration zohoAccessConfiguration;

  public ZohoDereferenceService(ZohoAccessConfiguration zohoAccessConfiguration) {
    this.zohoAccessConfiguration = zohoAccessConfiguration;
  }

  @Override
  public Optional<Entity> dereferenceEntityById(@NonNull String id) throws Exception {

    Optional<Record> zohoOrganization =
        zohoAccessConfiguration.getZohoAccessClient().getZohoRecordOrganizationById(id);
    //    Gson resp = new Gson();
    //    System.out.println(resp.toJson(zohoOrganization.get().getKeyValues()));
    //
    if(zohoOrganization.isPresent()) {
      return Optional.of(ZohoOrganizationConverter.convertToOrganizationEntity(zohoOrganization.get(), zohoAccessConfiguration.getZohoBaseUrl())); 
    } else {
      return Optional.empty();
    }
  }  
}
