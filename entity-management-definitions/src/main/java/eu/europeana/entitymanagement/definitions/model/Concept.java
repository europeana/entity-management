package eu.europeana.entitymanagement.definitions.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.europeana.entitymanagement.definitions.model.impl.ConceptImpl;

@JsonDeserialize(as = ConceptImpl.class)
public interface Concept extends Entity, eu.europeana.corelib.definitions.edm.entity.Concept {

	public String[] getBroader();
	
	public String[] getNarrower();
	
	public String[] getRelated();
	
	public String[] getBroadMatch();
	
	public String[] getNarrowMatch();
	
	public String[] getExactMatch();
	
	public String[] getCoref();
	
	public String[] getRelatedMatch();
	
	public String[] getCloseMatch();
	
	public String[] getInScheme();
	
	public Map<String, List<String>> getNotation();
	
	public String getType();
	
    void setNotation(Map<String, List<String>> notation);

    void setInScheme(String[] inScheme);

    void setCloseMatch(String[] closeMatch);

    void setRelatedMatch(String[] relatedMatch);

    void setCoref(String[] coref);

    void setExactMatch(String[] exactMatch);

    void setNarrowMatch(String[] narrowMatch);

    void setBroadMatch(String[] broadMatch);

    void setRelated(String[] related);

    void setNarrower(String[] narrower);

    void setBroader(String[] broader);

//    void setTimestamp(Date timestamp);
//
//    Date getTimestamp();

}
