package eu.europeana.entitymanagement.solr.service.impl;

import java.io.IOException;
import java.util.List;

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
import org.springframework.stereotype.Service;

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
import eu.europeana.entitymanagement.utils.EntityObjectFactory;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

@Service(AppConfigConstants.BEAN_EM_SOLR_SERVICE)
public class SolrServiceImpl implements SolrService {

	@Autowired
	SolrClient solrServer;
	
	@Autowired
	EntityManagementConfiguration emConfiguration;
	
	private final Logger log = LogManager.getLogger(getClass());

	@Override
	public void storeEntity(Entity solrObject, boolean doCommit) throws SolrServiceException {
		Entity solrEntity = null;
		if(solrObject instanceof Agent) {
			solrEntity = new SolrAgent((Agent)solrObject);
		}
		else if(solrObject instanceof Concept) {
			solrEntity = new SolrConcept((Concept)solrObject);
		}
		else if(solrObject instanceof Organization) {
			solrEntity = new SolrOrganization((Organization)solrObject);
		}
		else if(solrObject instanceof Place) {
			solrEntity = new SolrPlace((Place)solrObject);
		}
		else if(solrObject instanceof Timespan) {
			solrEntity = new SolrTimespan((Timespan)solrObject);
		}
				
		try {			
			log.debug("Storing to Solr. Object: " + solrEntity.toString());				
			UpdateResponse rsp = solrServer.addBean(emConfiguration.getSearchApiSolrCollection(), solrEntity);
			log.info("Solr response after storing: " + rsp.toString());
			if(doCommit)
				solrServer.commit(emConfiguration.getSearchApiSolrCollection());
		} catch (SolrServerException ex) {
			throw new SolrServiceException(
					"An unexpected Solr server exception occured when storing the Entity: " + solrEntity.toString(),
					ex);
		} catch (IOException ex) {
			throw new SolrServiceException(
					"An unexpected IO exception occured when storing the Entity: " + solrEntity.toString() + " in Solr.", ex);
		} catch (RuntimeException ex) {
			throw new SolrServiceException(
					"An unexpected runtime exception occured when storing the Entity: " + solrEntity.toString() + " in Solr.", ex);			
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
			
		} catch (IOException | SolrServerException e) {
		    throw new SolrServiceException(
				    "Unexpected exception occured when searching entities for id: " + entityId + " in Solr.", e);
		}		
		
		DocumentObjectBinder binder = new DocumentObjectBinder();
		SolrDocumentList docList = rsp.getResults();
		
		if (docList==null) return null;		
		
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

}
