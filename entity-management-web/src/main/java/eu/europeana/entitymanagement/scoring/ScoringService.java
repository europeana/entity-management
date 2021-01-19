package eu.europeana.entitymanagement.scoring;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.scoring.model.EntityMetrics;

public interface ScoringService {

    public EntityMetrics computeMetrics (Entity entity);
    
}
