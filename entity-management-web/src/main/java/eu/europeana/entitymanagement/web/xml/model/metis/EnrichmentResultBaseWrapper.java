package eu.europeana.entitymanagement.web.xml.model.metis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import eu.europeana.entitymanagement.definitions.model.impl.AgentImpl;
import eu.europeana.entitymanagement.definitions.model.impl.ConceptImpl;
import eu.europeana.entitymanagement.definitions.model.impl.EntityImpl;
import eu.europeana.entitymanagement.definitions.model.impl.PlaceImpl;
import eu.europeana.entitymanagement.definitions.model.impl.TimespanImpl;

/**
 * Root element for Metis de-reference response
 */
@XmlRootElement(namespace = "http://www.europeana.eu/schemas/metis", name = "result")
@XmlAccessorType(XmlAccessType.FIELD)
public class EnrichmentResultBaseWrapper {

    @XmlElements(value = {
            @XmlElement(name = "Concept", namespace = "http://www.w3.org/2004/02/skos/core#", type = ConceptImpl.class),
            @XmlElement(name = "Agent", namespace = "http://www.europeana.eu/schemas/edm/", type = AgentImpl.class),
            @XmlElement(name = "Place", namespace = "http://www.europeana.eu/schemas/edm/", type = PlaceImpl.class),
            @XmlElement(name = "Timespan", namespace = "http://www.europeana.eu/schemas/edm/", type = TimespanImpl.class)})
    private List<EntityImpl> xmlEntities = new ArrayList<>();


    public EnrichmentResultBaseWrapper() {
    }

    /**
     * Constructor with all fields
     *
     * @param enrichmentBase the enrichment information class generated
     */
    public EnrichmentResultBaseWrapper(List<EntityImpl> enrichmentBase) {
        this.xmlEntities = new ArrayList<>(enrichmentBase);
    }

    public List<EntityImpl> getEnrichmentBaseList() {
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
            Collection<List<EntityImpl>> resultList) {
        return resultList.stream().map(EnrichmentResultBaseWrapper::new).collect(Collectors.toList());
    }
}
