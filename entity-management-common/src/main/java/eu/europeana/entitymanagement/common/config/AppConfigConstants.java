package eu.europeana.entitymanagement.common.config;

public class AppConfigConstants {

  public static final String BEAN_EM_XML_SERIALIZER = "emXmlSerializer";
  public static final String BEAN_EM_JSONLD_SERIALIZER = "emJsonldSerializer";
  public static final String BEAN_EM_DATA_SOURCES = "emDataSources";
  public static final String BEAN_EM_LANGUAGE_CODES = "emLanguageCodes";
  public static final String BEAN_EM_VALIDATOR_FACTORY = "emValidatorFactory";
  public static final String BEAN_EM_ENTITY_FIELD_CLEANER = "emEntityFieldCleaner";
  public static final String BEAN_EM_ENTITY_FIELD_DATATYPE_VALIDATION =
      "emEntityFieldDatatypeValidation";
  public static final String BEAN_EM_SCORING_SERVICE = "emScoringService";
  public static final String BEAN_CLIENT_DETAILS_SERVICE = "clientDetailsService";
  public static final String BEAN_ENTITY_RECORD_REPO = "emEntityRecordRepo";
  public static final String BEAN_ENTITY_RECORD_SERVICE = "emEntityRecordService";
  public static final String BEAN_ZOHO_SYNC_SERVICE = "emZohoSyncService";
 
  public static final String BEAN_EM_DATA_STORE = "emDataStore";
  public static final String BEAN_BATCH_DATA_STORE = "batchDataStore";
  public static final String BEAN_METIS_DEREF_SERVICE = "metisDerefService";
  public static final String BEAN_EM_BUILD_INFO = "emBuildInfo";
  public static final String BEAN_AUTHORIZATION_SERVICE = "authorizationService";
  public static final String SCHEDULED_UPDATE_TASK_EXECUTOR = "scheduledUpdateTaskExecutor";
  public static final String SCHEDULED_REMOVAL_TASK_EXECUTOR = "scheduledRemovalTaskExecutor";
  public static final String UPDATES_STEP_EXECUTOR = "updatesStepExecutor";
  public static final String REMOVALS_STEP_EXECUTOR = "removalsStepExecutor";
  public static final String WEB_REQUEST_JOB_EXECUTOR = "webRequestTaskExecutor";
  public static final String ENTITY_UPDATE_JOB_LAUNCHER = "entityUpdateJobLauncher";
  public static final String ENTITY_REMOVALS_JOB_LAUNCHER = "entityRemovalsJobLauncher";
  public static final String SYNC_WEB_REQUEST_JOB_LAUNCHER = "synchronousWebRequestJobLauncher";

  public static final String PERIODIC_UPDATES_SCHEDULER = "periodicUpdatesScheduler";
  public static final String PERIODIC_REMOVALS_SCHEDULER = "periodicRemovalsScheduler";

  public static final String BEAN_XML_MAPPER = "emXmlMapper";
  public static final String BEAN_JSON_MAPPER = "emJsonMapper";

  public static final String METIS_DEREF_PATH = "/dereference";

  public static final String ENTITY_RECORD_CTX_KEY = "entityRecordCtx";

  public static final String BEAN_EM_SOLR_SERVICE = "emSolrService";
  public static final String BEAN_INDEXING_SOLR_CLIENT = "indexingSolrClient";
  public static final String BEAN_PR_SOLR_CLIENT = "prSolrClient";

  public static final String BEAN_SOLR_ENTITY_SUGGESTER_FILTER = "solrEntityFilter";

  public static final String BEAN_ZOHO_ACCESS_CONFIGURATION = "zohoAccessConfiguration";
  public static final String BEAN_WIKIDATA_ACCESS_SERVICE = "wikidataAccessService";
  public static final String BEAN_WIKIDATA_ACCESS_DAO = "wikidataAccessDao";
}
