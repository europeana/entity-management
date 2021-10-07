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

import eu.europeana.entitymanagement.web.xml.model.XmlAgentImpl;
import eu.europeana.entitymanagement.web.xml.model.XmlBaseEntityImpl;
import eu.europeana.entitymanagement.web.xml.model.XmlConceptImpl;
import eu.europeana.entitymanagement.web.xml.model.XmlOrganizationImpl;
import eu.europeana.entitymanagement.web.xml.model.XmlPlaceImpl;
import eu.europeana.entitymanagement.web.xml.model.XmlTimespanImpl;

/**
 * Root element for Metis de-reference response
 */
//@XmlRootElement(namespace = "http://www.europeana.eu/schemas/metis", name = "result")
@XmlAccessorType(XmlAccessType.FIELD)
public class EnrichmentResultBaseWrapper {

    @XmlElements(value = {
            @XmlElement(name = "Concept", namespace = "http://www.w3.org/2004/02/skos/core#", type = XmlConceptImpl.class),
            @XmlElement(name = "Agent", namespace = "http://www.europeana.eu/schemas/edm/", type = XmlAgentImpl.class),
            @XmlElement(name = "Place", namespace = "http://www.europeana.eu/schemas/edm/", type = XmlPlaceImpl.class),
            @XmlElement(name = "Organization", namespace = "http://www.europeana.eu/schemas/edm/", type = XmlOrganizationImpl.class),
            @XmlElement(name = "TimeSpan", namespace = "http://www.europeana.eu/schemas/edm/", type = XmlTimespanImpl.class)})
    private List<XmlBaseEntityImpl<?>> xmlEntities = new ArrayList<>();


    public EnrichmentResultBaseWrapper() {
    }

    /**
     * Constructor with all fields
     *
     * @param enrichmentBase the enrichment information class generated
     */
    public EnrichmentResultBaseWrapper(List<XmlBaseEntityImpl<?>> enrichmentBase) {
        this.xmlEntities = new ArrayList<>(enrichmentBase);
    }

    public List<XmlBaseEntityImpl<?>> getEnrichmentBaseList() {
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
            Collection<List<XmlBaseEntityImpl<?>>> resultList) {
        return resultList.stream().map(EnrichmentResultBaseWrapper::new).collect(Collectors.toList());
    }
}
