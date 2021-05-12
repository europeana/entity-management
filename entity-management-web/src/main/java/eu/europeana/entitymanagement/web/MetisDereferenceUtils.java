package eu.europeana.entitymanagement.web;

import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.web.xml.model.XmlBaseEntityImpl;
import eu.europeana.entitymanagement.web.xml.model.metis.EnrichmentResultList;
import java.io.StringReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class MetisDereferenceUtils {

  private static Unmarshaller jaxbDeserializer;

  /**
   * Unmarshalls Metis dereference response into an Entity
   * @param id entity ID
   * @param metisResponseBody XML response body from Metis
   * @return Entity implementation
   * @throws EuropeanaApiException on error
   */
  public static Entity parseMetisResponse(Unmarshaller unmarshaller, String id, String metisResponseBody)
      throws EuropeanaApiException {
    EnrichmentResultList derefResult;

      try {
        derefResult = (EnrichmentResultList) unmarshaller.unmarshal(new StringReader(metisResponseBody));
      } catch (JAXBException | RuntimeException e) {
        throw new EuropeanaApiException(
            "Unexpected exception occurred when parsing metis dereference response for entity:  " + id, e);
      }


    if (derefResult == null || derefResult.getEnrichmentBaseResultWrapperList().isEmpty()
        || derefResult.getEnrichmentBaseResultWrapperList().get(0).getEnrichmentBaseList().isEmpty()) {
      // Metis returns an empty XML response if de-referencing is unsuccessful,
      // instead of throwing an error
      return null;
    }

    XmlBaseEntityImpl<?> xmlBaseEntity = derefResult.getEnrichmentBaseResultWrapperList().get(0)
        .getEnrichmentBaseList().get(0);

    return xmlBaseEntity.toEntityModel();
  }
}
