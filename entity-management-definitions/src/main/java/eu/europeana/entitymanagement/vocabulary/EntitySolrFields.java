package eu.europeana.entitymanagement.vocabulary;

public interface EntitySolrFields {

  public static final String DYNAMIC_FIELD_SEPARATOR = ".";
  public static final String ID = "id";
  public static final String TYPE = "type";
  public static final String RIGHTS = "rights";
  public static final String DEPICTION = "foaf_depiction";
  public static final String NOTE = "skos_note";
  public static final String NOTE_ALL = NOTE + DYNAMIC_FIELD_SEPARATOR + "*";

  @Deprecated
  /** @deprecated should be removed when old suggester is removed * */
  public static final String PREF_LABEL = "skos_prefLabel";

  public static final String PREF_LABEL_ALL = PREF_LABEL + DYNAMIC_FIELD_SEPARATOR + "*";
  public static final String ALT_LABEL = "skos_altLabel";
  public static final String ALT_LABEL_ALL = ALT_LABEL + DYNAMIC_FIELD_SEPARATOR + "*";
  public static final String HIDDEN_LABEL = "skos_hiddenLabel";
  public static final String HIDDEN_LABEL_NO_LANG = HIDDEN_LABEL + DYNAMIC_FIELD_SEPARATOR;
  public static final String HIDDEN_LABEL_ALL = HIDDEN_LABEL + DYNAMIC_FIELD_SEPARATOR + "*";
  public static final String LABEL_ENRICH_GENERAL = "label_enrich";
  public static final String LABEL_ENRICH_ALL =
      LABEL_ENRICH_GENERAL + DYNAMIC_FIELD_SEPARATOR + "*";
  public static final String SAME_AS = "owl_sameAs";
  public static final String IDENTIFIER = "dc_identifier";
  public static final String IS_RELATED_TO = "edm_isRelatedTo";
  public static final String HAS_PART = "dcterms_hasPart";
  public static final String IS_PART_OF = "dcterms_isPartOf";

  public static final String EUROPEANA_DOC_COUNT = "europeana_doc_count";
  public static final String PAGERANK = "pagerank";
  public static final String DERIVED_SCORE = "derived_score";

  public static final String TIMESTAMP = "timestamp";
  public static final String CREATED = "created";
  public static final String MODIFIED = "modified";

  public static final String IN_SCHEME = "skos_inScheme";
  public static final String SUGGEST_FILTERS = "suggest_filters";
  public static final String SUGGEST_FILTER_EUROPEANA = "europeana";

  public static final String IS_SHOWN_BY = "isShownBy";
  public static final String IS_SHOWN_BY_ALL = IS_SHOWN_BY + DYNAMIC_FIELD_SEPARATOR + "*";
  public static final String IS_SHOWN_BY_ID = "isShownBy.id";
  public static final String IS_SHOWN_BY_SOURCE = "isShownBy.source";
  public static final String IS_SHOWN_BY_THUMBNAIL = "isShownBy.thumbnail";

  public static final String PAYLOAD = "payload";

  // fields for Timespans and Agents
  public static final String BEGIN = "edm_begin";
  public static final String END = "edm_end";

}
