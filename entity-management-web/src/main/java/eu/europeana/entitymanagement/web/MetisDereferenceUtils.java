package eu.europeana.entitymanagement.web;

import java.io.StringReader;
import java.util.List;
import java.util.Optional;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.exception.DatasourceDereferenceException;
import eu.europeana.entitymanagement.web.xml.model.XmlBaseEntityImpl;
import eu.europeana.entitymanagement.web.xml.model.metis.EnrichmentResultList;

public class MetisDereferenceUtils {

  /**
   * Unmarshalls Metis dereference response into an Entity
   *
   * @param id entity ID
   * @param metisResponseBody XML response body from Metis
   * @return Entity implementation
   * @throws EuropeanaApiException on error
   */
  public static XmlBaseEntityImpl<?> parseMetisResponse(
      Unmarshaller unmarshaller, String id, String metisResponseBody) throws EuropeanaApiException {
    EnrichmentResultList derefResult;

    try {
      derefResult =
          (EnrichmentResultList) unmarshaller.unmarshal(new StringReader(metisResponseBody));
    } catch (JAXBException | RuntimeException e) {
      throw new DatasourceDereferenceException(
          String.format(
              "Error while deserializing metis dereference response %s: ", metisResponseBody),
          e);
    }

    if (derefResult == null
        || derefResult.getEnrichmentBaseResultWrapperList().isEmpty()
        || derefResult
            .getEnrichmentBaseResultWrapperList()
            .get(0)
            .getEnrichmentBaseList()
            .isEmpty()) {
      // Metis returns an empty XML response if de-referencing is unsuccessful,
      // instead of throwing an error
      return null;
    }

    Optional<XmlBaseEntityImpl<?>> entityOptional =
        derefResult.getEnrichmentBaseResultWrapperList().get(0).getEnrichmentBaseList().stream()
            // Metis sometimes returns multiple entities in result. Make sure the correct one is
            // picked.
            .filter(e -> (id.equals(e.getAbout()) || e.hasCoref(id)))
            .findFirst();

    if (entityOptional.isEmpty()) {
      throw new DatasourceDereferenceException(
          String.format("No match in Metis dereference response for uri '%s'", id));
    }

    return entityOptional.get();
  }
  
  public static List<XmlBaseEntityImpl<?>> parseMetisResponseMany(
      Unmarshaller unmarshaller, String metisResponseBody) throws EuropeanaApiException {
    EnrichmentResultList derefResult;

    try {
      derefResult =
          (EnrichmentResultList) unmarshaller.unmarshal(new StringReader(metisResponseBody));
    } catch (JAXBException | RuntimeException e) {
      throw new DatasourceDereferenceException(
          String.format(
              "Error while deserializing metis dereference response %s: ", metisResponseBody),
          e);
    }

    if (derefResult == null
        || derefResult.getEnrichmentBaseResultWrapperList().isEmpty()
        || derefResult
            .getEnrichmentBaseResultWrapperList()
            .get(0)
            .getEnrichmentBaseList()
            .isEmpty()) {
      // Metis returns an empty XML response if de-referencing is unsuccessful,
      // instead of throwing an error
      return null;
    }

    return derefResult.getEnrichmentBaseResultWrapperList().get(0).getEnrichmentBaseList();
    
  }
}
