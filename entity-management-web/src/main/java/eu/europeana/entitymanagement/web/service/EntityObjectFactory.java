package eu.europeana.entitymanagement.web.service;

import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.definitions.model.Timespan;
import eu.europeana.entitymanagement.exception.EntityCreationException;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import java.util.Map;

/**
 * Instantiates a
 * {@link eu.europeana.entitymanagement.definitions.model.Entity} instance,
 * based on the
 * {@link eu.europeana.entitymanagement.web.xml.model.XmlBaseEntityImpl}
 * instance
 */
public class EntityObjectFactory {

	private static final Map<EntityTypes, Class<? extends Entity>> entityTypesClassMap = Map.of(
			EntityTypes.Agent, Agent.class,
			EntityTypes.Concept, Concept.class,
			EntityTypes.Organization, Organization.class,
			EntityTypes.Place, Place.class,
			EntityTypes.Timespan, Timespan.class
	);

	@SuppressWarnings("unchecked")
    public static <T extends Entity> T createEntityObject(EntityTypes entityType) throws EntityCreationException {

	try {
		Class<T> entityClass = (Class<T>) entityTypesClassMap.get(entityType);
		// entityClass cannot be null here as map contains all possible types
		return entityClass.getDeclaredConstructor().newInstance();

	} catch (Exception e) {
	    throw new EntityCreationException("Error creating instance for " + entityType.toString(), e);
	}
    }
    
    public static Entity createEntityObject(String entityType) throws EntityCreationException {
	
	return createEntityObject(EntityTypes.valueOf(entityType));
}
}
