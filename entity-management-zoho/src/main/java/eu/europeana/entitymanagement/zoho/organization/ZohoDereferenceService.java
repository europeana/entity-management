package eu.europeana.entitymanagement.zoho.organization;

import com.zoho.crm.api.record.Record;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.dereference.Dereferencer;
import java.util.Optional;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class ZohoDereferenceService implements Dereferencer {

  private final ZohoAccessConfiguration zohoAccessConfiguration;

  public ZohoDereferenceService(
      ZohoAccessConfiguration zohoAccessConfiguration) {
    this.zohoAccessConfiguration = zohoAccessConfiguration;
  }

  @Override
  public Optional<Entity> dereferenceEntityById(@NonNull String id) throws Exception{

    Optional<Record> zohoOrganization = zohoAccessConfiguration.getZohoAccessClient()
        .getZohoRecordOrganizationById(id);

    return zohoOrganization.map(ZohoOrganizationConverter::convertToOrganizationEntity);
  }
}
