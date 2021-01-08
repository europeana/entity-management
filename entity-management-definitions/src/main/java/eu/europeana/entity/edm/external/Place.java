package eu.europeana.entity.edm.external;

import java.util.List;
import java.util.Map;

public class Place {
    private String rdfAbout;
    private String rdfType;
    private String foafDepiction;
    private String isShownBy;
    private Map<String, List<String>> skosPrefLabel;
    private Map<String, List<String>> skosAltLabel;
    private Map<String, List<String>> skosHiddenLabel;
    private Map<String, List<String>> skosNote;
    private Map<String, List<String>> dcTermsHasPart;
    private Map<String, List<String>> dcTermsIsPartOf;
    private Map<String, List<String>> owlSameAs;

    private Long wgs84PosLat;
    private Long wgs84PosLong;
    private Long wgs84PosAlt;
    private String wgs84PosLatLong;
    private Map<String, List<String>> edmIsNextInSequence;

}
