package eu.europeana.entitymanagement.util;

import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.definitions.model.Timespan;
import eu.europeana.entitymanagement.definitions.model.impl.*;
import eu.europeana.entitymanagement.exception.EntityCreationException;

/**
 * Instantiates a {@link eu.europeana.entitymanagement.definitions.model.impl.BaseEntity} instance, based on
 * the {@link eu.europeana.entitymanagement.web.xml.model.XmlBaseEntityImpl} instance
 */
public class BaseEntityFactory {

    public static BaseEntity createEntityFromXmlType(Class<? extends BaseEntity> xmlBaseEntityClass) throws EntityCreationException {
        if (xmlBaseEntityClass.isAssignableFrom(Concept.class)) {
            return new BaseConcept();
        }

        if (xmlBaseEntityClass.isAssignableFrom(Timespan.class)) {
            return new BaseTimespan();
        }

        if (xmlBaseEntityClass.isAssignableFrom(Place.class)) {
            return new BasePlace();
        }

        if (xmlBaseEntityClass.isAssignableFrom(Agent.class)) {
            return new BaseAgent();
        }

        if (xmlBaseEntityClass.isAssignableFrom(Organization.class)) {
            return new BaseOrganization();
        }

        //TODO: add other types
        throw new EntityCreationException("No matching BaseEntityImplementation for XML type " + xmlBaseEntityClass);
    }
}
