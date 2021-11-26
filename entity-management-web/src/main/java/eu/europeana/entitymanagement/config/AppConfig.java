package eu.europeana.entitymanagement.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import dev.morphia.Datastore;
import eu.europeana.api.commons.oauth2.service.impl.EuropeanaClientDetailsService;
import eu.europeana.batch.config.MongoBatchConfigurer;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;

/** @author GordeaS */
@Configuration
public class AppConfig extends AppConfigConstants {

  private static final Logger LOG = LogManager.getLogger(AppConfig.class);

  @Resource private EntityManagementConfiguration emConfiguration;

  @Resource(name = BEAN_XML_MAPPER)
  private XmlMapper xmlMapper;

  @Resource(name = SCHEDULED_UPDATE_TASK_EXECUTOR)
  private TaskExecutor defaultTaskExecutor;

  public AppConfig() {
    LOG.info("Initializing EntityManagementConfiguration bean as: configuration");
  }

  @PostConstruct
  public void init() {
    if (emConfiguration.isAuthEnabled()) {
      String jwtTokenSignatureKey = emConfiguration.getApiKeyPublicKey();
      if (jwtTokenSignatureKey == null || jwtTokenSignatureKey.isBlank()) {
        throw new IllegalStateException("The jwt token signature key cannot be null or empty.");
      }
    }
  }

  @Bean(name = BEAN_EM_DATA_SOURCES)
  public DataSources getDataSources() throws IOException {
    String datasourcesXMLConfigFile = emConfiguration.getDatasourcesXMLConfig();

    DataSources dataSources;
    try (InputStream inputStream = getClass().getResourceAsStream(datasourcesXMLConfigFile)) {
      assert inputStream != null;
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
        String contents = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        dataSources = xmlMapper.readValue(contents, DataSources.class);
      }
    }

    if (dataSources.getEuropeanaDatasource().isEmpty()) {
      throw new IllegalStateException(
          String.format(
              "Datasource must be configured with id='%s' in %s",
              DataSource.EUROPEANA_ID, datasourcesXMLConfigFile));
    }

    return dataSources;
  }

  @Bean(name = BEAN_CLIENT_DETAILS_SERVICE)
  public EuropeanaClientDetailsService getClientDetailsService() {
    EuropeanaClientDetailsService clientDetailsService = new EuropeanaClientDetailsService();
    clientDetailsService.setApiKeyServiceUrl(emConfiguration.getApiKeyUrl());
    return clientDetailsService;
  }

  /**
   * Configures Spring Batch to use Mongo
   *
   * @param datastore Morphia datastore for Spring Batch
   * @return BatchConfigurer instance
   */
  @Bean
  public MongoBatchConfigurer mongoBatchConfigurer(
      @Qualifier(BEAN_BATCH_DATA_STORE) Datastore datastore) {
    return new MongoBatchConfigurer(datastore, defaultTaskExecutor);
  }

  @Bean
  public ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
    return new ShallowEtagHeaderFilter();
  }
}
