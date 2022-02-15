package eu.europeana.entitymanagement.web.auth;

import eu.europeana.api.commons.web.model.vocabulary.Operations;

public abstract class EMOperations implements Operations {

  public static final String OPERATION_ZOHO_SYNC = "zoho_sync";
  public static final String OPERATION_PERM_DELETE = "permanentDelete";
  
  private EMOperations() {};
}
