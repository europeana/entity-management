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

    /**
     * Resets the values for next time
     */
    public void reset(TaskType taskType) {
        totalEntitiesForUpdate.set(0);
        agents.set(0);
        concepts.set(0);
        timespans.set(0);
        places.set(0);
        failed.set(0);
        this.taskType = taskType;
    }

    public TaskType getTaskType() {
        return this.taskType;
    }

    /**
     * Increments the totalEntitiesForUpdate
     */
    public void addEntityUpdated() {
        totalEntitiesForUpdate.getAndIncrement();
    }

    /**
     * Increments the entites values by type
     * @param entityRecord entity to be checked for type
     */
    public void updateEntityByType(BatchEntityRecord entityRecord) {
        try {
            switch (EntityTypes.getByEntityType(entityRecord.getEntityRecord().getEntity().getType())) {
                case Agent:    agents.incrementAndGet();
                break;
                case Concept:  concepts.incrementAndGet();
                break;
                case Place:    places.incrementAndGet();
                break;
                case TimeSpan: timespans.incrementAndGet();
                break;
                case Aggregator, ConceptScheme, Organization: //skip organizations and concept schemes
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
