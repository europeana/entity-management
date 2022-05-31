package eu.europeana.entitymanagement.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.exception.HttpBadRequestException;
import eu.europeana.entitymanagement.exception.HttpUnprocessableException;
import java.util.List;
import java.util.Optional;

@JacksonXmlRootElement(localName = "config")
public class DataSources {

  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "source")
  private List<DataSource> datasources;

  public List<DataSource> getDatasources() {
    return datasources;
  }

  /**
   * Checks if a datasource is configured for the given entity ID
   *
   * @param url url to match
   * @return true if a Datasource match is configured, false otherwise.
   */
  public boolean hasDataSource(String url) {
    return datasources.stream().anyMatch(s -> url.contains(s.getUrl()));
  }

  /**
   * Gets the configured data source for the given entity ID
   *
   * @param url entity ID
   * @return Matching data source, or empty Optional if none found
   */
  public Optional<DataSource> getDatasource(String url) {
    return datasources.stream().filter(s -> url.contains(s.getUrl())).findFirst();
  }

  public Optional<DataSource> getEuropeanaDatasource() {
    return getDatasourceById(DataSource.EUROPEANA_ID);
  }

  public Optional<DataSource> getDatasourceById(String dataSourceId) {
    return datasources.stream().filter(s -> s.getId().equals(dataSourceId)).findFirst();
  }

  public DataSource verifyDataSource(String creationRequestId, boolean allowStatic)
      throws HttpBadRequestException, HttpUnprocessableException {
    Optional<DataSource> dataSource = getDatasource(creationRequestId);
    // return 400 error if ID does not match a configured datasource
    if (dataSource.isEmpty()) {
      throw new HttpBadRequestException(
          String.format("id %s does not match a configured datasource", creationRequestId));
    }

    // return 422 error if datasource is static
    if (!allowStatic && dataSource.get().isStatic()) {
      throw new HttpUnprocessableException(
          String.format(
              "Entity registration not permitted. id %s matches a static datasource.",
              creationRequestId));
    }
    return dataSource.get();
  }
}
