package eu.europeana.entitymanagement.utils;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.europeana.corelib.edm.model.schemaorg.ContextualEntity;
import eu.europeana.corelib.edm.model.schemaorg.EdmOrganization;
import eu.europeana.corelib.edm.model.schemaorg.GeoCoordinates;
import eu.europeana.corelib.edm.model.schemaorg.MultilingualString;
import eu.europeana.corelib.edm.model.schemaorg.Person;
import eu.europeana.corelib.edm.model.schemaorg.PostalAddress;
import eu.europeana.corelib.edm.model.schemaorg.Reference;
import eu.europeana.corelib.edm.model.schemaorg.SchemaOrgConstants;
import eu.europeana.corelib.edm.model.schemaorg.Text;
import eu.europeana.corelib.edm.model.schemaorg.Thing;
import eu.europeana.corelib.utils.EuropeanaUriUtils;
import eu.europeana.entitymanagement.definitions.model.Address;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.definitions.model.Timespan;

public final class SchemaOrgUtils {

    private static final Logger LOG = LogManager.getLogger(SchemaOrgUtils.class);

    private static final String URL_PREFIX = "http://data.europeana.eu";
    private static final String PLACE_PREFIX = "http://data.europeana.eu/place";
    private static final String TIMESPAN_PREFIX = "http://semium.org";
    private static final String UNIT_CODE_E37 = "E37";

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy",
            Locale.ENGLISH);
    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);

    private SchemaOrgUtils() {
        // empty constructor prevent initialization
    }

    /**
     * Update properties of the given Schema.Org Thing using data from the given EDM Entity
     *
     * @param entity	EDM Entity
     * @param contextualEntity	Schema.Org Entity to update
     */
    public static void  processEntity(Entity entity, ContextualEntity contextualEntity) {

        if (entity instanceof Concept) {
            SchemaOrgUtils.processConcept((Concept) entity, contextualEntity);
        } else if (entity instanceof Place) {
            SchemaOrgUtils.processPlace((Place) entity, contextualEntity);
        } else if (entity instanceof Organization) {
            SchemaOrgUtils.processOrganization((Organization) entity, contextualEntity);
        } else if (entity instanceof Agent) {
            SchemaOrgUtils.processAgent((Agent) entity, contextualEntity);
        } else if (entity instanceof Timespan) {
            SchemaOrgUtils.processTimespan((Timespan) entity, contextualEntity);
        }
    }

    /**
     * Fill the properties of the Schema.Org Thing from equivalent attributes of EDM
     * concept.
     *
     * @param concept       source EDM concept
     * @param conceptObject Schema.Org Contextual Entity object in which the
     *                      properties will be filled in
     */
    public static void processConcept(Concept concept, ContextualEntity conceptObject) {
        // @id
        conceptObject.setId(concept.getAbout());
        // name
        addMultilingualProperties(conceptObject, SchemaOrgConstants.PROPERTY_NAME, concept.getPrefLabel());

        // alternateName
        addMultilingualProperties(conceptObject, concept.getAltLabel(), SchemaOrgConstants.PROPERTY_ALTERNATE_NAME);

        // description
        addMultilingualProperties(conceptObject, concept.getNote(), SchemaOrgConstants.PROPERTY_DESCRIPTION);

        // sameAs
        addTextProperties(conceptObject, concept.getExactMatch(), SchemaOrgConstants.PROPERTY_SAME_AS);

        // url
        addEntityPageUrl(concept, conceptObject, SchemaOrgConstants.ENTITY_PAGE_URL_CONCEPT_TYPE);

        // image
        if(concept.getDepiction()!=null) conceptObject.setImage(concept.getDepiction().getId());

    }

    private static void addEntityPageUrl(Entity entity,
                                         eu.europeana.corelib.edm.model.schemaorg.ContextualEntity conceptObject, String entityPageType) {
        if (StringUtils.startsWithIgnoreCase(entity.getAbout(), URL_PREFIX)) {
            String entityPageUrl = String.format(SchemaOrgConstants.ENTITY_PAGE_URL_PATTERN, entityPageType,
                    entity.getEntityIdentifier());
            conceptObject.setEntityPageUrl(entityPageUrl);
        }
    }

    /**
     * Update the properties of the given Schema.Org Place using data from EDM place
     *
     * @param edmPlace    source EDM place
     * @param placeObject Schema.Org Contextual Entity object to update
     */
    public static void processPlace(Place edmPlace, ContextualEntity placeObject) {
        if (edmPlace == null) {
            return;
        }

        // @id
        placeObject.setId(edmPlace.getAbout());

        // name
        addMultilingualProperties(placeObject, SchemaOrgConstants.PROPERTY_NAME, edmPlace.getPrefLabel());

        // alternateName
        addMultilingualProperties(placeObject, edmPlace.getAltLabel(), SchemaOrgConstants.PROPERTY_ALTERNATE_NAME);

        // geo
        createGeoCoordinates(edmPlace, (eu.europeana.corelib.edm.model.schemaorg.Place) placeObject);

        // description
        addMultilingualProperties(placeObject, edmPlace.getNote(), SchemaOrgConstants.PROPERTY_DESCRIPTION);

        // containsPlace
        addReferences(placeObject, edmPlace.getHasPart(), SchemaOrgConstants.PROPERTY_CONTAINS_PLACE,
        		eu.europeana.corelib.edm.model.schemaorg.Place.class, null);

        // containedInPlace
        addReferences(placeObject, edmPlace.getIsPartOfArray(), SchemaOrgConstants.PROPERTY_CONTAINED_IN_PLACE, eu.europeana.corelib.edm.model.schemaorg.Place.class, null);

        // sameAs
        addTextProperties(placeObject, edmPlace.getOwlSameAs(), SchemaOrgConstants.PROPERTY_SAME_AS);

        // url
        //not available yet
//        addEntityPageUrl(edmPlace, placeObject, SchemaOrgConstants.ENTITY_PAGE_URL_PLACE_TYPE);

        // image
        if(edmPlace.getDepiction()!=null) placeObject.setImage(edmPlace.getDepiction().getId());
    }

    /**
     * Create GeoCoordinates object for the latitude, longitude and altitude taken
     * from EDM Place and set the corresponding properties in Schema.Org Place.
     *
     * @param edmPlace    EDM Place with necessary data
     * @param placeObject Schema.Org Place object to update
     */
    private static void createGeoCoordinates(Place edmPlace,
    		eu.europeana.corelib.edm.model.schemaorg.Place placeObject) {
        GeoCoordinates geoCoordinates = new GeoCoordinates();

        // latitude
        if (edmPlace.getLatitude() != null) {
            geoCoordinates.addLatitude(new Text(String.valueOf(edmPlace.getLatitude())));
        }
        // TODO Try to retrieve the latitude from position if possible

        // longitude
        if (edmPlace.getLongitude() != null) {
            geoCoordinates.addLongitude(new Text(String.valueOf(edmPlace.getLongitude())));
        }
        // TODO Try to retrieve the longitude from position if possible

        // elevation
        if (edmPlace.getAltitude() != null) {
            geoCoordinates.addElevation(new Text(String.valueOf(edmPlace.getAltitude())));
        }

        // geo
        placeObject.addGeo(geoCoordinates);
    }

    /**
     * Update properties of the given Schema.Org Thing (Person or Organization)
     * using data from the given EDM Agent
     *
     * @param agentObject Schema.Org Contextual Entity object to update
     * @param agent       source EDM agent
     */
    public static void processAgent(Agent agent, ContextualEntity agentObject) {
        // @id
        agentObject.setId(agent.getAbout());

        // name
        addMultilingualProperties(agentObject, SchemaOrgConstants.PROPERTY_NAME, agent.getPrefLabel());
        addMultilingualProperties(agentObject, SchemaOrgConstants.PROPERTY_NAME, agent.getName());

        // alternateName
        addMultilingualProperties(agentObject, agent.getAltLabel(), SchemaOrgConstants.PROPERTY_ALTERNATE_NAME);

        // description
        addMultilingualProperties(agentObject, agent.getNote(), SchemaOrgConstants.PROPERTY_DESCRIPTION);
        addMultilingualProperties(agentObject, agent.getBiographicalInformation(),
                SchemaOrgConstants.PROPERTY_DESCRIPTION);

        if (agentObject instanceof Person) {

            // birthDate
            if (agent.getDateOfBirth() != null) {
                addStringProperties(agentObject, agent.getDateOfBirth(),
                        SchemaOrgConstants.PROPERTY_BIRTH_DATE);
            } else if (agent.getBegin() != null) {
                addStringProperties(agentObject, agent.getBegin(), SchemaOrgConstants.PROPERTY_BIRTH_DATE);
            }

            // deathDate
            if (agent.getDateOfDeath() != null) {
                addStringProperties(agentObject, agent.getDateOfDeath(),
                        SchemaOrgConstants.PROPERTY_DEATH_DATE);
            } else if (agent.getEnd() != null) {
                addStringProperties(agentObject, agent.getEnd(), SchemaOrgConstants.PROPERTY_DEATH_DATE);
            }

            // gender
            addStringProperty(agentObject, agent.getGender(), SchemaOrgConstants.PROPERTY_GENDER);

            // jobTitle
            addStringProperties(agentObject, agent.getProfessionOrOccupation(),
                    SchemaOrgConstants.PROPERTY_JOB_TITLE);

            // birthPlace
            addResourceOrReferenceProperties(agentObject, agent.getPlaceOfBirth(),
                    SchemaOrgConstants.PROPERTY_BIRTH_PLACE, eu.europeana.corelib.edm.model.schemaorg.Place.class, null);

            // deathPlace
            addResourceOrReferenceProperties(agentObject, agent.getPlaceOfDeath(),
                    SchemaOrgConstants.PROPERTY_DEATH_PLACE, eu.europeana.corelib.edm.model.schemaorg.Place.class, null);
        }

        if (agentObject instanceof eu.europeana.corelib.edm.model.schemaorg.Organization) {
            // foundingDate
            if (agent.getDateOfEstablishment() != null) {
                addStringProperty(agentObject, agent.getDateOfEstablishment(),
                        SchemaOrgConstants.PROPERTY_FOUNDING_DATE);
            }

            // dissolutionDate
            if (agent.getDateOfTermination()!= null) {
                addStringProperty(agentObject, agent.getDateOfTermination(),
                        SchemaOrgConstants.PROPERTY_DISSOLUTION_DATE);
            }
        }

        // sameAs
        addTextProperties(agentObject, agent.getOwlSameAs(), SchemaOrgConstants.PROPERTY_SAME_AS);

        // url
        addEntityPageUrl(agent, agentObject, SchemaOrgConstants.ENTITY_PAGE_URL_AGENT_TYPE);

        // image
        if(agent.getDepiction()!=null)	agentObject.setImage(agent.getDepiction().getId());
        
    }

    /**
     * Update properties of the given Schema.Org Thing (EdmOrganization)
     * using data from the given EDM Organization
     *
     * @param entityObject Schema.Org Contextual Entity object to update
     * @param organization source EDM Organization
     */
    public static void processOrganization(Organization organization, ContextualEntity entityObject) {
        // @id
        entityObject.setId(organization.getAbout());

        // name
        addMultilingualProperties(entityObject, SchemaOrgConstants.PROPERTY_NAME, organization.getPrefLabel());

        // alternateName
        addMultilingualProperties(entityObject, organization.getAltLabel(), SchemaOrgConstants.PROPERTY_ALTERNATE_NAME);
        addMultilingualProperties(entityObject, organization.getAcronym(), SchemaOrgConstants.PROPERTY_ALTERNATE_NAME);

        // description
        addMultilingualProperties(entityObject, SchemaOrgConstants.PROPERTY_DESCRIPTION, organization.getDescription());

        // mainEntityOfPage
        addTextProperties(entityObject, Arrays.asList(organization.getHomepage()), SchemaOrgConstants.PROPERTY_MAIN_ENTITY_OF_PAGE);

        // logo
        addTextProperties(entityObject, Arrays.asList(organization.getLogo()), SchemaOrgConstants.PROPERTY_LOGO);

        // telephone
        addTextProperties(entityObject, organization.getPhone(), SchemaOrgConstants.PROPERTY_TELEPHONE);

        // address
        createPostalAddress(organization, (EdmOrganization) entityObject);

        // identifier
        addTextProperties(entityObject, organization.getIdentifier(), SchemaOrgConstants.PROPERTY_IDENTIFIER);

        // sameAs
        addTextProperties(entityObject, organization.getOwlSameAs(), SchemaOrgConstants.PROPERTY_SAME_AS);

        // url
        addEntityPageUrl(organization, entityObject, SchemaOrgConstants.ENTITY_PAGE_URL_ORGANIZATION_TYPE);

        // image
        if(organization.getDepiction()!=null) entityObject.setImage(organization.getDepiction().getId());

    }

    /**
     * Update properties of the given Schema.Org entity
     * using data from the given EDM Timespan
     *
     * @param timespanObject Schema.Org Contextual Entity object to update
     * @param time           source EDM timespan
     */
    public static void processTimespan(Timespan time, ContextualEntity timespanObject) {
        // @id
        timespanObject.setId(time.getAbout());

        // name
        addMultilingualProperties(timespanObject, SchemaOrgConstants.PROPERTY_NAME, time.getPrefLabel());

        // alternateName
        addMultilingualProperties(timespanObject, time.getAltLabel(), SchemaOrgConstants.PROPERTY_ALTERNATE_NAME);

        // description
        addMultilingualProperties(timespanObject, time.getNote(), SchemaOrgConstants.PROPERTY_DESCRIPTION);

        //url
        addEntityPageUrl(time, timespanObject, SchemaOrgConstants.ENTITY_PAGE_URL_TIMESPAN_TYPE);

        // sameAs
        addTextProperties(timespanObject, time.getOwlSameAs(), SchemaOrgConstants.PROPERTY_SAME_AS);

    }

    /**
     * Create PostalAddress object for the properties taken
     * from EDM Organization and set the corresponding properties in Schema.Org EdmOrganization.
     *
     * @param organization       EDM Organization with necessary data
     * @param organizationObject Schema.Org EdmOrganization object to update
     */
    private static void createPostalAddress(Organization organization,
                                            EdmOrganization organizationObject) {
        PostalAddress postalAddress = new PostalAddress();

        Address address = organization.getAddress();
        if (address == null)
            return;

        // id
        postalAddress.setId(address.getAbout());

        // streetAddress
        addTextProperties(postalAddress, Arrays.asList(address.getVcardStreetAddress()), SchemaOrgConstants.PROPERTY_STREET_ADDRESS);

        // postalCode
        addTextProperties(postalAddress, Arrays.asList(address.getVcardPostalCode()), SchemaOrgConstants.PROPERTY_POSTAL_CODE);

        // postOfficeBoxNumber
        addTextProperties(postalAddress, Arrays.asList(address.getVcardPostOfficeBox()), SchemaOrgConstants.PROPERTY_POST_OFFICE_BOX_NUMBER);

        // addressLocality
        addTextProperties(postalAddress, Arrays.asList(address.getVcardLocality()), SchemaOrgConstants.PROPERTY_ADDRESS_LOCALITY);

        // addressCountry
        addTextProperties(postalAddress, Arrays.asList(address.getVcardCountryName()), SchemaOrgConstants.PROPERTY_ADDRESS_COUNTRY);

        // postalAddress
        organizationObject.addAddress(postalAddress);
    }

    /**
     * Adds multilingual string properties for all values lists in the given map.
     *
     * @param object       object for which the property values will be added
     * @param map          map of language to list of values to be added
     * @param propertyName name of property
     */
    private static void addMultilingualProperties(Thing object, Map<String, List<String>> map, String propertyName) {
        if (map == null) {
            return;
        }
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            addMultilingualProperties(object, entry.getValue(),
                    SchemaOrgConstants.DEFAULT_LANGUAGE.equals(entry.getKey()) ? "" : entry.getKey(), propertyName);
        }
    }

    private static void addMultilingualProperties(Thing object, String propertyName, Map<String, String> map) {
        if (map == null) {
            return;
        }
        String language = null;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            language = SchemaOrgConstants.DEFAULT_LANGUAGE.equals(entry.getKey()) ? "" : entry.getKey();
            addMultilingualProperty(object, entry.getValue(), language, propertyName);
        }
    }

    /**
     * Adds multilingual string properties to property named with
     * <code>propertyName</code> for all values present in the values list. The
     * given language will be used for each value.
     *
     * @param object       object for which the property values will be added
     * @param values       values to be added
     * @param language     language used for each value
     * @param propertyName name of property
     */
    private static void addMultilingualProperties(Thing object, List<String> values, String language,
                                                  String propertyName) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            if (notNullNorEmpty(value)) {
                addMultilingualProperty(object, value, language, propertyName);
            }
        }
    }

    private static void addMultilingualProperty(Thing object, String value, String language, String propertyName) {
        MultilingualString property = new MultilingualString();
        property.setLanguage(language);
        property.setValue(value);
        object.addProperty(propertyName, property);
    }
 
    /**
     * Adds string properties to property named with
     * <code>propertyName</code> for all values present in the values list. The
     * given language will be used for each value.
     *
     * @param object       object for which the property values will be added
     * @param values       values to be added
     * @param propertyName name of property
     */
    private static void addStringProperties(Thing object, List<String> values, String propertyName) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            if (notNullNorEmpty(value)) {
                addStringProperty(object, value, propertyName);
            }
        }
    }

    private static void addStringProperty(Thing object, String value, String propertyName) {
        object.addProperty(propertyName, new Text(value));
    }
    
    private static void addResourceOrReferenceProperties(Thing object, List<String> entry,
                                                         String propertyName, Class<? extends Thing> referenceClass, List<String> linkedContextualEntities) {
        if (entry == null) {
            return;
        }

        for (String value : entry) {
            if (EuropeanaUriUtils.isUri(value)) {
                //while creating reference for about, contributor, creator, publisher reference class should be null.
                // Only for values NOT starting with http://data.europeana.eu
                if (referenceNull(propertyName, value)) {
                    referenceClass = null;
                } else {
                    // for value starting with http://data.europeana.eu, assign correct reference class
                    referenceClass = getReferenceClass(value);
                }
                addProperty(object, value, "", propertyName, referenceClass, linkedContextualEntities);
            } else {
                addResourceProperty(object, value, "", propertyName, referenceClass);
            }
        }        
    }

    /**
     * Adds references for all values in the array. References may have a specific
     * type if <code>referenceClass</code> is specified.
     *
     * @param object                   object for which the properties will be added
     * @param values                   values to be added as references
     * @param propertyName             name of property
     * @param referenceClass           class of reference
     * @param linkedContextualEntities list of all the references in the object
     */
    private static void addReferences(Thing object, List<String> values, String propertyName,
                                      Class<? extends Thing> referenceClass, List<String> linkedContextualEntities) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            addLinkedContextualEntities(value, linkedContextualEntities);
            addReference(object, value, propertyName, referenceClass);
        }
    }

    /**
     * Checks whether value is Uri adding a reference in this case or adding the
     * multilingual string otherwise.
     *
     * @param object                   object for which the property will be added
     * @param propertyName             name of property
     * @param language                 language string, may be empty
     * @param value                    value to add
     * @param linkedContextualEntities list of all the references in the object
     */
    private static void addProperty(Thing object, String value, String language, String propertyName,
                                    Class<? extends Thing> referenceClass, List<String> linkedContextualEntities) {
        if (notNullNorEmpty(value)) {
            if (EuropeanaUriUtils.isUri(value)) {
                addLinkedContextualEntities(value, linkedContextualEntities);
                addReference(object, value, propertyName, referenceClass);
            } else {
                addMultilingualProperty(object, value,
                        SchemaOrgConstants.DEFAULT_LANGUAGE.equals(language) ? "" : language, propertyName);
            }
        }
    }

    /**
     * Creates a general (not typed) reference and adds it to the specified object
     * as a property value of the given property name.
     *
     * @param object         object for which the reference will be added
     * @param id             id of the reference
     * @param propertyName   name of property
     * @param referenceClass class of reference that should be used for Reference
     *                       object
     */
    private static void addReference(Thing object, String id, String propertyName,
                                     Class<? extends Thing> referenceClass) {
        Reference reference = new Reference(referenceClass);
        reference.setId(id);
        object.addProperty(propertyName, reference);
    }

    /**
     * Creates a reference object, adds the multilingual string to the reference and
     * adds the reference to the object.
     *
     * @param object         object for which the property will be added
     * @param propertyName   name of property
     * @param language       language string, may be empty
     * @param value          value to add
     * @param referenceClass class of reference that should be used for Reference
     *                       object
     */
    private static void addResourceProperty(Thing object, String value, String language, String propertyName,
                                            Class<? extends Thing> referenceClass) {
        if (notNullNorEmpty(value)) {
            Thing resource = instantiateResourceObject(referenceClass);
            if (StringUtils.equals(language, SchemaOrgConstants.DEFAULT_LANGUAGE)) {
                resource.addProperty(SchemaOrgConstants.PROPERTY_NAME, new Text(value));
            } else {
                addMultilingualProperty(resource, value,
                        language, SchemaOrgConstants.PROPERTY_NAME);
            }
            object.addProperty(propertyName, resource);
        }
    }

    /**
     * Adds Text properties from the given values. Those are language independent
     *
     * @param object       Thing object to update
     * @param values       string values to put under property with the given
     *                     property name
     * @param propertyName name of property
     */
    private static void addTextProperties(Thing object, List<String> values, String propertyName) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            if (notNullNorEmpty(value)) {
                object.addProperty(propertyName, new Text(value));
            }
        }
    }

    /*
     * Instantiates the reference class object, otherwise a Thing object will be
     * instantiate.
     *
     * @param referenceClass class to instantiate
     */
    private static Thing instantiateResourceObject(Class<? extends Thing> referenceClass) {
        if (referenceClass == null)
            return new Thing();
        Thing resource = null;
        try {
            resource = referenceClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            resource = new Thing();
            LOG.info("Cannot instantiate object of class {} . Instance of Thing is used instead!",
                    referenceClass.getCanonicalName());
        }
        return resource;
    }

    /**
     * Checks whether the given parameter is neither null nor empty
     *
     * @param value value to check
     * @return true when the given value is neither null nor empty, false otherwise
     */
    private static boolean notNullNorEmpty(String value) {
        return value != null && !value.isEmpty();
    }

    /**
     * To check if reference class will be null for the property
     * Only for propertyValue NOT starting with http://data.europeana.eu
     *
     * @param propertyName  property to be checked
     * @param propertyValue property value to be checked
     */
    private static boolean referenceNull(String propertyName, String propertyValue) {
        if (!StringUtils.startsWith(propertyValue, URL_PREFIX)) {
            return (StringUtils.equals(propertyName, SchemaOrgConstants.PROPERTY_ABOUT)
                    || StringUtils.equals(propertyName, SchemaOrgConstants.PROPERTY_CONTRIBUTOR)
                    || StringUtils.equals(propertyName, SchemaOrgConstants.PROPERTY_CREATOR)
                    || StringUtils.equals(propertyName, SchemaOrgConstants.PROPERTY_PUBLISHER));
        }
        return false;
    }

    /**
     * get the reference class for the property value
     * starting with http://data.europeana.eu
     *
     * @param propertyValue property value to be checked
     */
    private static Class<? extends Thing> getReferenceClass(String propertyValue) {
        if (StringUtils.startsWith(propertyValue, URL_PREFIX)) {
            if (propertyValue.startsWith(URL_PREFIX + "/agent")) {
                return Person.class;
            }
            if (propertyValue.startsWith(URL_PREFIX + "/place")) {
                return eu.europeana.corelib.edm.model.schemaorg.Place.class;
            }
            if (propertyValue.contains(URL_PREFIX + "/concept")) {
                return Thing.class;
            }
            if (propertyValue.contains(URL_PREFIX + "/organization")) {
                return EdmOrganization.class;
            }
            // any other unrecognised entity, consider it as a Thing
            return Thing.class;
        }
        // return null for every other case
        return null;
    }

    /**
     * Add values to the linkedContextualEntities list
     *
     * @param value value to be added
     */
    private static void addLinkedContextualEntities(String value, List<String> linkedContextualEntities) {
        if (linkedContextualEntities != null) {
            linkedContextualEntities.add(value);
        }
    }
}
