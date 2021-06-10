package eu.europeana.entitymanagement.solr.service;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import eu.europeana.entitymanagement.solr.model.SolrAgent;
import eu.europeana.entitymanagement.solr.model.SolrOrganization;
import eu.europeana.entitymanagement.solr.model.SolrTimespan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.solr.exception.SolrServiceException;
import eu.europeana.entitymanagement.solr.model.SolrEntity;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.*;

@Service(AppConfigConstants.BEAN_EM_SOLR_SERVICE)
public class SolrService implements InitializingBean {

	@Autowired
	@Qualifier(BEAN_JSON_MAPPER)
	private final ObjectMapper objectMapper;

	private final Logger log = LogManager.getLogger(getClass());
	private final SolrClient solrClient;
	private final FilterProvider solrEntityFilter;

	private final boolean isExplicitCommitsEnabled;
   
	@Autowired 
    public SolrService(@Qualifier(BEAN_INDEXING_SOLR_CLIENT) SolrClient solrClient,
					   EntityManagementConfiguration configuration,
					   @Qualifier(BEAN_JSON_MAPPER) ObjectMapper objectMapper,
					   @Qualifier(BEAN_SOLR_ENTITY_FILTER) FilterProvider solrEntityFilter) {
		this.solrClient = solrClient;
		this.isExplicitCommitsEnabled = configuration.explicitCommitsEnabled();
		this.objectMapper = objectMapper;
		this.solrEntityFilter = solrEntityFilter;
	}

	@Override
	public void afterPropertiesSet()  {
		// this means we can't set FilterProviders elsewhere
		// TODO: confirm if we can create a new ObjectMapper here instead
		objectMapper.setFilterProvider(solrEntityFilter);
	}

	public void storeEntity(final SolrEntity<? extends Entity> solrEntity) throws SolrServiceException {
		try {
			solrEntity.setPayload(createSolrSuggesterField(solrEntity));
		} catch (JsonProcessingException e) {
			throw new SolrServiceException(String.format("Error generating Solr payload for entityId=%s",
					solrEntity.getEntityId()), e);
		}

		try {
			UpdateResponse rsp = solrClient.addBean(solrEntity);
			if(isExplicitCommitsEnabled) {
				solrClient.commit();
				log.debug("Performed explicit commit for entityId={}", solrEntity.getEntityId());
			}

			log.debug("Indexed entity to Solr in {}ms: entityId={}", rsp.getElapsedTime(),
					solrEntity.getEntityId());
		} catch (SolrServerException | IOException | RuntimeException ex) {
			throw new SolrServiceException(String.format("Error during Solr indexing for entityId=%s", solrEntity.getEntityId()), ex);
		}
		
	}
	
		@SuppressWarnings("unused")
	    public <T extends Entity, U extends SolrEntity<T>> U searchById(Class<U> classType, String entityId)
				throws SolrServiceException {


		QueryResponse rsp;
		SolrQuery query = new SolrQuery();
		query.set("q", EntitySolrFields.ID+ ":\"" + entityId + "\"");
		try {
			rsp = solrClient.query(query);
			log.debug("Performed Solr search query in {}ms:  type={}, entityId={}",
					rsp.getElapsedTime(), classType.getSimpleName(), entityId);
			
		} catch (IOException | SolrServerException ex) {
		    throw new SolrServiceException(String.format("Error while searching Solr for entityId=%s", entityId), ex);
		}		

		DocumentObjectBinder binder = new DocumentObjectBinder();
		SolrDocumentList docList = rsp.getResults();
		
		if (docList==null || docList.size()==0) return null;		
		
		SolrDocument doc = docList.get(0);
		return binder.getBean(classType, doc);

    }

		private String createSolrSuggesterField(SolrEntity<? extends Entity> solrEntity) throws JsonProcessingException {

			/*
			 * specifying fields to be filtered
			 * TODO: add the isShownBy.source and isShownBy.thumbnail fields
			 */
		      
			if(solrEntity instanceof SolrAgent || solrEntity instanceof SolrTimespan) {
				return objectMapper.writeValueAsString(solrEntity);
			}
			else if(solrEntity instanceof SolrOrganization) {
				/*
				 * according to the specifications, leaving only the value for the "en" key in the suggester for organizationDomain
				 */

				ObjectNode agentJacksonNode = objectMapper.valueToTree(solrEntity);
				JsonNode organizationDomainNode = agentJacksonNode.get("organizationDomain");
				agentJacksonNode.replace("organizationDomain", organizationDomainNode.get("en"));
				return objectMapper.writeValueAsString(organizationDomainNode);
			}
			return null;
		}

	/**
	 * Deletes all documents.
	 * Only used during integration tests
	 */
	public void deleteAllDocuments() throws Exception {
		UpdateResponse response = solrClient.deleteByQuery("*");
		solrClient.commit();
		log.info("Deleted all documents from Solr in {}ms", response.getElapsedTime());
	}
}