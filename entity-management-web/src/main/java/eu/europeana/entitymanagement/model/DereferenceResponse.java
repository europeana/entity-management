package eu.europeana.entitymanagement.model;

/**
 * Stub class to contain de-reference response from Metis
 * This only contains fields from the XML response that are required for processing
 * entity creation requests.
 */
public class DereferenceResponse {

    private String owlSameAs;
    private String skosExactMatch;

    public DereferenceResponse(String owlSameAs, String skosExactMatch) {
        this.owlSameAs = owlSameAs;
        this.skosExactMatch = skosExactMatch;
    }


    public String getOwlSameAs() {
        return owlSameAs;
    }

    public void setOwlSameAs(String owlSameAs) {
        this.owlSameAs = owlSameAs;
    }

    public String getSkosExactMatch() {
        return skosExactMatch;
    }

    public void setSkosExactMatch(String skosExactMatch) {
        this.skosExactMatch = skosExactMatch;
    }
}
