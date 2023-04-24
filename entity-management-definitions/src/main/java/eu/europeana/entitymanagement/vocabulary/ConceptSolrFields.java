package eu.europeana.entitymanagement.vocabulary;

public interface ConceptSolrFields extends EntitySolrFields {

  public static final String CLOSE_MATCH = "skos_closeMatch";
  public static final String BROAD_MATCH = "skos_broadMatch";
  public static final String NARROW_MATCH = "skos_narrowMatch";
  public static final String RELATED_MATCH = "skos_relatedMatch";
  public static final String EXACT_MATCH = "skos_exactMatch";
  public static final String BROADER = "skos_broader";
  public static final String RELATED = "skos_related";
  public static final String NARROWER = "skos_narrower";
  public static final String COREF = "coref";
  public static final String NOTATION = "skos_notation";
  public static final String NOTATION_ALL = NOTATION + DYNAMIC_FIELD_SEPARATOR + "*";
}
