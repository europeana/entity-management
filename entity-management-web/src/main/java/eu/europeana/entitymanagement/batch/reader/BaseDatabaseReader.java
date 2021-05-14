package eu.europeana.entitymanagement.batch.reader;

import org.springframework.batch.item.data.AbstractPaginatedDataItemReader;

public abstract class BaseDatabaseReader<T> extends AbstractPaginatedDataItemReader<T> {

  public BaseDatabaseReader(int pageSize) {
    setPageSize(pageSize);
    // Non-restartable, as we expect this to run in multi-threaded steps.
    // see: https://stackoverflow.com/a/20002493
    setSaveState(false);
  }

  abstract String getClassName();

  @Override
  protected void doOpen() throws Exception {
    super.doOpen();
    setName(getClassName());
  }
}
