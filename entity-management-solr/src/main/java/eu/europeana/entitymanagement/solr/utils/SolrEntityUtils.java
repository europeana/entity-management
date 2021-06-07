package eu.europeana.entitymanagement.solr.utils;

import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.definitions.model.Timespan;
import eu.europeana.entitymanagement.solr.model.SolrAgent;
import eu.europeana.entitymanagement.solr.model.SolrConcept;
import eu.europeana.entitymanagement.solr.model.SolrEntity;
import eu.europeana.entitymanagement.solr.model.SolrOrganization;
import eu.europeana.entitymanagement.solr.model.SolrPlace;
import eu.europeana.entitymanagement.solr.model.SolrTimespan;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

public class SolrEntityUtils {


    public static SolrEntity<?> createSolrEntity(Entity entity){
        if(entity.getType().compareToIgnoreCase(EntityTypes.Agent.toString())==0){
            return new SolrAgent((Agent) entity);
        }
        else if(entity.getType().compareToIgnoreCase(EntityTypes.Concept.toString())==0){
            return new SolrConcept((Concept) entity);
        }
        else if(entity.getType().compareToIgnoreCase(EntityTypes.Organization.toString())==0){
            return new SolrOrganization((Organization) entity);
        }
        else if(entity.getType().compareToIgnoreCase(EntityTypes.Place.toString())==0){
            return new SolrPlace((Place) entity);
        }
        else if(entity.getType().compareToIgnoreCase(EntityTypes.Timespan.toString())==0){
            return new SolrTimespan((Timespan) entity);
        }
        return null;
    }
}
