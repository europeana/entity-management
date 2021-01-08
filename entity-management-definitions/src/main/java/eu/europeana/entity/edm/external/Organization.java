package eu.europeana.entity.edm.external;

import java.util.List;
import java.util.Map;

public class Organization {
    private String rdfAbout;
    private String rdfType;
    private String foafDepiction;

    private Map<String, List<String>> skosPrefLabel;
    private Map<String, List<String>> edmAcronym;

    private Map<String, List<String>> skosAltLabel;
    private String dcDescription;
    private String foafLogo;

    private Map<String, List<String>> edmEuropeanaRole;
    private Map<String, List<String>> edmOrganizationDomain;
    private String edmGeographicalLevel;
    private String edmCountry;
    private String foafHomepage;
    private List<String> foafPhone;
    private List<String> foafMbox;
    private String vcardHasAddress;
    private List<String> dcIdentifier;
    private List<String> sameAs;


    private class VcardAddress {
        private String rdfAbout;
        private String rdfType;

        private String vcardStreetAddress;
        private String vcardPostalCode;
        private String vcardPostOfficeBox;
        private String vcardLocality;
        private String vcardRegion;
        private String vcardCountryName;
        private String vcardHasGeo;
    }
}
