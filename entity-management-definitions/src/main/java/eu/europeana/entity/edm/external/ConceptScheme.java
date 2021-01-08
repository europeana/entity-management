package eu.europeana.entity.edm.external;

import java.util.List;
import java.util.Map;

public class ConceptScheme {
    private String rdfAbout;
    private String rdfType;

    private Map<String, List<String>> skosPrefLabel;
    private Map<String, List<String>> skosDefinition;
    private String rdfIsDefinedBy;

    private List<String> sameAs;
}
