package eu.europeana.entitymanagement.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;

import eu.europeana.entitymanagement.batch.model.Task;
import eu.europeana.entitymanagement.batch.processor.EntityConsolidationProcessor;
import eu.europeana.entitymanagement.batch.processor.EntityDereferenceProcessor;
import eu.europeana.entitymanagement.batch.processor.EntityMetricsProcessor;
import eu.europeana.entitymanagement.batch.processor.EntityVerificationLogger;
import eu.europeana.entitymanagement.batch.writer.EntityRecordDatabaseInsertionWriter;
import eu.europeana.entitymanagement.batch.writer.EntitySolrInsertionWriter;
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import eu.europeana.api.commons.config.i18n.I18nService;
import eu.europeana.api.commons.config.i18n.I18nServiceImpl;
import eu.europeana.api.commons.oauth2.service.impl.EuropeanaClientDetailsService;
import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.model.Vocabulary;
import eu.europeana.entitymanagement.exception.ApplicationInitializationException;
import eu.europeana.entitymanagement.mongo.repository.VocabularyRepository;
import eu.europeana.entitymanagement.web.MetisDereferenceUtils;
import eu.europeana.entitymanagement.web.xml.model.RdfXmlUtils;
import eu.europeana.entitymanagement.web.xml.model.XmlBaseEntityImpl;
import eu.europeana.entitymanagement.web.xml.model.XmlConceptImpl;

/** @author GordeaS */
@Configuration
public class AppAutoconfig extends AppConfigConstants {

  private static final Logger LOG = LogManager.getLogger(AppAutoconfig.class);

  @Autowired
  ApplicationContext applicationContext;

  @Resource private EntityManagementConfiguration emConfiguration;

  @Resource(name = BEAN_XML_MAPPER)
  private XmlMapper xmlMapper;
  
  @Resource(name = BEAN_VOCABULARY_REPO)
  private VocabularyRepository vocabRepository;
  
  @Resource protected JAXBContext jaxbContext; 
  
  public AppAutoconfig() {
    LOG.info("Initializing EntityManagementConfiguration bean as: configuration");
  }

  @PostConstruct
  public void init() throws ApplicationInitializationException {
    if (emConfiguration.isAuthReadEnabled() || emConfiguration.isAuthWriteEnabled()) {
      String jwtTokenSignatureKey = emConfiguration.getApiKeyPublicKey();
      if (jwtTokenSignatureKey == null || jwtTokenSignatureKey.isBlank()) {
        throw new IllegalStateException("The jwt token signature key cannot be null or empty.");
      }
    }
    //ensure data
    ensureDatabaseInitialization();
    
  }

