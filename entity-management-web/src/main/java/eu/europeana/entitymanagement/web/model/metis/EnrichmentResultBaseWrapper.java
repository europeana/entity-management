package eu.europeana.entitymanagement.web.model.metis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import eu.europeana.entitymanagement.definitions.model.impl.BaseAgent;
import eu.europeana.entitymanagement.definitions.model.impl.BaseConcept;
import eu.europeana.entitymanagement.definitions.model.impl.BaseEntity;
import eu.europeana.entitymanagement.definitions.model.impl.BasePlace;
import eu.europeana.entitymanagement.definitions.model.impl.BaseTimespan;

/**
 * Root element for Metis de-reference response
 */
@XmlRootElement(namespace = "http://www.europeana.eu/schemas/metis", name = "result")
@XmlAccessorType(XmlAccessType.FIELD)
public class EnrichmentResultBaseWrapper {

    @XmlElements(value = {
            @XmlElement(name = "Concept", namespace = "http://www.w3.org/2004/02/skos/core#", type = BaseConcept.class),
            @XmlElement(name = "Agent", namespace = "http://www.europeana.eu/schemas/edm/", type = BaseAgent.class),
            @XmlElement(name = "Place", namespace = "http://www.europeana.eu/schemas/edm/", type = BasePlace.class),
            @XmlElement(name = "Timespan", namespace = "http://www.europeana.eu/schemas/edm/", type = BaseTimespan.class)})
    private List<BaseEntity> xmlEntities = new ArrayList<>();


    public EnrichmentResultBaseWrapper() {
    }

    /**
     * Constructor with all fields
     *
     * @param enrichmentBase the enrichment information class generated
     */
    public EnrichmentResultBaseWrapper(List<BaseEntity> enrichmentBase) {
        this.xmlEntities = new ArrayList<>(enrichmentBase);
    }

    public List<BaseEntity> getEnrichmentBaseList() {
        return new ArrayList<>(xmlEntities);
    }

    /**
     * Convert a collection of {@link XmlBaseEntityImpl} to a list of {@link
     * EnrichmentResultBaseWrapper}.
     * <p>This is mostly used for dereferencing.</p>
     *
     * @param resultList the collection of {@link XmlBaseEntityImpl}
     * @return the converted list
     */
    public static List<EnrichmentResultBaseWrapper> createEnrichmentResultBaseWrapperList(
            Collection<List<BaseEntity>> resultList) {
        return resultList.stream().map(EnrichmentResultBaseWrapper::new).collect(Collectors.toList());
    }
}
