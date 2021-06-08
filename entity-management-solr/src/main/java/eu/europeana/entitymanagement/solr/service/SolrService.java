package eu.europeana.entitymanagement.solr.service;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.BEAN_INDEXING_SOLR_CLIENT;

import java.io.IOException;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.solr.exception.SolrServiceException;
import eu.europeana.entitymanagement.solr.model.SolrEntity;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

@Service(AppConfigConstants.BEAN_EM_SOLR_SERVICE)
public class SolrService {

	@Autowired
	@Qualifier(AppConfigConstants.BEAN_JSON_MAPPER)
	ObjectMapper payloadObjectMapper;	
	
	private final Logger log = LogManager.getLogger(getClass());
	
	EntityManagementConfiguration emConfiguration;
	
	SolrClient indexingSolrClient;
   
	@Autowired 
    public SolrService(EntityManagementConfiguration emConfig,
					   @Qualifier(BEAN_INDEXING_SOLR_CLIENT) SolrClient indexingSolrClient) {
		emConfiguration=emConfig;
		this.indexingSolrClient = indexingSolrClient;
	}

	public void storeEntity(final SolrEntity<? extends Entity> solrEntity, boolean doCommit) throws SolrServiceException {

		try {
			solrEntity.setPayload(createSolrSuggesterField(solrEntity));
		} catch (IOException ex) {
			throw new SolrServiceException("An unexpected exception occured when creating the Solr objects from the corresponding Entity object.", ex);
		}
				
		try {			
			log.debug("Storing to Solr. Object: " + solrEntity.toString());				
			UpdateResponse rsp =indexingSolrClient.addBean(solrEntity);
			log.info("Solr response after storing: " + rsp.toString());
			if(doCommit)
				indexingSolrClient.commit();
		} catch (SolrServerException | IOException | RuntimeException ex) {
			throw new SolrServiceException("An unexpected exception occured when storing the Entity: " + solrEntity.toString() + " to Solr.", ex);
		}
		
	}
	
		@SuppressWarnings("unused")
	    public <T extends Entity, U extends SolrEntity<T>> U searchById(Class<U> classType, String entityId)
				throws SolrServiceException {

		log.debug("Search entity (type:" + classType.getName() + " ) by id: " + entityId + " in Solr.");

		QueryResponse rsp;
		SolrQuery query = new SolrQuery();
		query.set("q", EntitySolrFields.ID+ ":\"" + entityId + "\"");
		try {
			rsp =indexingSolrClient.query(query);
			log.debug("query response: " + rsp.toString());
			
		} catch (IOException | SolrServerException ex) {
		    throw new SolrServiceException("Unexpected exception occured when searching entities for id: " + entityId + " in Solr.", ex);
		}		

		DocumentObjectBinder binder = new DocumentObjectBinder();
		SolrDocumentList docList = rsp.getResults();
		
		if (docList==null || docList.size()==0) return null;		
		
		SolrDocument doc = docList.get(0);
		return binder.getBean(classType, doc);

    }

		private String createSolrSuggesterField(SolrEntity<? extends Entity> entity) throws JsonGenerationException, JsonMappingException, IOException {

			/*
			 * specifying fields to be filtered
			 * TODO: add the isShownBy.source and isShownBy.thumbnail fields
			 */

			SimpleFilterProvider filterProvider = new SimpleFilterProvider();
		      
			if(entity.getEntity().getType().compareToIgnoreCase(EntityTypes.Agent.toString())==0) {
				filterProvider.addFilter("solrSuggesterFilter", SimpleBeanPropertyFilter.filterOutAllExcept("isShownBy", "prefLabel", "altLabel", "hiddenLabel", "dateOfBirth", "dateOfDeath", "dateOfEstablishment", "dateOfTermination"));
				payloadObjectMapper.setFilterProvider(filterProvider);
				ObjectNode agentJacksonNode = payloadObjectMapper.valueToTree(entity.getEntity());
				return agentJacksonNode.toString();
			}
			else if(entity.getEntity().getType().compareToIgnoreCase(EntityTypes.Organization.toString())==0) {
				/*
				 * according to the specifications, leaving only the value for the "en" key in the suggester for organizationDomain
				 */
				filterProvider.addFilter("solrSuggesterFilter", SimpleBeanPropertyFilter.filterOutAllExcept("isShownBy", "prefLabel", "altLabel", "hiddenLabel", "acronym", "organizationDomain", "country"));			
				payloadObjectMapper.setFilterProvider(filterProvider);
				ObjectNode agentJacksonNode = payloadObjectMapper.valueToTree(entity.getEntity());
				JsonNode organizationDomainNode = agentJacksonNode.get("organizationDomain");
				agentJacksonNode.replace("organizationDomain", organizationDomainNode.get("en"));
				return agentJacksonNode.toString();

			}
			else if(entity.getType().compareToIgnoreCase(EntityTypes.Timespan.toString())==0) {
				filterProvider.addFilter("solrSuggesterFilter", SimpleBeanPropertyFilter.filterOutAllExcept("isShownBy", "prefLabel", "altLabel", "hiddenLabel", "begin", "end"));
				payloadObjectMapper.setFilterProvider(filterProvider);
				ObjectNode agentJacksonNode = payloadObjectMapper.valueToTree(entity.getEntity());
				return agentJacksonNode.toString();
			}
			
			return null;
		}
		

}
