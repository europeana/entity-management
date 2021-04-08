package eu.europeana.entitymanagement.serialization;

import eu.europeana.entitymanagement.definitions.model.impl.BaseEntity;
import eu.europeana.entitymanagement.serialization.mixins.XmlIgnoreFieldsMixin;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.exceptions.EntityManagementRuntimeException;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.vocabulary.EntityProfile;

@Component(AppConfigConstants.BEAN_EM_XML_SERIALIZER)
public class EntityXmlSerializer {

	private final static XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();

	private final XmlMapper xmlMapper;

	public EntityXmlSerializer() {
		JacksonXmlModule xmlModule = new JacksonXmlModule();
		xmlModule.setDefaultUseWrapper(false);
		xmlMapper = new XmlMapper(xmlModule);
		xmlMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		xmlMapper.addMixIn(BaseEntity.class, XmlIgnoreFieldsMixin.class);
	}

	final String XML_HEADER_TAG_CONCEPT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
    	    	" <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\r\n" +  
    	    	"         xmlns:foaf=\"http://xmlns.com/foaf/0.1/\"\r\n" + 
    	    	"         xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\r\n" + 
    	    	"         xmlns:edm=\"http://www.europeana.eu/schemas/edm/\"\r\n" + 
    	    	"	  xmlns:ore=\"http://www.openarchives.org/ore/terms/\"\r\n" +
    	    	"         xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"\r\n " +
		"         xmlns:dcterms=\"http://purl.org/dc/terms/\" >";
    	final String XML_HEADER_TAG_AGENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
		" <rdf:RDF xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + 
		"         xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\r\n" + 
		"         xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\r\n" + 
		"         xmlns:edm=\"http://www.europeana.eu/schemas/edm/\"\r\n" + 
		"         xmlns:foaf=\"http://xmlns.com/foaf/0.1/\"\r\n" + 
		"         xmlns:rdaGr2=\"http://rdvocab.info/ElementsGr2/\"\r\n" + 
		"         xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\r\n" + 
		"	  xmlns:ore=\"http://www.openarchives.org/ore/terms/\"\r\n" +
		"         xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"\r\n" + 
		"         xmlns:dcterms=\"http://purl.org/dc/terms/\" >";
    	final String XML_HEADER_TAG_PLACE = "?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
	    	" <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\r\n" + 
	    	"         xmlns:foaf=\"http://xmlns.com/foaf/0.1/\"\r\n" + 
	    	"         xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\r\n" + 
	    	"         xmlns:edm=\"http://www.europeana.eu/schemas/edm/\"\r\n" + 
	    	"         xmlns:wgs84_pos=\"http://www.w3.org/2003/01/geo/wgs84_pos#\"\r\n" + 
	    	"         xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\r\n" + 
	    	"         xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"\r\n" + 
	    	"	  xmlns:ore=\"http://www.openarchives.org/ore/terms/\"\r\n" +
	    	"         xmlns:dcterms=\"http://purl.org/dc/terms/\"";
    	final String XML_HEADER_TAG_ORGANIZATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
	    	" <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\r\n" + 
	    	"         xmlns:foaf=\"http://xmlns.com/foaf/0.1/\"\r\n" + 
	    	"         xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\r\n" + 
	    	"         xmlns:edm=\"http://www.europeana.eu/schemas/edm/\"\r\n" + 
	    	"         xmlns:vcard=\"https://www.w3.org/2006/vcard/\"\r\n" + 
	    	"         xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\r\n" + 
	    	"	  xmlns:ore=\"http://www.openarchives.org/ore/terms/\"\r\n" +
	    	"         xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"\r\n " +
		"         xmlns:dcterms=\"http://purl.org/dc/terms/\" >";
    	final String XML_END_TAG = "</rdf:RDF>";
    	final String XML_HEADER_TAG_TIMESPAN = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
		" <rdf:RDF xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + 
		"         xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\r\n" + 
		"         xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\r\n" + 
		"         xmlns:edm=\"http://www.europeana.eu/schemas/edm/\"\r\n" + 
		"         xmlns:foaf=\"http://xmlns.com/foaf/0.1/\"\r\n" + 
		"         xmlns:rdaGr2=\"http://rdvocab.info/ElementsGr2/\"\r\n" + 
		"         xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\r\n" + 
		"	  xmlns:ore=\"http://www.openarchives.org/ore/terms/\"\r\n" +
		"         xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"\r\n" + 
		"         xmlns:dcterms=\"http://purl.org/dc/terms/\" >";
    
    public String serializeXml(EntityRecord record, String profileString) throws EntityManagementRuntimeException {
    	EntityProfile profile = EntityProfile.valueOf(profileString);
    	String res = null;
    	switch (profile) {
    	case internal:
    	    res = serializeXmlInternal(record);
    	    break;
    	case external:
    	    res = serializeXmlExternal(record);
    	    break;
    	default:
    	    throw new EntityManagementRuntimeException("Serialization not supported for profile:" + profile);
    	}
    	return res;	    	
   	}

    /**
	 * This method serializes EntityRecord object to xml formats for the external profile.
	 * @param entityRecord The EntityRecord object
	 * @return The serialized entityRecord in the xml string format
     * @throws EntityManagementRuntimeException 
	 */
	public String serializeXmlExternal(EntityRecord entityRecord) throws EntityManagementRuntimeException {
		StringWriter stringWriter = new StringWriter();
		try {
			XMLStreamWriter sw = xmlOutputFactory.createXMLStreamWriter(stringWriter);

			sw.writeStartDocument();
			SerializationUtils.serializeExternalXml(sw, xmlMapper, entityRecord);
			sw.writeEndDocument();
			return stringWriter.toString();
		} catch (IOException | XMLStreamException e) {
			throw new EntityManagementRuntimeException("Unexpected exception occured when serializing entity to external format!",e);
		}
	}



	/**
	 * This method serializes EntityRecord object to xml formats for the internal profile.
	 * @param entityRecord The EntityRecord object
	 * @return The serialized entityRecord in the xml string format
	 * @throws EntityManagementRuntimeException
	 */
	public String serializeXmlInternal(EntityRecord entityRecord)
			throws EntityManagementRuntimeException {
		StringWriter stringWriter = new StringWriter();
		try {
		XMLStreamWriter sw = xmlOutputFactory.createXMLStreamWriter(stringWriter);
		SerializationUtils.serializeInternalXml(sw, xmlMapper, entityRecord);
			return stringWriter.toString();
		} catch (IOException | XMLStreamException e) {
		    throw new EntityManagementRuntimeException("Unexpected exception occured when serializing entity to external format!",e);
		}
	}	
}
