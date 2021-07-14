package eu.europeana.entitymanagement.utils;

import eu.europeana.entitymanagement.definitions.exceptions.EntityCreationException;
import eu.europeana.entitymanagement.definitions.exceptions.EntityManagementRuntimeException;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.definitions.model.Timespan;
import eu.europeana.entitymanagement.schemaorg.model.SchemaOrgEntity;
import eu.europeana.entitymanagement.schemaorg.model.SchemaOrgPlace;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.web.xml.model.XmlAgentImpl;
import eu.europeana.entitymanagement.web.xml.model.XmlBaseEntityImpl;
import eu.europeana.entitymanagement.web.xml.model.XmlConceptImpl;
import eu.europeana.entitymanagement.web.xml.model.XmlOrganizationImpl;
import eu.europeana.entitymanagement.web.xml.model.XmlPlaceImpl;
import eu.europeana.entitymanagement.web.xml.model.XmlTimespanImpl;
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

	private static final Map<EntityTypes, Class<? extends XmlBaseEntityImpl<?>>> xmlEntityMap = Map.of(
			EntityTypes.Agent, XmlAgentImpl.class,
			EntityTypes.Concept, XmlConceptImpl.class,
			EntityTypes.Organization, XmlOrganizationImpl.class,
			EntityTypes.Place, XmlPlaceImpl.class,
			EntityTypes.Timespan, XmlTimespanImpl.class
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

	@SuppressWarnings("unchecked")
	public static <T extends Entity> SchemaOrgEntity<T> createSchemaOrgEntity(Entity entity) {
		switch (EntityTypes.valueOf(entity.getType())) {
			case Place:
				return (SchemaOrgEntity<T>) new SchemaOrgPlace((Place) entity);

			//TODO: convert other types
			default:
				throw new EntityManagementRuntimeException(String.format("Encountered invalid entityType %s in entityId=%s",
						entity.getType(), entity.getEntityId()));
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends XmlBaseEntityImpl<?>> T createXmlEntity(Entity entity)
			throws EntityManagementRuntimeException {
		// we have to explicitly instantiate the Xml instances, as getDeclaredConstructor().newInstance(args)
		// wouldn't work

		switch (EntityTypes.valueOf(entity.getType())){
			case Agent:
				return (T) new XmlAgentImpl((Agent)entity);
			case Place:
				return (T) new XmlPlaceImpl((Place) entity);
			case Concept:
				return (T) new XmlConceptImpl((Concept) entity);
			case Timespan:
				return (T) new XmlTimespanImpl((Timespan) entity);
			case Organization:
				return (T) new XmlOrganizationImpl((Organization) entity);

			default:
				throw new EntityManagementRuntimeException(String.format("Encountered invalid entityType %s in entityId=%s",
						entity.getType(), entity.getEntityId()));
		}
	}
	
	public static Entity createNewEntity(Entity entity)
                        throws EntityManagementRuntimeException {
	    
	        switch (EntityTypes.valueOf(entity.getType())){
                        case Agent:
                                return new Agent((Agent)entity);
                        case Place:
                                return new Place((Place) entity);
                        case Concept:
                                return new Concept((Concept) entity);
                        case Timespan:
                                return new Timespan((Timespan) entity);
                        case Organization:
                                return new Organization((Organization) entity);

                        default:
                                throw new EntityManagementRuntimeException(String.format("Encountered invalid entityType %s in entityId=%s",
                                                entity.getType(), entity.getEntityId()));
                }
        }
}
