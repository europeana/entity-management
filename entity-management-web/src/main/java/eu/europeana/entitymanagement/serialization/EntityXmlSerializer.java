package eu.europeana.entitymanagement.serialization;

import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.exceptions.EntityManagementRuntimeException;
import eu.europeana.entitymanagement.exception.FunctionalRuntimeException;
import eu.europeana.entitymanagement.web.xml.model.RdfBaseWrapper;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(AppConfigConstants.BEAN_EM_XML_SERIALIZER)
public class EntityXmlSerializer implements InitializingBean {

  private final JAXBContext jaxbContext;

  /** Create a separate JAXB marshaller for each thread */
  private ThreadLocal<Marshaller> marshaller;

  @Autowired
  public EntityXmlSerializer(JAXBContext jaxbContext) {
    this.jaxbContext = jaxbContext;
  }

  @Override
  public void afterPropertiesSet() {
    marshaller =
        ThreadLocal.withInitial(
            () -> {
              try {
                Marshaller newMarshaller = jaxbContext.createMarshaller();
                newMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                return newMarshaller;
              } catch (JAXBException e) {
                throw new FunctionalRuntimeException("Error creating JAXB unmarshaller ", e);
              }
            });
  }

  /**
   * This method serializes EntityRecord object to xml formats for the external profile.
   *
   * @param xmlWrapper xml object for entity
   * @return The serialized entityRecord in the xml string format
   * @throws EntityManagementRuntimeException on error
   */
  public String serializeXmlExternal(RdfBaseWrapper xmlWrapper)
      throws EntityManagementRuntimeException {
    StringWriter sw = new StringWriter();

    try {
      marshaller.get().marshal(xmlWrapper, sw);
    } catch (JAXBException e) {
      throw new EntityManagementRuntimeException(
          String.format("Error serializing xml; about=%s ", xmlWrapper.getXmlEntity().getAbout()),
          e);
    }

    return sw.toString();
  }
}
