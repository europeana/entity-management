package eu.europeana.entitymanagement.config;

import static eu.europeana.entitymanagement.definitions.EntityRecordFields.ENTITY_ID;
import static eu.europeana.entitymanagement.definitions.batch.EMBatchConstants.UPDATE_TYPE;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;

import eu.europeana.api.commons.auth.AuthenticationBuilder;
import eu.europeana.api.commons.auth.AuthenticationConfig;
import eu.europeana.api.commons.auth.AuthenticationHandler;
import eu.europeana.entitymanagement.web.service.DepictionGeneratorService;
import eu.europeana.entitymanagement.web.service.EnrichmentCountQueryService;
import eu.europeana.entitymanagement.web.service.SearchRecordAccess;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import dev.morphia.query.filters.Filters;
import eu.europeana.api.commons.config.i18n.I18nService;
import eu.europeana.api.commons.config.i18n.I18nServiceImpl;
import eu.europeana.api.commons.oauth2.service.impl.EuropeanaClientDetailsService;
import eu.europeana.entitymanagement.batch.config.EntityUpdateJobFactory;
import eu.europeana.entitymanagement.batch.config.JobDescriptionFactory;
import eu.europeana.entitymanagement.batch.listener.ScheduledTaskItemListener;
import eu.europeana.entitymanagement.batch.model.EntityUpdateStats;
import eu.europeana.entitymanagement.batch.model.JobDescription;
import eu.europeana.entitymanagement.batch.model.Task;
import eu.europeana.entitymanagement.batch.reader.EntityRecordDatabaseReader;
import eu.europeana.entitymanagement.batch.reader.ScheduledTaskDatabaseReader;
import eu.europeana.entitymanagement.batch.service.FailedTaskService;
import eu.europeana.entitymanagement.batch.service.ScheduledTaskService;
import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.batch.EMBatchConstants;
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.definitions.batch.model.TaskType;
import eu.europeana.entitymanagement.definitions.model.Vocabulary;
import eu.europeana.entitymanagement.exception.ApplicationInitializationException;
import eu.europeana.entitymanagement.mongo.repository.VocabularyRepository;
import eu.europeana.entitymanagement.web.MetisDereferenceUtils;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
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
  
  @Bean(name = BEAN_ENTITY_UPDATE_STATS)
  public EntityUpdateStats getEntityUpdateStats() {
    return new EntityUpdateStats(TaskType.full_update);
  }
  
  @Bean(name = BEAN_METRICS_UPDATE_STATS)
  public EntityUpdateStats getMetricUpdateStats() {
    return new EntityUpdateStats(TaskType.metrics_update);
  }

  @Bean(name = BEAN_CLIENT_DETAILS_SERVICE)
  public EuropeanaClientDetailsService getClientDetailsService() {
    EuropeanaClientDetailsService clientDetails = new EuropeanaClientDetailsService();
    clientDetails.setApiKeyServiceUrl(emConfiguration.getApiKeyUrl());
    clientDetails.setAuthHandler(getAuthenticationHandler());
    return clientDetails;
  }

  /**
   * Generate AuthenticationHandler to access other services via EM ( like keycloak and SR API)
   * @return
   */
  public AuthenticationHandler getAuthenticationHandler() {
    if (StringUtils.isNotEmpty(emConfiguration.getTokenEndpoint()) && StringUtils.isNotEmpty(emConfiguration.getKeycloakAccessGrantParams())) {
      AuthenticationConfig config = new AuthenticationConfig(emConfiguration.getTokenEndpoint(), emConfiguration.getKeycloakAccessGrantParams());
      return AuthenticationBuilder.newAuthentication(config);
    } else {
      LOG.error("Keycloak token endpoint and parameters NOT set !!");
    }
    return null;
  }

  /**
   * Creates a authentication handler for SR API access
   * @return authentication for SR API access
   */
  @Bean
  public AuthenticationHandler getSearchRecordAccess() {
    return getAuthenticationHandler();
  }

  @Bean(BEAN_ENRICHMENT_COUNT_SERVICE)
  public EnrichmentCountQueryService getEnrichmentCountQueryService() {
    return new EnrichmentCountQueryService(getSearchRecordAccess());
  }

  @Bean(BEAN_ENTITY_DEPICTION_SERVICE)
  public DepictionGeneratorService getDepictionGeneratorService(){
    return  new DepictionGeneratorService(getSearchRecordAccess());
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
    source.setBasename("classpath:messages");
    source.setDefaultEncoding(StandardCharsets.UTF_8.name());
    return source;
  }

  public EntityUpdateJobFactory getEntityUpdateJobFactory() {
   return applicationContext.getBean(ENTITY_UPDATE_JOB_FACTORY, EntityUpdateJobFactory.class);
  }

  @Bean(JOB_DESCRIPTION_FACTORY)
  public JobDescriptionFactory jobDescriptionProvider() {
    JobDescriptionFactory factory = new JobDescriptionFactory();
    factory.register(
            new JobDescription(
                    TaskType.full_update
                    , JobDescription.PROCESSORS_FULL_UPDATE
                    , JobDescription.PERSISTENCE_ITEM_WRITERS));

    factory.register(
            new JobDescription(
                    TaskType.meta_update
                    , JobDescription.PROCESSORS_META_UPDATE
                    , JobDescription.PERSISTENCE_ITEM_WRITERS));

    factory.register(
            new JobDescription(
                    TaskType.metrics_update
                    , Arrays.asList(Task.METRICS)
                    , JobDescription.PERSISTENCE_ITEM_WRITERS));

    factory.register(
            new JobDescription(
                    TaskType.deprecation
                    , null
                    , JobDescription.DEPRECATION_WRITERS));

    factory.register(
            new JobDescription(
                    TaskType.permanent_deletion
                    , null
                    , JobDescription.PERMANENT_REMOVAL_WRITERS));
    return factory;
  }
  
  @Bean(FULL_ENTITY_UPDATE_PROCESSOR)
  @DependsOn({JOB_DESCRIPTION_FACTORY, ENTITY_UPDATE_JOB_FACTORY})
  public ItemProcessor<BatchEntityRecord, BatchEntityRecord> getFullEntityUpdateProcessor() {
    return getEntityUpdateJobFactory().createFullEntityUpdateProcessor();
  }
  
  @Bean(ENTITY_UPDATE_WRITERS)
  @DependsOn({BEAN_ENTITY_RECORD_DBINSERTION_WRITER, BEAN_ENTITY_SOLR_INSERTION_WRITER, ENTITY_UPDATE_JOB_FACTORY})
  public ItemWriter<BatchEntityRecord> entityUpdateWriters() {
    return getEntityUpdateJobFactory().buildEntityUpdateWriters();
  }

  /** Note for StepScope annotations we need the context to be set for the target class
   *  EnableBatchProcessing annotation is needed to the target class
   *  by specifying a spring batch component being StepScope means that Spring Batch
   *  will use the spring container to instantiate a new instance of that component for each step execution.
   *
   *  Another useful reason to use StepScope is when you decide to reuse the same component in parallel steps
   */

  /*
   * Creates a listener that's called while processing a single item
   *
   * JobParameters cannot be boolean, so the isSynchronous value is converted from its string representation
   */
  @Bean
  @StepScope
  public ScheduledTaskItemListener getScheduledTaskItemListener(
          // see JobParameter enum for string values
          @Value("#{jobParameters[isSynchronous]}") String isSynchronousString) {
    return new ScheduledTaskItemListener(
            applicationContext.getBean("failedTaskService", FailedTaskService.class),
            applicationContext.getBean(BEAN_BATCH_SCHEDULED_TASK_SERVICE, ScheduledTaskService.class),
            Boolean.parseBoolean(isSynchronousString),
            applicationContext.getBean(BEAN_ENTITY_UPDATE_STATS, EntityUpdateStats.class),
            applicationContext.getBean(BEAN_METRICS_UPDATE_STATS, EntityUpdateStats.class));
  }

  /** ItemReader that queries by entityId when retrieving EntityRecords from the database */
  @Bean(name = SINGLE_ENTITY_RECORD_READER)
  @StepScope
  public EntityRecordDatabaseReader singleEntityRecordReader(
          @Value("#{jobParameters[entityId]}") String entityIdString,
          @Value("#{jobParameters[updateType]}") String updateType) {
    return new EntityRecordDatabaseReader(
            updateType,
            applicationContext.getBean(BEAN_ENTITY_RECORD_SERVICE, EntityRecordService.class),
            emConfiguration.getBatchChunkSize(),
            Filters.eq(ENTITY_ID, entityIdString));
  }

  @Bean(name = SCHEDULED_TASK_READER)
  @StepScope
  public SynchronizedItemStreamReader<BatchEntityRecord> scheduledTaskReader(
          @Value("#{jobParameters[currentStartTime]}") Date currentStartTime,
          @Value("#{jobParameters[updateType]}") String updateType) {

    List<String> updateTypeList =
            Stream.of(updateType.split(",")).map(String::trim).collect(Collectors.toList());

    ScheduledTaskDatabaseReader reader =
            new ScheduledTaskDatabaseReader(
                    applicationContext.getBean(BEAN_BATCH_SCHEDULED_TASK_SERVICE, ScheduledTaskService.class),
                    applicationContext.getBean(BEAN_ENTITY_RECORD_SERVICE, EntityRecordService.class),
                    emConfiguration.getBatchChunkSize(),
                    Filters.lte(EMBatchConstants.CREATED, currentStartTime),
                    Filters.in(UPDATE_TYPE, updateTypeList));

    return threadSafeReader(reader);
  }

  /** Makes ItemReader thread-safe */
  private <T> SynchronizedItemStreamReader<T> threadSafeReader(ItemStreamReader<T> reader) {
    final SynchronizedItemStreamReader<T> synchronizedItemStreamReader =
            new SynchronizedItemStreamReader<>();
    synchronizedItemStreamReader.setDelegate(reader);
    return synchronizedItemStreamReader;
  }

}
