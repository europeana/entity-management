package eu.europeana.entitymanagement.batch.model;

import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.definitions.batch.model.TaskType;
import eu.europeana.entitymanagement.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.ENTITY_UPDATE_STATUS;
/**
 * Entity Update statistics class
 * @author srishti singh
 * @since 15 September 2025
 */
@Component(ENTITY_UPDATE_STATUS)
public class EntityUpdateStats {

    private static final Logger logger = LogManager.getLogger(EntityUpdateStats.class);

    private TaskType taskType;
    private AtomicInteger totalEntitiesForUpdate = new AtomicInteger();
    private AtomicInteger agents = new AtomicInteger();
    private AtomicInteger concepts = new AtomicInteger();
    private AtomicInteger timespans = new AtomicInteger();
    private AtomicInteger places = new AtomicInteger();
    private AtomicInteger failed = new AtomicInteger();

    public void reset() {
        totalEntitiesForUpdate.set(0);
        agents.set(0);
        concepts.set(0);
        timespans.set(0);
        places.set(0);
        failed.set(0);
    }

    public String getTaskType() {
        return TaskType.full_update == this.taskType ? "update" : "metrics update";
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public void addEntityUpdated() {
        totalEntitiesForUpdate.getAndIncrement();
    }

    public void updateEntityByType(BatchEntityRecord entityRecord) {
        try {
            switch (EntityTypes.getByEntityType(entityRecord.getEntityRecord().getEntity().getType())) {
                case Agent:    agents.incrementAndGet(); break;
                case Concept:  concepts.incrementAndGet(); break;
                case Place:    places.incrementAndGet(); break;
                case TimeSpan: timespans.incrementAndGet(); break;
                case Aggregator:
                case ConceptScheme:
                case Organization:
                  //skip organizations and concept schemes
                  break;
                default:
                  break;
            }
        } catch (UnsupportedEntityTypeException e) {
            logger.info("Unknown type of entity found in the DB {}", e.getMessage(), e);
        }
    }

    public void addFailed() {
        failed.incrementAndGet();
    }

    public int getAgents() {
        return agents.get();
    }

    public int getTotalEntitiesForUpdate() {
        return totalEntitiesForUpdate.get();
    }

    public int getConcepts() {
        return concepts.get();
    }

    public int getTimespans() {
        return timespans.get();
    }

    public int getPlaces() {
        return places.get();
    }

    public int getFailed() {
        return failed.get();
    }
}
