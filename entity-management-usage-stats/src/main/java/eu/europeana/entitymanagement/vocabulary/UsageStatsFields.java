package eu.europeana.entitymanagement.vocabulary;

/**
 * Usage Statistics Fields Constants
 *
 * @author Srishti Singh (srishti.singh@europeana.eu)
 * @since 2021-07-29
 */
public class UsageStatsFields {

    // Metric Constants
    public static final String OVERALL_TOTAL_TYPE = "OverallTotal";
    public static final String TYPE               = "type";
    public static final String CREATED            = "created";
    public static final String ENTITIES_PER_LANG_TYPE  = "entities";
    public static final String ENTITY_LANG   = "language";
    public static final String TIMESPAN      = "timespan";
    public static final String PLACE         = "place";
    public static final String ORGANISATION  = "organization";
    public static final String CONCEPT       = "concept";
    public static final String AGENT         = "agent";
    public static final String TOTAL         = "total";

    // entity api constants
    public static final String ENTITY_API_BASE_URL = "https://api.europeana.eu/entity/search.json";
    public static final String ENTITY_API_PREFLABEL_SEARCH_QUERY = "query=skos_prefLabel.";
    public static final String ENTITY_API_PREFLABEL_QUERY_SEPERATOR = ":*";
    public static final String ENTITY_API_SEARCH_ALL_QUERY = "query=*";
    public static final String ENTITY_API_SEARCH_PFPS = "&profile=facets&facet=type&pageSize=0";

}
