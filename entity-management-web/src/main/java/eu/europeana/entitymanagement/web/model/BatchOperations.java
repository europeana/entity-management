package eu.europeana.entitymanagement.web.model;

import eu.europeana.api.commons.web.model.vocabulary.Operations;
import eu.europeana.entitymanagement.exception.FunctionalRuntimeException;
import java.util.SortedSet;
import java.util.TreeSet;

public class BatchOperations {

  private SortedSet<Operation> createOperations = new TreeSet<>();
  private SortedSet<Operation> enableOperations  = new TreeSet<>();
  private SortedSet<Operation> updateOperations = new TreeSet<>();
  private SortedSet<Operation> deleteOperations  = new TreeSet<>();
  private SortedSet<Operation> permanentDeleteOperations = new TreeSet<>();

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
        createOperations.add(operation);
        break;

      case Operations.ENABLE:
        enableOperations.add(operation);
        break;

      case Operations.UPDATE:
        updateOperations.add(operation);
        break;

      case Operations.DELETE:
        deleteOperations.add(operation);
        break;

      case Operations.PERMANENT_DELETE:
        permanentDeleteOperations.add(operation);
        break;

      default:
        throw new FunctionalRuntimeException(
            "Unsuported zoho sync operation: " + operation.getAction());
    }
  }
}
