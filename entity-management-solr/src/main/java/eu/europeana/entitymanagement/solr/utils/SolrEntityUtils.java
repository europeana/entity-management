package eu.europeana.entitymanagement.solr.utils;

import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.solr.model.SolrAgent;
import eu.europeana.entitymanagement.solr.model.SolrEntity;

public class SolrEntityUtils {


    public static SolrEntity<?> createSolrEntity(Entity entity){
        if(entity instanceof Agent){
            return new SolrAgent((Agent) entity);
        }

        // TODO: convert other types

        return null;
    }
}
