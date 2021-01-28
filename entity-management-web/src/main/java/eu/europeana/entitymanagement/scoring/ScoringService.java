package eu.europeana.entitymanagement.scoring;

import java.io.IOException;

import eu.europeana.entitymanagement.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entitymanagement.definitions.model.EntityRoot;
import eu.europeana.entitymanagement.exception.FunctionalRuntimeException;
import eu.europeana.entitymanagement.scoring.model.EntityMetrics;
import eu.europeana.entitymanagement.scoring.model.MaxEntityMetrics;

public interface ScoringService {

    public EntityMetrics computeMetrics(EntityRoot entity)
	    throws FunctionalRuntimeException, UnsupportedEntityTypeException;
    
    public MaxEntityMetrics getMaxEntityMetrics() throws IOException;
    
    public EntityMetrics getMaxOverallMetrics() throws IOException;;
    
}
