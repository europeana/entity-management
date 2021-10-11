package eu.europeana.entitymanagement.batch.repository;

import dev.morphia.Datastore;
import eu.europeana.entitymanagement.batch.entity.SequenceGenerator;
import org.springframework.stereotype.Repository;

@Repository
public class SequenceRepository extends AbstractRepository {
  public SequenceRepository(Datastore datastore) {
    super(datastore);
  }

  @Override
  public void drop() {
    dropCollection(SequenceGenerator.class);
  }
}