  public void ensureDatabaseInitialization() throws ApplicationInitializationException {
    if(vocabRepository.countRecords() < 1) {
        List<XmlBaseEntityImpl<?>> xmlEntities;
        try {
          xmlEntities = MetisDereferenceUtils.parseMetisResponseMany(
              jaxbContext.createUnmarshaller(),  emConfiguration.loadRoleVocabulary());
        } catch (Exception e) {
          throw new ApplicationInitializationException("Cannot load vocabulary from resources!", e);
        } 
        
        List<Vocabulary> roles = new ArrayList<>(xmlEntities.size());
        for(XmlBaseEntityImpl<?> xmlEntity : xmlEntities) {
          XmlConceptImpl xmlConcept = (XmlConceptImpl) xmlEntity;
          Vocabulary vocab = new Vocabulary();
          vocab.setId(xmlConcept.getAbout());
          vocab.setInScheme(RdfXmlUtils.toStringList(xmlConcept.getInScheme()));
          vocab.setPrefLabel(RdfXmlUtils.toLanguageMap(xmlConcept.getPrefLabel()));
          roles.add(vocab);
        }
        vocabRepository.saveBulk(roles);
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

  @Bean
  public ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
    return new ShallowEtagHeaderFilter();
  }
  
  @Bean(name = BEAN_I18N_SERVICE)
  public I18nService i18nService(){
    return new I18nServiceImpl();
  }
  
  @Bean(name = BEAN_MESSAGE_SOURCE)
  public MessageSource i18nMessagesSource(){
    ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
    source.setBasename("messages");
    source.setDefaultEncoding(StandardCharsets.UTF_8.name());
    return source;
  }


  public EntityRecordDatabaseInsertionWriter recordDBInsertionWriter() {
    return applicationContext.getBean(BEAN_ENTITY_RECORD_DBINSERTION_WRITER, EntityRecordDatabaseInsertionWriter.class);
  }

  public EntitySolrInsertionWriter entitySolrInsertionWriter() {
    return applicationContext.getBean(BEAN_ENTITY_SOLR_INSERTION_WRITER, EntitySolrInsertionWriter.class);
  }


  /**
   * Creating it as a bean as the writer list is same for all the Internal Task of EM.
   * For performance will access them from application context than creating a list for every request
   * @see <a href="http://docs.google.com/document/d/16k9PcCMFwl2LXjnnzotZRPc-QqM-Ar1D0VELHt4t_hA/edit?tab=t.0#heading=h.fj6e15rbq64q"></a> }
   * Creates the writer list -
   *    Writer: Db update + Solr update
   * @return
   */
  @Bean(ENTITY_UPDATE_WRITERS)
  public ItemWriter<BatchEntityRecord> entityUpdateWriters() {
    CompositeItemWriter<BatchEntityRecord> compositeWriter = new CompositeItemWriter<>();
    compositeWriter.setDelegates(Arrays.asList(recordDBInsertionWriter(), entitySolrInsertionWriter()));
    return compositeWriter;
  }


  /**
   * Creating it as a bean as this processor list is used for most of the Internal Task of EM.
   * For performnace will access them from application context than creating a list for every request
   * @see <a href="http://docs.google.com/document/d/16k9PcCMFwl2LXjnnzotZRPc-QqM-Ar1D0VELHt4t_hA/edit?tab=t.0#heading=h.fj6e15rbq64q"></a> }
   *
   * Creates the processor list -
   *    Processors: Dereference + consolidation + metrics + validation
   * @return
   */
  @Bean(FULL_ENTITY_UPDATE_PROCESSOR)
  public ItemProcessor<BatchEntityRecord, BatchEntityRecord> fullEntityUpdateProcessor() {
    CompositeItemProcessor<BatchEntityRecord, BatchEntityRecord> compositeItemProcessor =
            new CompositeItemProcessor<>();
    compositeItemProcessor.setDelegates(Arrays.asList(
            applicationContext.getBean(BEAN_ENTITY_DEREFERENCE_PROCESSOR, EntityDereferenceProcessor.class),
            applicationContext.getBean(BEAN_ENTITY_CONSOLIDATION_PROCESSOR, EntityConsolidationProcessor.class),
            applicationContext.getBean(BEAN_ENTITY_METRICS_PROCESSOR, EntityMetricsProcessor.class),
            applicationContext.getBean(BEAN_ENTITY_VERIFICATION_LOGGER, EntityVerificationLogger.class)));

    return compositeItemProcessor;
  }

  /**
   * More generic composite processor,
   * Creates a composite processor with the list of processors provided
   * @param processors
   * @return
   */
  public ItemProcessor<BatchEntityRecord, BatchEntityRecord> compositeProcessor(List<Task> processors) {
    CompositeItemProcessor<BatchEntityRecord, BatchEntityRecord> compositeItemProcessor =
            new CompositeItemProcessor<>();
    List<ItemProcessor<BatchEntityRecord, BatchEntityRecord>> delegates = new ArrayList<>(processors.size());
    for (Task process: processors) {
      delegates.add((ItemProcessor<BatchEntityRecord, BatchEntityRecord>) applicationContext.getBean(process.getBeanName()));
    }
    compositeItemProcessor.setDelegates(delegates);
    return compositeItemProcessor;
  }

  public ApplicationContext getApplicationContext() {
    return applicationContext;
  }
}
