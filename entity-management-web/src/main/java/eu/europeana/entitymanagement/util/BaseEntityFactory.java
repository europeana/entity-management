package eu.europeana.entitymanagement.util;

import eu.europeana.entitymanagement.definitions.model.impl.*;
import eu.europeana.entitymanagement.exception.EntityCreationException;
import eu.europeana.entitymanagement.web.xml.model.*;

/**
 * Instantiates a {@link eu.europeana.entitymanagement.definitions.model.impl.BaseEntity} instance, based on
 * the {@link eu.europeana.entitymanagement.web.xml.model.XmlBaseEntityImpl} instance
 */
public class BaseEntityFactory {

    public static BaseEntity createEntityFromXmlType(Class<? extends XmlBaseEntityImpl> xmlBaseEntityClass) throws EntityCreationException {
        if (xmlBaseEntityClass.isAssignableFrom(XmlConceptImpl.class)) {
            return new BaseConcept();
        }

        if (xmlBaseEntityClass.isAssignableFrom(XmlTimespanImpl.class)) {
            return new BaseTimespan();
        }

        if (xmlBaseEntityClass.isAssignableFrom(XmlPlaceImpl.class)) {
            return new BasePlace();
        }

        if (xmlBaseEntityClass.isAssignableFrom(XmlAgentImpl.class)) {
            return new BaseAgent();
        }

        if (xmlBaseEntityClass.isAssignableFrom(XmlOrganizationImpl.class)) {
            return new BaseOrganization();
        }

        //TODO: add other types
        throw new EntityCreationException("No matching BaseEntityImplementation for XML type " + xmlBaseEntityClass);
    }
}
