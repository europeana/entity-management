package eu.europeana.entitymanagement.solr;

import eu.europeana.entitymanagement.definitions.model.*;
import eu.europeana.entitymanagement.solr.model.*;
import org.apache.commons.collections.MapUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class implements supporting methods for Solr*Impl classes e.g. normalization of the content
 * to match to the required output format.
 *
 */
public class SolrUtils {

	public static final String SOLR_AGENT_SUGGESTER_FILTER = "solrAgentFilter";
	public static final String SOLR_ORGANIZATION_SUGGESTER_FILTER = "solrOrganizationFilter";
	public static final String SOLR_TIMESPAN_SUGGESTER_FILTER = "solrTimespanFilter";

	/**
	 * This method adds prefixes to the fields in format Map<String, List<String>> languageMap
	 * e.g. "skos_prefLabel"
	 * @param fieldNamePrefix e.g. ConceptSolrFields.PREF_LABEL
	 * @param languageMap e.g. prefLabel
	 * @return normalized content in format Map<String, List<String>>  
	 */
    public static Map<String, List<String>> normalizeStringListMapByAddingPrefix(String fieldNamePrefix,
	    Map<String, List<String>> languageMap) {
    if(MapUtils.isEmpty(languageMap)) {
    	return new HashMap<>();
	}
	Map<String, List<String>> res;
	if (!languageMap.keySet().iterator().next().contains(fieldNamePrefix)) {
	    res = languageMap.entrySet().stream()
		    .collect(Collectors.toMap(entry -> fieldNamePrefix + entry.getKey(), Map.Entry::getValue));
	} else {
	    res = languageMap;
	}
	return res;
    }

	/**
	 * This method adds prefixes to the fields in format Map<String, String> languageMap
	 * e.g. "skos_prefLabel"
	 * @param fieldNamePrefix e.g. ConceptSolrFields.PREF_LABEL
	 * @param languageMap e.g. prefLabel
	 * @return normalized content in format Map<String, String>  
	 */
	public static Map<String, String> normalizeStringMapByAddingPrefix(String fieldNamePrefix,
			Map<String, String> languageMap) {
		
		if(MapUtils.isEmpty(languageMap)) {
			return new HashMap<>();
		}
		
		Map<String, String> res;
		if (!languageMap.keySet().iterator().next().contains(fieldNamePrefix)) {
			res = languageMap.entrySet().stream()
					.collect(Collectors.toMap(entry -> fieldNamePrefix + entry.getKey(), Map.Entry::getValue));
		} else {
			res = languageMap;
		}
		return res;
	}


	public static SolrEntity<? extends Entity> createSolrEntity(Entity entity){
        if(entity instanceof Agent){
            return new SolrAgent((Agent) entity);
        }
        else if(entity instanceof Concept){
            return new SolrConcept((Concept) entity);
        }
        else if(entity instanceof Organization){
            return new SolrOrganization((Organization) entity);
        }
        else if(entity instanceof Place){
            return new SolrPlace((Place) entity);
        }
        else if(entity instanceof Timespan){
            return new SolrTimespan((Timespan) entity);
        }

        // All possible types have been checked
       throw new IllegalArgumentException(String.format("Unrecognized entity type while creating SolrEntity: %s ",
			   entity.getClass().getName()));
    }
}
