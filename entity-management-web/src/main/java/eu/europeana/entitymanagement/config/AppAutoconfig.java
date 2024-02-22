package eu.europeana.entitymanagement.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
}
