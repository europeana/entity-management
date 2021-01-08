package eu.europeana.entity.edm.external;

import java.util.List;
import java.util.Map;

public class TimeSpan {

    private String rdfAbout;
    private String rdfType;
    private String foafDepiction;
    private String isShownBy;
    private Map<String, List<String>> skosPrefLabel;
    private Map<String, List<String>> skosAltLabel;

    private String edmBegin;
    private String edmEnd;
    private Map<String, List<String>> skosNote;
    private Map<String, List<String>> dcTermsHasPart;
    private Map<String, List<String>> dcTermsIsPartOf;
    private Map<String, List<String>> owlSameAs;


}
