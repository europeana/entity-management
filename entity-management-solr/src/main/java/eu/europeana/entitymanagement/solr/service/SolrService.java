package eu.europeana.entitymanagement.solr.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import eu.europeana.entitymanagement.solr.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.definitions.model.Timespan;
import eu.europeana.entitymanagement.solr.exception.SolrServiceException;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

@Service(AppConfigConstants.BEAN_EM_SOLR_SERVICE)
public class SolrService {

	@Qualifier(AppConfigConstants.BEAN_JSON_MAPPER)
	@Autowired
	ObjectMapper emJsonMapper;	
	
	private final Logger log = LogManager.getLogger(getClass());
	
	EntityManagementConfiguration emConfiguration;
	
	SolrClient indexingSolrClient;
   
	@Autowired 
    public SolrService(EntityManagementConfiguration emConfig) {
		emConfiguration=emConfig;
    	indexingSolrClient = new HttpSolrClient.Builder(emConfiguration.getIndexingSolrUrl()).build();
    }

	public void storeEntity(final SolrEntity<? extends Entity> solrEntity, boolean doCommit) throws SolrServiceException {

		// TODO: adapt and uncomment
//			try {
//
//			solrEntity.setPayload(createSolrSuggesterField(solrEntity));
//
//		} catch (IOException ex) {
//			throw new SolrServiceException("An unexpected exception occured when creating the Solr objects from the corresponding Entity object.", ex);
//		}
				
		try {			
			log.debug("Storing to Solr. Object: " + solrEntity.toString());				
			UpdateResponse rsp =indexingSolrClient.addBean(emConfiguration.getIndexingSolrCollection(), solrEntity);
			log.info("Solr response after storing: " + rsp.toString());
			if(doCommit)
				indexingSolrClient.commit(emConfiguration.getIndexingSolrCollection());
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
			rsp =indexingSolrClient.query(emConfiguration.getIndexingSolrCollection(), query);
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

//    TODO: adapt and uncomment

//		private String createSolrSuggesterField(SolrEntity<? extends Entity> entity) throws JsonGenerationException, JsonMappingException, IOException {
//
//			if(entity.getType().compareToIgnoreCase(EntityTypes.Agent.toString())==0) {
//				Agent agent = new Agent((Agent)entity);
//				agent.setNote(null);
//				agent.setHasPart(null);
//				agent.setIsPartOfArray(null);
//				agent.setDepiction(null);
//				agent.setSameAs(null);
//				agent.setType(null);
//				agent.setEntityId(null);
//				agent.setIsAggregatedBy(null);
//
//				agent.setDate(null);
//				agent.setIdentifier(null);
//				agent.setBegin(null);
//				agent.setEnd(null);
//				agent.setHasMet(null);
//				agent.setIsRelatedTo(null);
//				agent.setWasPresentAt(null);
//				agent.setName(null);
//				agent.setBiographicalInformation(null);
//				agent.setPlaceOfBirth(null);
//				agent.setPlaceOfDeath(null);
//				agent.setGender(null);
//				agent.setProfessionOrOccupation(null);
//				agent.setIdentifier(null);
//				agent.setBegin(null);
//				agent.setEnd(null);
//				agent.setDate(null);
//
//				ObjectNode agentJacksonNode = emJsonMapper.valueToTree(agent);
//				return agentJacksonNode.toString();
//			}
//			else if(entity.getType().compareToIgnoreCase(EntityTypes.Organization.toString())==0) {
//
//				/*
//				 * according to the specifications, leaving only the "en" key in the suggester
//				 */
//				Map<String,List<String>> orgDomain = organization.getOrganizationDomain();
//				for(Map.Entry<String, List<String>> entry : orgDomain.entrySet()) {
//					if(!entry.getKey().equals("en")) {
//						orgDomain.remove(entry.getKey());
//					}
//				}
//
//				ObjectNode organizationJacksonNode = emJsonMapper.valueToTree(organization);
//				return organizationJacksonNode.toString();
//			}
//			else if(entity.getType().compareToIgnoreCase(EntityTypes.Timespan.toString())==0) {
//				Timespan timespan = new Timespan((Timespan)entity);
//				timespan.setNote(null);
//				timespan.setHasPart(null);
//				timespan.setIsPartOfArray(null);
//				timespan.setDepiction(null);
//				timespan.setSameAs(null);
//				timespan.setType(null);
//				timespan.setEntityId(null);
//				timespan.setIsAggregatedBy(null);
//
//				timespan.setIdentifier(null);
//				timespan.setIsNextInSequence(null);
//				timespan.setIsRelatedTo(null);
//				timespan.setNote(null);
//
//				ObjectNode timespanJacksonNode = emJsonMapper.valueToTree(timespan);
//				return timespanJacksonNode.toString();
//			}
//
//			return "";
//
//		}
		

}
