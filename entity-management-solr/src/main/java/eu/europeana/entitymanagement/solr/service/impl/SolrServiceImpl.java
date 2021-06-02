package eu.europeana.entitymanagement.solr.service.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

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
import eu.europeana.entitymanagement.solr.model.SolrAgent;
import eu.europeana.entitymanagement.solr.model.SolrConcept;
import eu.europeana.entitymanagement.solr.model.SolrOrganization;
import eu.europeana.entitymanagement.solr.model.SolrPlace;
import eu.europeana.entitymanagement.solr.model.SolrTimespan;
import eu.europeana.entitymanagement.solr.service.SolrService;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

@Service(AppConfigConstants.BEAN_EM_SOLR_SERVICE)
public class SolrServiceImpl implements SolrService {

	@Autowired
	SolrClient solrServer;
	
	@Autowired
	EntityManagementConfiguration emConfiguration;
	
	@Qualifier(AppConfigConstants.BEAN_JSON_MAPPER)
	@Autowired
	ObjectMapper emJsonMapper;	
	
	private final Logger log = LogManager.getLogger(getClass());

	@Override
	public void storeEntity(Entity solrObject, boolean doCommit) throws SolrServiceException {
		Entity solrEntity = null;
		try {
			if(solrObject.getType().compareToIgnoreCase(EntityTypes.Agent.toString())==0) {
				solrEntity = new SolrAgent((Agent)solrObject);				
			}
			else if(solrObject.getType().compareToIgnoreCase(EntityTypes.Concept.toString())==0) {
				solrEntity = new SolrConcept((Concept)solrObject);				
			}
			else if(solrObject.getType().compareToIgnoreCase(EntityTypes.Organization.toString())==0) {
				solrEntity = new SolrOrganization((Organization)solrObject);
			}
			else if(solrObject.getType().compareToIgnoreCase(EntityTypes.Place.toString())==0) {
				solrEntity = new SolrPlace((Place)solrObject);
			}
			else if(solrObject.getType().compareToIgnoreCase(EntityTypes.Timespan.toString())==0) {
				solrEntity = new SolrTimespan((Timespan)solrObject);
			}
			
			solrEntity.setPayload(createSolrSuggesterField(solrObject));
			
		} catch (IOException ex) {
			throw new SolrServiceException("An unexpected exception occured when creating the Solr objects from the corresponding Entity object.", ex);
		}
				
		try {			
			log.debug("Storing to Solr. Object: " + solrEntity.toString());				
			UpdateResponse rsp = solrServer.addBean(emConfiguration.getSearchApiSolrCollection(), solrEntity);
			log.info("Solr response after storing: " + rsp.toString());
			if(doCommit)
				solrServer.commit(emConfiguration.getSearchApiSolrCollection());
		} catch (SolrServerException | IOException | RuntimeException ex) {
			throw new SolrServiceException("An unexpected exception occured when storing the Entity: " + solrEntity.toString() + " to Solr.", ex);
		}
		
	}
	
