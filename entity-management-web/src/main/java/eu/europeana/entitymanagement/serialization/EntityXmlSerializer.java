package eu.europeana.entitymanagement.serialization;

import eu.europeana.entitymanagement.web.xml.model.RdfBaseWrapper;
import eu.europeana.entitymanagement.web.xml.model.XmlBaseEntityImpl;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.exceptions.EntityManagementRuntimeException;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;

@Component(AppConfigConstants.BEAN_EM_XML_SERIALIZER)
public class EntityXmlSerializer {

		private final JAXBContext jaxbContext;

		@Autowired
	public EntityXmlSerializer(JAXBContext jaxbContext) {
		this.jaxbContext = jaxbContext;
	}


	/**
	 * This method serializes EntityRecord object to xml formats for the external profile.
	 * @param xmlWrapper xml object for entity
	 * @return The serialized entityRecord in the xml string format
	 * @throws EntityManagementRuntimeException
	 */
	public String serializeXmlExternal(RdfBaseWrapper xmlWrapper) throws EntityManagementRuntimeException {
		StringWriter sw = new StringWriter();

		try {
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(xmlWrapper, sw);
		} catch (JAXBException e) {
			throw new EntityManagementRuntimeException(String.format("Error serializing xml; about=%s ", xmlWrapper.getXmlEntity().getAbout()), e);
		}

		return sw.toString();
	}
}
