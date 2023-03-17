package eu.europeana.entitymanagement.vocabulary;

public interface ConceptSchemeSolrFields extends EntitySolrFields {

  public static final String DEFINITION = "skos_definition";
  public static final String DEFINITION_ALL = DEFINITION + DYNAMIC_FIELD_SEPARATOR + "*";

}