		@SuppressWarnings("unused")
		@Override
	    public Entity searchById(String type, String entityId) throws SolrServiceException, UnsupportedEntityTypeException {

		log.debug("Search entity (type:" + type + " ) by id: " + entityId + " in Solr.");

		QueryResponse rsp = null;
		SolrQuery query = new SolrQuery();
		query.set("q", EntitySolrFields.ID+ ":\"" + entityId + "\"");
		try {
			rsp = solrServer.query(emConfiguration.getSearchApiSolrCollection(), query);
			log.info("query response: " + rsp.toString());
			if (rsp==null) return null;
			
		} catch (IOException | SolrServerException ex) {
		    throw new SolrServiceException("Unexpected exception occured when searching entities for id: " + entityId + " in Solr.", ex);
		}		
		
		DocumentObjectBinder binder = new DocumentObjectBinder();
		SolrDocumentList docList = rsp.getResults();
		
		if (docList==null || docList.size()==0) return null;		
		
		SolrDocument doc = docList.get(0);	
		
		if(type.compareToIgnoreCase(EntityTypes.Concept.toString())==0) {
			SolrConcept entity;
			Class<SolrConcept> entityClass = null;
			entityClass = SolrConcept.class;
			entity = (SolrConcept) binder.getBean(entityClass, doc);
	    	return entity;
		}
		else if(type.compareToIgnoreCase(EntityTypes.Agent.toString())==0) {
			SolrAgent entity;
			Class<SolrAgent> entityClass = null;
			entityClass = SolrAgent.class;
			entity = (SolrAgent) binder.getBean(entityClass, doc);
	    	return entity;
		}
		else if(type.compareToIgnoreCase(EntityTypes.Organization.toString())==0) {
			SolrOrganization entity;
			Class<SolrOrganization> entityClass = null;
			entityClass = SolrOrganization.class;
			entity = (SolrOrganization) binder.getBean(entityClass, doc);
	    	return entity;
		}
		else if(type.compareToIgnoreCase(EntityTypes.Place.toString())==0) {
			SolrPlace entity;
			Class<SolrPlace> entityClass = null;
			entityClass = SolrPlace.class;
			entity = (SolrPlace) binder.getBean(entityClass, doc);
	    	return entity;
		}
		else if(type.compareToIgnoreCase(EntityTypes.Timespan.toString())==0) {
			SolrTimespan entity;
			Class<SolrTimespan> entityClass = null;
			entityClass = SolrTimespan.class;
			entity = (SolrTimespan) binder.getBean(entityClass, doc);
	    	return entity;
		}
		else {
			return null;
		}

    }
		private String createSolrSuggesterField(Entity entity) throws JsonGenerationException, JsonMappingException, IOException {

			if(entity.getType().compareToIgnoreCase(EntityTypes.Agent.toString())==0) {
				Agent agent = new Agent((Agent)entity);				
				agent.setNote(null);
				agent.setHasPart(null);
				agent.setIsPartOfArray(null);
				agent.setDepiction(null);
				agent.setSameAs(null);
				agent.setType(null);
				agent.setEntityId(null);
				agent.setIsAggregatedBy(null);
				
				agent.setDate(null);
				agent.setIdentifier(null);
				agent.setBegin(null);
				agent.setEnd(null);
				agent.setHasMet(null);
				agent.setIsRelatedTo(null);
				agent.setWasPresentAt(null);
				agent.setName(null);
				agent.setBiographicalInformation(null);
				agent.setPlaceOfBirth(null);
				agent.setPlaceOfDeath(null);
				agent.setGender(null);
				agent.setProfessionOrOccupation(null);
				agent.setIdentifier(null);
				agent.setBegin(null);
				agent.setEnd(null);
				agent.setDate(null);
				
				ObjectNode agentJacksonNode = emJsonMapper.valueToTree(agent);
				return agentJacksonNode.toString();
			}
			else if(entity.getType().compareToIgnoreCase(EntityTypes.Organization.toString())==0) {
				Organization organization = new Organization((Organization)entity);				
				organization.setNote(null);
				organization.setHasPart(null);
				organization.setIsPartOfArray(null);
				organization.setDepiction(null);
				organization.setSameAs(null);
				organization.setType(null);
				organization.setEntityId(null);
				organization.setIsAggregatedBy(null);
				
				organization.setDescription(null);
				organization.setEuropeanaRole(null);
				organization.setHasAddress(null);
				organization.setHasGeo(null);
				organization.setHomepage(null);
				organization.setIdentifier(null);
				organization.setIsRelatedTo(null);
				organization.setLocality(null);
				organization.setLogo(null);
				organization.setMbox(null);
				organization.setNote(null);
				organization.setPhone(null);
				organization.setPostalCode(null);
				organization.setPostBox(null);
				organization.setRegion(null);
				organization.setStreetAddress(null);
				/*
				 * according to the specifications, leaving only the "en" key in the suggester 
				 */
				Map<String,List<String>> orgDomain = organization.getOrganizationDomain();
				for(Map.Entry<String, List<String>> entry : orgDomain.entrySet()) {
					if(!entry.getKey().equals("en")) {
						orgDomain.remove(entry.getKey());
					}
				}
				
				ObjectNode organizationJacksonNode = emJsonMapper.valueToTree(organization);
				return organizationJacksonNode.toString();
			}
			else if(entity.getType().compareToIgnoreCase(EntityTypes.Timespan.toString())==0) {
				Timespan timespan = new Timespan((Timespan)entity);				
				timespan.setNote(null);
				timespan.setHasPart(null);
				timespan.setIsPartOfArray(null);
				timespan.setDepiction(null);
				timespan.setSameAs(null);
				timespan.setType(null);
				timespan.setEntityId(null);
				timespan.setIsAggregatedBy(null);
				
				timespan.setIdentifier(null);
				timespan.setIsNextInSequence(null);
				timespan.setIsRelatedTo(null);
				timespan.setNote(null);	
				
				ObjectNode timespanJacksonNode = emJsonMapper.valueToTree(timespan);
				return timespanJacksonNode.toString();
			}
			
			return "";
			
		}
		

}
