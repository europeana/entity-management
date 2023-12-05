package eu.europeana.entitymanagement.zoho.organization;

import java.io.IOException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import com.zoho.crm.api.record.Record;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.dereference.Dereferencer;

@Service
public class ZohoDereferenceService implements Dereferencer {

  private final ZohoOrganizationConverter zohoOrgConverter;
  private final ZohoConfiguration zohoConfiguration;

  @Autowired
  public ZohoDereferenceService(ZohoConfiguration zohoConfiguration, ZohoOrganizationConverter zohoOrgConverter) throws IOException {
    this.zohoConfiguration = zohoConfiguration;
    this.zohoOrgConverter=zohoOrgConverter;
  }

  @Override
  public Optional<Entity> dereferenceEntityById(@NonNull String id) throws Exception {

    Optional<Record> zohoOrganization =
        zohoConfiguration.getZohoAccessClient().getZohoRecordOrganizationById(id);
    //    Gson resp = new Gson();
    //    System.out.println(resp.toJson(zohoOrganization.get().getKeyValues()));
    //
    if(zohoOrganization.isPresent()) {
      return Optional.of(zohoOrgConverter.convertToOrganizationEntity(zohoOrganization.get())); 
    } else {
      return Optional.empty();
    }
  }

}
