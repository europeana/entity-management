package eu.europeana.entity.edm.external;

import java.util.List;
import java.util.Map;

public class Agent {

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

    private String edmBegin;
    private String edmEnd;


    private Map<String, List<String>> foafName;


    private String rdaGr2DateOfBirth;
    private String rdaGr2DateOfEstablishment;
    private String rdaGr2DateOfDeath;
    private String rdaGr2DateOfTermination;
    private Map<String, List<String>> dcDate;
    private String rdaGr2PlaceOfBirth;
    private String rdaGr2PlaceOfDeath;
    private String rdaGr2Gender;
    private Map<String, List<String>> rdaGr2ProfessionOrOccupation;
    private Map<String, List<String>> rdaGr2BiographicalInformation;
    private Map<String, List<String>> edmHasMet;
    private Map<String, List<String>> edmIsRelatedTo;
    private Map<String, List<String>> edmWasPresentAt;
    private Map<String, List<String>> dcIdentifier;


}
