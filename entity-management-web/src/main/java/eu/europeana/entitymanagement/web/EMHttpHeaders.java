package eu.europeana.entitymanagement.web;

// TODO: similar class exists in the set-api, consider moving to the api-commons
public class EMHttpHeaders {

  public static final String VALUE_NO_CAHCHE_STORE_REVALIDATE =
      "no-cache, no-store, must-revalidate";
  public static final String VALUE_BASIC_CONTAINER =
      "<http://www.w3.org/ns/ldp#BasicContainer>; rel=\"type\"";

  public static final String PREFERENCE_APPLIED = "Preference-Applied";
  public static final String VARY = "Vary";
  public static final String ETAG = "ETag";
  public static final String CACHE_CONTROL = "Cache-Control";
}
