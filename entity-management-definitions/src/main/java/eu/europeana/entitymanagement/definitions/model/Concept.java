package eu.europeana.entitymanagement.definitions.model;

import java.util.List;
import java.util.Map;

public interface Concept extends Entity, eu.europeana.corelib.definitions.edm.entity.Concept {

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
