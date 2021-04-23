package eu.europeana.entitymanagement.batch.errorhandling;

import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EntityUpdateFailureService {

  private final EntityUpdateFailureRepository failureRepository;
  private static final Logger logger = LogManager.getLogger(EntityUpdateFailureService.class);

  @Autowired
  public EntityUpdateFailureService(
      EntityUpdateFailureRepository failureRepository) {
    this.failureRepository = failureRepository;
  }

  public void persistFailure(String entityId, Exception e) {
    EntityUpdateFailure savedFailure = failureRepository.save(
        createUpdateFailure(entityId, Instant.now(), e.getMessage(),
            ExceptionUtils.getStackTrace(e)));

    logger.debug("Persisted update failure to db: {}", savedFailure);
  }

  public void persistFailureBulk(List<? extends EntityRecord> entityRecords, Exception e) {
    String message = e.getMessage();
    String stackTrace = ExceptionUtils.getStackTrace(e);
    Instant now = Instant.now();

    List<EntityUpdateFailure> failures = entityRecords.stream()
        .map(r -> createUpdateFailure(r.getEntityId(), now, message, stackTrace)).collect(
            Collectors.toList());

   List<EntityUpdateFailure> savedFailures =  failureRepository.saveBulk(failures);
    logger.debug("Persisted update failures to db: {}", Arrays.toString(savedFailures.stream().map(EntityUpdateFailure::getEntityId)
        .toArray(String[]::new)));
  }


  private EntityUpdateFailure createUpdateFailure(String entityId, Instant time, String message,
      String stacktrace) {

    return new EntityUpdateFailure.Builder(entityId)
        .created(time)
        .message(message)
        .stackTrace(stacktrace)
        .build();
  }
}
