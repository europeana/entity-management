package eu.europeana.entitymanagement.web.service;

import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.model.ConceptScheme;
import eu.europeana.entitymanagement.exception.EntityNotFoundException;
import eu.europeana.entitymanagement.exception.EntityRemovedException;
import eu.europeana.entitymanagement.exception.ingestion.EntityUpdateException;
import eu.europeana.entitymanagement.mongo.repository.ConceptSchemeRepository;
import eu.europeana.entitymanagement.solr.service.SolrService;
import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import eu.europeana.entitymanagement.utils.EntityUtils;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

@Service(AppConfig.BEAN_CONCEPT_SCHEME_SERVICE)
public class ConceptSchemeService {

  private final ConceptSchemeRepository emConceptSchemeRepo;
  private final SolrService solrService;
  final EntityManagementConfiguration emConfiguration;

  private static final Logger logger = LogManager.getLogger(ConceptSchemeService.class);

  @Autowired
  public ConceptSchemeService(
      ConceptSchemeRepository emConceptSchemeRepo,
      EntityManagementConfiguration emConfiguration,
      SolrService solrService) {
    this.emConceptSchemeRepo = emConceptSchemeRepo;
    this.emConfiguration = emConfiguration;
    this.solrService = solrService;
  }

  public void setMandatoryFields(ConceptScheme scheme) {

    if (scheme.getItems() != null) {
      scheme.setTotal(scheme.getItems().size());
    }
    Date now = new Date();
    scheme.setCreated(now);
    scheme.setModified(now);
  }


  public ConceptScheme createConceptScheme(ConceptScheme scheme) {
    
    Long id = generateConceptSchemeIdentifier();
    scheme.setIdentifier(id);

    setMandatoryFields(scheme);

    ConceptScheme dbScheme = emConceptSchemeRepo.saveConceptScheme(scheme);
    setConceptSchemeId(dbScheme);

//    try {
//      solrService.storeConceptScheme(new SolrConceptScheme(scheme));
//    } catch (SolrServiceException e) {
//     throw new SolrServiceException(
//         String.format("Error during Solr indexing for id=%s", scheme.getConceptSchemeId()), e);
//    }
    return dbScheme;
  }

  public ConceptScheme storeConceptScheme(ConceptScheme scheme) {
    emConceptSchemeRepo.saveConceptScheme(scheme);
    //disabling indexing in this version
//    try {
//      solrService.storeConceptScheme(new SolrConceptScheme(scheme));
//    } catch (SolrServiceException e) {
//      logger.warn("Exception during Solr indexing of the concept scheme with the id: {}", scheme.getConceptSchemeId());
//    }
    return scheme;
  }
  
  public ConceptScheme retrieveConceptScheme(long identifier)
      throws EuropeanaApiException {
        
    return retrieveConceptScheme(identifier, false);
  }

  public ConceptScheme retrieveConceptScheme(long identifier, boolean retrieveDisabled)
      throws EuropeanaApiException {
    ConceptScheme dbScheme = emConceptSchemeRepo.findConceptScheme(identifier);

    if (dbScheme == null) {
      throw new EntityNotFoundException("identifier:" + identifier);
    }

    if (!retrieveDisabled && dbScheme.isDisabled()) {
      throw new EntityRemovedException(
          String.format(EntityRecordUtils.ENTITY_ID_REMOVED_MSG, identifier));
    }

    setConceptSchemeId(dbScheme);
    return dbScheme;
  }

  void setConceptSchemeId(ConceptScheme scheme) {
    //set the schemeId for serialization
    scheme.setConceptSchemeId(
        EntityUtils.buildConceptSchemeId(emConfiguration.getSchemeDataEndpoint(), scheme.getIdentifier()));
  }

  public void disableConceptScheme(ConceptScheme scheme, boolean forceSolrCommit)
      throws EntityUpdateException {
    // not specified yet
    //    updateConceptSchemeEntities(scheme);

    //disable solr indexing in this version
//    try {
//      solrService.deleteById(List.of(scheme.getConceptSchemeId()), forceSolrCommit);
//    } catch (SolrServiceException e) {
//      throw new EntityUpdateException(
//          "Cannot delete solr record with id: " + scheme.getConceptSchemeId(), e);
//    }
    scheme.setDisabled(new Date());
    emConceptSchemeRepo.saveConceptScheme(scheme);
  }

  /**
   * generates the EntityId If entityId is present, generate entity id uri with entityId else
   * generates a auto increment id ex: http://data.europeana.eu/<entitytype>/<entityId> OR
   * http://data.europeana.eu/<entitytype>/<dbId>
   *
   * @param entityType
   * @param entityId
   * @return the generated EntityId
   */
  private Long generateConceptSchemeIdentifier() {
    return emConceptSchemeRepo.generateAutoIncrement(EntityTypes.ConceptScheme.getEntityType());
  }

  public void dropRepository() {
    logger.warn("The concept scheme collection was deleted!");
    this.emConceptSchemeRepo.dropCollection();
  }
}
