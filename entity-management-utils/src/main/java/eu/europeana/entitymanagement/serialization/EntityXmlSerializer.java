package eu.europeana.entitymanagement.serialization;

import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.exceptions.EntityManagementRuntimeException;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.WebResource;
import eu.europeana.entitymanagement.vocabulary.EntityProfile;

@Component(AppConfigConstants.BEAN_EM_XML_SERIALIZER)
public class EntityXmlSerializer {

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
    	final String XML_HEADER_TAG_PLACE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
	    	" <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\r\n" + 
	    	"         xmlns:foaf=\"http://xmlns.com/foaf/0.1/\"\r\n" + 
	    	"         xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\r\n" + 
	    	"         xmlns:edm=\"http://www.europeana.eu/schemas/edm/\"\r\n" + 
	    	"         xmlns:wgs84_pos=\"http://www.w3.org/2003/01/geo/wgs84_pos#\"\r\n" + 
	    	"         xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\r\n" + 
	    	"         xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"\r\n" + 
	    	"	  xmlns:ore=\"http://www.openarchives.org/ore/terms/\"\r\n" +
	    	"         xmlns:dcterms=\"http://purl.org/dc/terms/\" >";
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
		JacksonXmlModule xmlModule = new JacksonXmlModule();
		xmlModule.setDefaultUseWrapper(true);
		ObjectMapper objectMapper = new XmlMapper(xmlModule);
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		String output = "";
		try {
		    output = objectMapper.writeValueAsString(entityRecord.getEntity());
		} catch (JsonProcessingException e) {
		    throw new EntityManagementRuntimeException("Unexpected exception occured when serializing entity to external format!",e);
		}
//		
//		Aggregation tmpAggregation = entityRecord.getEntity().getIsAggregatedBy();
//		List<EntityProxy> tmpProxies = entityRecord.getProxies();
//		try {
////			entityRecord.setIsAggregatedBy(null);
////			entityRecord.setProxies(null);
//			
//    		StringBuilder strBuilder = new StringBuilder();
//		    strBuilder.append(objectMapper.writeValueAsString(entityRecord.getEntity()));    		
//
//		    //add referenced web resources
//		    WebResource webResource = entityRecord.getEntity().getReferencedWebResource();
//		    if (webResource!=null)
//		    {
//		    	strBuilder.append(objectMapper.writeValueAsString(webResource));
//		    }
//
//		    strBuilder.append(XML_END_TAG);
//		    output = strBuilder.toString();
//		} catch (JsonProcessingException e) {
//		    throw new EntityManagementRuntimeException("Unexpected exception occured when serializing entity to external format!",e);
//		}
//		    
////		entityRecord.setIsAggregatedBy(tmpAggregation);
//		entityRecord.setProxies(tmpProxies);
		return output;
	}
	
	
	
    /**
	 * This method serializes EntityRecord object to xml formats for the internal profile.
	 * @param entityRecord The EntityRecord object
	 * @return The serialized entityRecord in the xml string format
	 * @throws EntityManagementRuntimeException
	 */
	public String serializeXmlInternal(EntityRecord entityRecord) throws EntityManagementRuntimeException {
		JacksonXmlModule xmlModule = new JacksonXmlModule();
		xmlModule.setDefaultUseWrapper(true);
		ObjectMapper objectMapper = new XmlMapper(xmlModule);
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		String output = "";
		
		Entity tmpEntity = entityRecord.getEntity();
		try {
			entityRecord.setEntity(null);

			StringBuilder strBuilder = new StringBuilder();
		    strBuilder.append(objectMapper.writeValueAsString(entityRecord));
		    
		    //adding the referenced web resources for the proxy entities
		    List<EntityProxy> entityRecordProxies = entityRecord.getProxies();
		    for (EntityProxy proxy : entityRecordProxies) {
		    	WebResource webResource = null;
		    	if (proxy.getEntity()!=null) {
		    		webResource= proxy.getEntity().getReferencedWebResource();
		    	}
		    	if (webResource!=null)
			    {
			    	strBuilder.append(objectMapper.writeValueAsString(webResource));
			    }
		    }
		    
		    strBuilder.append(XML_END_TAG);
		    output = strBuilder.toString();
		} catch (JsonProcessingException e) {
		    throw new EntityManagementRuntimeException("Unexpected exception occured when serializing entity to external format!",e);
		}
		    
		entityRecord.setEntity(tmpEntity);		
		return output;
	}	
}
