package eu.europeana.entitymanagement.solr.service;

import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.BEAN_INDEXING_SOLR_CLIENT;
import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.BEAN_JSON_MAPPER;
import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.BEAN_SOLR_ENTITY_SUGGESTER_FILTER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.definitions.model.TimeSpan;
import eu.europeana.entitymanagement.solr.SolrEntitySuggesterMixins;
import eu.europeana.entitymanagement.solr.SolrEntitySuggesterMixins.ConceptSuggesterMixin;
import eu.europeana.entitymanagement.solr.SolrEntitySuggesterMixins.PlaceSuggesterMixin;
import eu.europeana.entitymanagement.solr.SolrEntitySuggesterMixins.TimeSpanSuggesterMixin;
import eu.europeana.entitymanagement.solr.SolrSearchCursorIterator;
import eu.europeana.entitymanagement.solr.SolrUtils;
import eu.europeana.entitymanagement.solr.exception.SolrServiceException;
import eu.europeana.entitymanagement.solr.model.SolrConceptScheme;
import eu.europeana.entitymanagement.solr.model.SolrEntity;
import eu.europeana.entitymanagement.solr.model.SolrOrganization;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.BindingException;
import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service(AppConfigConstants.BEAN_EM_SOLR_SERVICE)
public class SolrService implements InitializingBean {

  private final ObjectMapper payloadMapper;

  private final Logger log = LogManager.getLogger(getClass());
  private final SolrClient solrClient;
  private final FilterProvider solrEntityFilter;

  private final boolean isExplicitCommitsEnabled;
  private final int solrQueryMaxRows;

  @Autowired
  public SolrService(
      @Qualifier(BEAN_INDEXING_SOLR_CLIENT) SolrClient solrClient,
      EntityManagementConfiguration configuration,
      @Qualifier(BEAN_JSON_MAPPER) ObjectMapper objectMapper,
      @Qualifier(BEAN_SOLR_ENTITY_SUGGESTER_FILTER) FilterProvider solrEntityFilter) {
    this.solrClient = solrClient;
    this.isExplicitCommitsEnabled = configuration.explicitCommitsEnabled();
    this.solrQueryMaxRows = configuration.getSolrQueryMaxPageSize();

    // copy default mapper so Solr payload configurations don't overwrite any global ones
    this.payloadMapper = objectMapper.copy();
    this.solrEntityFilter = solrEntityFilter;
  }

  @Override
  public void afterPropertiesSet() {
    payloadMapper.addMixIn(Agent.class, SolrEntitySuggesterMixins.AgentSuggesterMixin.class);
    payloadMapper.addMixIn(
        Organization.class, SolrEntitySuggesterMixins.OrganizationSuggesterMixin.class);
    payloadMapper.addMixIn(TimeSpan.class, TimeSpanSuggesterMixin.class);
    payloadMapper.addMixIn(Concept.class, ConceptSuggesterMixin.class);
    payloadMapper.addMixIn(Place.class, PlaceSuggesterMixin.class);
    payloadMapper.setFilterProvider(solrEntityFilter);
  }

  /**
   * Indexes a single entity to Solr. Replaces any existing entities with the same entity
   * identifier.
   *
   * @param solrEntity entity to be indexed
   * @throws SolrServiceException if error occurs while generating payload, or during Solr indexing
   */
  public void storeEntity(final SolrEntity<? extends Entity> solrEntity)
      throws SolrServiceException {
    setPayload(solrEntity);

    try {
      UpdateResponse rsp = solrClient.addBean(solrEntity);
      if (isExplicitCommitsEnabled) {
        solrClient.commit();
        log.debug("Performed explicit commit for entityId={}", solrEntity.getEntityId());
      }

      log.debug(
          "Indexed entity to Solr in {}ms: entityId={}",
          rsp.getElapsedTime(),
          solrEntity.getEntityId());
    } catch (SolrServerException | IOException | RuntimeException ex) {
      throw new SolrServiceException(
          String.format("Error during Solr indexing for entityId=%s", solrEntity.getEntityId()),
          ex);
    }
  }

  public void storeConceptScheme(SolrConceptScheme solrConceptScheme) throws SolrServiceException {
    try {
      UpdateResponse rsp = solrClient.addBean(solrConceptScheme);
      if (isExplicitCommitsEnabled) {
        solrClient.commit();
        log.debug("Performed explicit commit for entityId={}", solrConceptScheme.getEntityId());
      }

      log.debug(
          "Indexed entity to Solr in {}ms: entityId={}",
          rsp.getElapsedTime(),
          solrConceptScheme.getEntityId());
    } catch (SolrServerException | IOException | RuntimeException ex) {
      throw new SolrServiceException(
          String.format(
              "Error during Solr indexing for entityId=%s", solrConceptScheme.getEntityId()),
          ex);
    }
  }

