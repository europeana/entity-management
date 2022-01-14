package eu.europeana.entitymanagement.batch.reader;

import org.springframework.batch.item.data.AbstractPaginatedDataItemReader;

public abstract class BaseDatabaseReader<T> extends AbstractPaginatedDataItemReader<T> {

  private final int readerPageSize;

  protected BaseDatabaseReader(int pageSize) {
    this.readerPageSize = pageSize;
  }

  abstract String getClassName();

  @Override
  protected void doOpen() throws Exception {
    super.doOpen();
    // Non-restartable, as we expect this to run in multi-threaded steps.
    // see: https://stackoverflow.com/a/20002493
    setSaveState(false);
    setPageSize(readerPageSize);
    setName(getClassName());
  }
}
