package eu.europeana.entitymanagement.solr.service.impl;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
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
		}
		
	}

}
