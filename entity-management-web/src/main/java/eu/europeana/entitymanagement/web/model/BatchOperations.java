package eu.europeana.entitymanagement.web.model;

import eu.europeana.api.commons.web.model.vocabulary.Operations;
import eu.europeana.entitymanagement.exception.FunctionalRuntimeException;
import java.util.SortedSet;
import java.util.TreeSet;

public class BatchOperations {

  SortedSet<Operation> createOperations;
  SortedSet<Operation> enableOperations;
  SortedSet<Operation> updateOperations;
  SortedSet<Operation> deleteOperations;
  SortedSet<Operation> permanentDeleteOperations;

  public SortedSet<Operation> getCreateOperations() {
    return createOperations;
  }
  
  public SortedSet<Operation> getEnableOperations() {
    return enableOperations;
  }

  public SortedSet<Operation> getUpdateOperations() {
    return updateOperations;
  }

  public SortedSet<Operation> getDeleteOperations() {
    return deleteOperations;
  }

  public SortedSet<Operation> getPermanentDeleteOperations() {
    return permanentDeleteOperations;
  }

  public void addOperation(Operation operation) {
    switch (operation.getAction()) {
      case Operations.CREATE:
        if (createOperations == null) {
          createOperations = new TreeSet<Operation>();
        }
        createOperations.add(operation);
        break;

      case Operations.ENABLE:
        if (enableOperations == null) {
          enableOperations = new TreeSet<Operation>();
        }
        enableOperations.add(operation);
        break;
  
      case Operations.UPDATE:
        if (updateOperations == null) {
          updateOperations = new TreeSet<Operation>();
        }
        updateOperations.add(operation);
        break;

      case Operations.DELETE:
        if (deleteOperations == null) {
          deleteOperations = new TreeSet<Operation>();
        }
        deleteOperations.add(operation);
        break;

      case Operations.PERMANENT_DELETE:
        if (permanentDeleteOperations == null) {
          permanentDeleteOperations = new TreeSet<Operation>();
        }
        permanentDeleteOperations.add(operation);
        break;

      default:
        throw new FunctionalRuntimeException(
            "Unsuported zoho sync operation: " + operation.getAction());
    }
  }
}
