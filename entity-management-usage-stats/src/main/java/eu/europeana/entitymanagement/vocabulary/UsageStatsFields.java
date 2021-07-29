package eu.europeana.entitymanagement.vocabulary;

/**
 * Usage Statistics Fields Constants
 *
 * @author Srishti Singh (srishti.singh@europeana.eu)
 * @since 2021-07-29
 */
public class UsageStatsFields {

    public static final String OVERALL_TOTAL_TYPE = "OverallTotal";
    public static final String TYPE               = "type";
    public static final String CREATED            = "created";
    public static final String ENTITY_PER_TYPE_PER_LANG  = "entities";
    public static final String ENTITY_LANG  = "lang";
    public static final String ENTITY_LANG_VALUES  = "values";
    public static final String ENTITY_LANG_COUNT  = "count";
    public static final String ENTITY_LANG_TYPE  = "type";

    // entity api constants
    public static final String ENTITY_API_BASE_URL = "https://api.europeana.eu/entity/search.json";
    public static final String ENTITY_API_SEARCH_QUERY = "query=skos_prefLabel.";
    public static final String ENTITY_API_SEARCH_PFPS = ":*&profile=facets&facet=type&pageSize=0";

}
