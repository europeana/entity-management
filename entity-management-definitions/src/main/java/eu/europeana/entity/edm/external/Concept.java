package eu.europeana.entity.edm.external;

import java.util.List;
import java.util.Map;

public class Concept {
    private String rdfAbout;
    private String rdfType;
    private String foafDepiction;
    private String isShownBy;
    private Map<String, List<String>> skosPrefLabel;
    private Map<String, List<String>> skosAltLabel;
    private Map<String, List<String>> skosHiddenLabel;
    private Map<String, List<String>> skosNote;
    private Map<String, List<String>> skosNotation;
    private Map<String, List<String>> skosBroader;
    private Map<String, List<String>> skosNarrower;
    private Map<String, List<String>> skosRelated;
    private Map<String, List<String>> skosBroadMatch;
    private Map<String, List<String>> skosNarrowMatch;
    private Map<String, List<String>> skosRelatedMatch;
    private Map<String, List<String>> skosCloseMatch;
    private Map<String, List<String>> skosExactMatch;
    private Map<String, List<String>> skosInScheme;

}