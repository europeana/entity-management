package eu.europeana.entitymanagement.web.model;

public class ZohoSyncReportFields {

  //mongo field names
  public static final String START_DATE = "startDate";
  
  
  //web field names
  public static final String LAST_SYNC_DATE = "lastSyncDate";
  public static final String NEW = "new";
  public static final String ENABLED = "enabled";
  public static final String UPDATED = "updated";
  public static final String DEPRECATED = "deprecated";
  public static final String DELETED = "deleted";
  public static final String SUBMITTED_ZOHO_EUROPEANA_ID = "submittedZohoEuropeanaID";
  public static final String EXECUTION_STATUS = "executionStatus";
  public static final String FAILED = "failed";
  public static final String ID = "id";
  public static final String ERROR = "error";
  public static final String MESSAGE = "message";
  public static final String TRACE = "trace";

  public static final String CREATION_ERROR = "Entity creation error";
  public static final String UPDATE_ERROR = "Entity scheduled update error";
  public static final String ENABLE_ERROR = "Entity enable error";
  public static final String SOLR_DELETION_ERROR = "Solr deletion error";
  public static final String ENTITY_DELETION_ERROR = "Entity deletion error";
  public static final String ENTITY_DEPRECATION_ERROR = "Entity deprecation error";
  public static final String ENTITY_SYNCHRONOUS_UPDATE_ERROR = "Entity synchronous update error";
  public static final String ZOHO_ACCESS_ERROR = "Zoho access error";
  public static final String ZOHO_UPDATE_ERROR = "Zoho update error";
  
  public static final String STATUS_COMPLETED = "completed";
  public static final String STATUS_INCOMPLETE = "incomplete";

}
