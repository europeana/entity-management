package eu.europeana.entitymanagement.web.xml.model.metis;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


/**
 * Contains a list of {@link EnrichmentResultBaseWrapper} results.
 * Xml root for response received from Metis
 */
@XmlRootElement(namespace = "http://www.europeana.eu/schemas/metis", name = "results")
@XmlAccessorType(XmlAccessType.FIELD)
public class EnrichmentResultList {

    @XmlElement(namespace = "http://www.europeana.eu/schemas/metis", name = "enrichmentBaseWrapperList")
    private List<EnrichmentResultBaseWrapper> enrichmentResultBaseWrapperList = new ArrayList<>();


    public EnrichmentResultList() {
    }

    /**
     * Constructor with initial {@link } list.
     *
     * @param enrichmentResultBaseWrappers the list to initialize the class with
     */
    public EnrichmentResultList(List<EnrichmentResultBaseWrapper> enrichmentResultBaseWrappers) {
        this.enrichmentResultBaseWrapperList.addAll(enrichmentResultBaseWrappers);
    }

    public List<EnrichmentResultBaseWrapper> getEnrichmentBaseResultWrapperList() {
        return new ArrayList<>(enrichmentResultBaseWrapperList);
    }
}