  /**
   * Indexes multiple entities to Solr. Replaces any existing entities with the same entity
   * identifier.
   *
   * @param solrEntityList list of entities to be indexed
   * @throws SolrServiceException if error occurs while generating payload, or during Solr indexing
   */
  public void storeMultipleEntities(final List<SolrEntity<? extends Entity>> solrEntityList)
      throws SolrServiceException {
    if (CollectionUtils.isEmpty(solrEntityList)) {
      // prevents Solr error if empty list is passed to this method
      return;
    }

    // first set payload on SolrEntities
    for (SolrEntity<? extends Entity> solrEntity : solrEntityList) {
      setPayload(solrEntity);
    }

    // EntityIDs for logging
    String entityIds = Arrays.toString(getEntityIds(solrEntityList));
    try {
      UpdateResponse response = solrClient.addBeans(solrEntityList);
      if (isExplicitCommitsEnabled) {
        solrClient.commit();
        log.debug("Performed explicit commit for entityIds={}", entityIds);
      }
      log.debug(
          "Indexed {} entities to Solr in {}ms: entityIds={}",
          solrEntityList.size(),
          response.getElapsedTime(),
          entityIds);
    } catch (SolrServerException | IOException | RuntimeException ex) {
      throw new SolrServiceException(
          String.format(
              "Error during Solr indexing for multiple entities, entityIds=%s", entityIds),
          ex);
    }
  }

  /**
   * Queries Solr for the document matching the provided ID and classType
   *
   * @param classType {@link SolrEntity} subclass; expected class type for document
   * @param entityId entityID
   * @return SolrEntity instance
   * @throws SolrServiceException if error occurs while executing query
   */
  public <T extends Entity, U extends SolrEntity<T>> U searchById(
      Class<U> classType, String entityId) throws SolrServiceException {

    QueryResponse rsp;
    SolrQuery query = new SolrQuery();
    query.set("q", EntitySolrFields.ID + ":\"" + entityId + "\"");
    try {
      rsp = solrClient.query(query);
      if (log.isDebugEnabled()) {
        log.debug(
            "Performed Solr search query in {}ms:  type={}, query={}",
            rsp.getElapsedTime(),
            classType.getSimpleName(),
            query);
      }

    } catch (IOException | SolrServerException ex) {
      throw new SolrServiceException(
          String.format("Error while searching Solr for entityId=%s", entityId), ex);
    }

    DocumentObjectBinder binder = new DocumentObjectBinder();
    SolrDocumentList docList = rsp.getResults();

    if (docList == null || docList.size() == 0) return null;

    SolrDocument doc = docList.get(0);
    return binder.getBean(classType, doc);
  }

  public SolrConceptScheme searchConceptSchemeById(String conceptSchemeId)
      throws SolrServiceException {
    QueryResponse rsp;
    SolrQuery query = new SolrQuery();
    query.set("q", EntitySolrFields.ID + ":\"" + conceptSchemeId + "\"");
    try {
      rsp = solrClient.query(query);
      if (log.isDebugEnabled()) {
        log.debug(
            "Performed Solr search query in {}ms:  type={}, query={}",
            rsp.getElapsedTime(),
            SolrConceptScheme.class.getSimpleName(),
            query);
      }
    } catch (IOException | SolrServerException ex) {
      throw new SolrServiceException(
          String.format("Error while searching Solr for entityId=%s", conceptSchemeId), ex);
    }

    DocumentObjectBinder binder = new DocumentObjectBinder();
    SolrDocumentList docList = rsp.getResults();

    if (docList == null || docList.size() == 0) return null;

    SolrDocument doc = docList.get(0);
    return binder.getBean(SolrConceptScheme.class, doc);
  }

