package eu.europeana.entitymanagement.batch.errorhandling;

import dev.morphia.Datastore;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class EntityUpdateFailureRepository {

  private final Datastore datastore;

  @Autowired
  public EntityUpdateFailureRepository(
      @Qualifier(AppConfigConstants.BEAN_EM_DATA_STORE) Datastore datastore) {
    this.datastore = datastore;
  }

  public EntityUpdateFailure save(EntityUpdateFailure failure) {
    return datastore.save(failure);
  }

  public List<EntityUpdateFailure> saveBulk(List<EntityUpdateFailure> failures){
    return datastore.save(failures);
  }

}
