package eu.europeana.entity.utils;

public class Constants {

    /**
     * Contexts
     */
    public static final String CONTEXT_ENTITY = "http://www.europeana.eu/schemas/context/entity.jsonld";

    public static final String JSONLD_FORMAT = "jsonld";
    /**
     * Media type for json-ld
     */
    public static final String MEDIA_TYPE_JSONLD = "application/ld+json";


    public static final String TYPE_CONCEPT = "Concept";
    public static final String ID_PREFIX_CONCEPT = "http://data.europeana.eu/agent/concept";

    public static final String ID_PREFIX_FORMATTER = "http://data.europeana.eu/%s/%s#aggregation";


    public static String europeanaId(String type, String identifier) {
        return String.format(ID_PREFIX_FORMATTER, type, identifier);
    }
}