  /**
   * Search by provided solr query
   *
   * @param query the solr query to be used for retrieval
   * @return a list of found entities or null if none found
   * @throws SolrServiceException
   */
  public List<SolrEntity<Entity>> searchByQuery(SolrQuery query) throws SolrServiceException {

    QueryResponse rsp;
    try {
      rsp = solrClient.query(query);
      if (log.isDebugEnabled()) {
        log.debug("Performed Solr search query in {}ms:  query={}", rsp.getElapsedTime(), query);
      }

    } catch (IOException | SolrServerException ex) {
      throw new SolrServiceException(
          String.format("Error while searching Solr for query=%s", query), ex);
    }

    return getEntityList(rsp);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  List<SolrEntity<Entity>> getEntityList(QueryResponse rsp) throws SolrServiceException {
    SolrDocumentList docList = rsp.getResults();

    if (docList == null || docList.isEmpty()) {
      return null;
    }

    try {
      List<SolrEntity<Entity>> res = new ArrayList<>(docList.size());
      String type;
      Class classType;
      DocumentObjectBinder binder = new DocumentObjectBinder();

      for (SolrDocument solrDocument : docList) {
        type = (String) solrDocument.get(EntitySolrFields.TYPE);
        classType = SolrUtils.getSolrEntityClass(type);
        res.add((SolrEntity<Entity>) binder.getBean(classType, solrDocument));
      }
      return res;
    } catch (BindingException e) {
      throw new SolrServiceException("Cannot parse solr response.", e);
    }
  }

  /**
   * Fetches all documents matching the query string using a cursor.
   *
   * <p>This method returns an iterator yielding a list of {@link SolrEntity} instances.
   *
   * @param searchQueryString query string
   * @param fields if not empty, specifies fields to be included in the query response
   * @return iterator
   */
  public SolrSearchCursorIterator getSearchIterator(String searchQueryString, List<String> fields) {
    SolrQuery q =
        new SolrQuery(searchQueryString)
            .setRows(solrQueryMaxRows)
            .setFields(fields.toArray(String[]::new))
            .setSort(SolrQuery.SortClause.asc(EntitySolrFields.ID));

    return new SolrSearchCursorIterator(solrClient, q);
  }

  /**
   * Fetches all documents matching the query string using a cursor.
   *
   * <p>This method returns an iterator yielding a list of {@link SolrEntity} instances.
   *
   * @param searchQueryString query string
   * @return iterator
   */
  public SolrSearchCursorIterator getSearchIterator(String searchQueryString) {
    return getSearchIterator(searchQueryString, Collections.emptyList());
  }

  /**
   * Deletes the document whose id matches the specified entityId value
   *
   * @param entityIds entity id list
   * @param forceCommit flag use to force synchronuous solr commit
   * @throws SolrServiceException on error
   */
  public void deleteById(List<String> entityIds, boolean forceCommit) throws SolrServiceException {
    try {
      UpdateResponse updateResponse = solrClient.deleteById(entityIds);
      if (forceCommit || isExplicitCommitsEnabled) {
        solrClient.commit();
      }
      log.info(
          "Deleted {} documents from Solr; entityIds={}",
          updateResponse.getResponse().size(),
          entityIds);
    } catch (SolrServerException | IOException e) {
      throw new SolrServiceException(String.format("Error deleting entityId=%s", entityIds), e);
    }
  }

  /** Deletes all documents. Only used during integration tests */
  public void deleteAllDocuments() throws Exception {
    UpdateResponse response = solrClient.deleteByQuery("*");
    solrClient.commit();
    log.info("Deleted all documents from Solr in {}ms", response.getElapsedTime());
  }

  // TODO: consider moving this method to SolrUtils class
  private String createPayload(SolrEntity<? extends Entity> solrEntity)
      throws JsonProcessingException {

    /*
     * specifying fields to be filtered
     * TODO: add the isShownBy.source and isShownBy.thumbnail fields
     */

    if (solrEntity instanceof SolrOrganization) {
      /*
       * according to the specifications, leaving only the value for the "en" key in the suggester for organizationDomain
       */

      ObjectNode agentJacksonNode = payloadMapper.valueToTree(solrEntity.getEntity());
      JsonNode organizationDomainNode = agentJacksonNode.get("organizationDomain");
      if (organizationDomainNode != null && organizationDomainNode.toString().contains("\"en\"")) {
        agentJacksonNode.replace("organizationDomain", organizationDomainNode.get("\"en\""));
      }
      return payloadMapper.writeValueAsString(agentJacksonNode);
    } else {
      // if (solrEntity instanceof SolrAgent || solrEntity instanceof SolrTimeSpan) {
      return payloadMapper.writeValueAsString(solrEntity.getEntity());
    }
  }

  private void setPayload(SolrEntity<? extends Entity> solrEntity) throws SolrServiceException {
    try {
      solrEntity.setPayload(createPayload(solrEntity));
    } catch (JsonProcessingException e) {
      throw new SolrServiceException(
          String.format("Error generating Solr payload for entityId=%s", solrEntity.getEntityId()),
          e);
    }
  }

  private String[] getEntityIds(List<SolrEntity<?>> solrEntityList) {
    return solrEntityList.stream().map(SolrEntity::getEntityId).toArray(String[]::new);
  }
}
