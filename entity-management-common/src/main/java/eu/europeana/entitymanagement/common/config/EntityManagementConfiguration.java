package eu.europeana.entitymanagement.common.config;

public interface EntityManagementConfiguration {

    /**
     * uses entity.environment property
     */
    public String getJwtTokenSignatureKey();

    public String getAuthorizationApiName();

    String getDatasourcesXMLConfig();

    String getMetisBaseUrl();

    String getLanguagecodesXMLConfig();

    String getHitsQuery();

    String getEnrichmentsQuery();

    String getSearchApiSolrUrl();

    String getPrSolrUrl();
}
