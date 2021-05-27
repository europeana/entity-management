package eu.europeana.entitymanagement.solr.service;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.solr.exception.SolrServiceException;

public interface SolrService {

	/**
	 * This method stores objects in SOLR.
	 * @param solrObject
	 * @param doCommit commit
	 * @throws SolrServiceException 
	 */
	public void storeEntity(Entity solrObject, boolean doCommit) throws SolrServiceException ;	

}
