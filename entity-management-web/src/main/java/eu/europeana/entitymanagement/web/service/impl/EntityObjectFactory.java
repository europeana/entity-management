package eu.europeana.entitymanagement.web.service.impl;

import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.definitions.model.Timespan;
import eu.europeana.entitymanagement.definitions.model.impl.AgentImpl;
import eu.europeana.entitymanagement.definitions.model.impl.ConceptImpl;
import eu.europeana.entitymanagement.definitions.model.impl.OrganizationImpl;
import eu.europeana.entitymanagement.definitions.model.impl.PlaceImpl;
import eu.europeana.entitymanagement.definitions.model.impl.TimespanImpl;
import eu.europeana.entitymanagement.exception.EntityCreationException;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

/**
 * Instantiates a
 * {@link eu.europeana.entitymanagement.definitions.model.impl.Entity} instance,
 * based on the
 * {@link eu.europeana.entitymanagement.web.xml.model.XmlBaseEntityImpl}
 * instance
 */
public class EntityObjectFactory {

//    @Deprecated
//    public static Entity createEntityFromXmlType(Class<? extends Entity> xmlBaseEntityClass)
//	    throws EntityCreationException {
//	if (xmlBaseEntityClass.isAssignableFrom(Concept.class)) {
//	    return new ConceptImpl();
//	}
//
//	if (xmlBaseEntityClass.isAssignableFrom(Timespan.class)) {
//	    return new TimespanImpl();
//	}
//
//	if (xmlBaseEntityClass.isAssignableFrom(Place.class)) {
//	    return new PlaceImpl();
//	}
//
//	if (xmlBaseEntityClass.isAssignableFrom(Agent.class)) {
//	    return new AgentImpl();
//	}
//
//	if (xmlBaseEntityClass.isAssignableFrom(Organization.class)) {
//	    return new OrganizationImpl();
//	}
//
//	// TODO: add other types
//	throw new EntityCreationException("No matching BaseEntityImplementation for XML type " + xmlBaseEntityClass);
//    }

    public static Class<? extends Entity> getClassForType(EntityTypes modelType) {

	Class<? extends Entity> ret = null;
//	EntityTypes entityType = EntityTypes.valueOf(modelType.name());

	switch (modelType) {
	case Organization:
	    ret = OrganizationImpl.class;
	    break;
	case Concept:
	    ret = ConceptImpl.class;
	    break;
	case Agent:
	    ret = AgentImpl.class;
	    break;
	case Place:
	    ret = PlaceImpl.class;
	    break;
	case Timespan:
	    ret = TimespanImpl.class;
	    break;
	default:
	    throw new RuntimeException("The given type is not supported by the web model");
	}

	return ret;
    }
    
    public static Entity createEntityObjectFromCopy(Entity copy) {
    	switch (EntityTypes.valueOf(copy.getType())) {
    	case Organization:
    	    return new OrganizationImpl((Organization)copy);
    	case Concept:
    	    return new ConceptImpl((Concept)copy);
    	case Agent:
    	    return new AgentImpl((Agent)copy);
    	case Place:
    	    return new PlaceImpl((Place)copy);
    	case Timespan:
    	    return new TimespanImpl((Timespan)copy);
    	default:
    	    throw new RuntimeException("The given type is not supported by the web model");
    	}    	
    }

    public static Entity createEntityObject(EntityTypes entityType) throws EntityCreationException {

	try {
	    return getClassForType(entityType).getDeclaredConstructor().newInstance();

	} catch (Exception e) {
	    throw new EntityCreationException(entityType.toString(), e);
	}
    }
    
    public static Entity createEntityObject(String entityType) throws EntityCreationException {
	
	return createEntityObject(EntityTypes.valueOf(entityType));
}
}
