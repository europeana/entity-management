package eu.europeana.entitymanagement.serialization;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.WebResource;
import eu.europeana.entitymanagement.exception.EntityManagementRuntimeException;
import eu.europeana.entitymanagement.vocabulary.EntityProfile;

@Component
public class JsonLdSerializer {

    ObjectMapper mapper;
    public static final String DATE_FORMAT  = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    
    public JsonLdSerializer() {
	mapper = new ObjectMapper();
	SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
	mapper.setDateFormat(df);
    }
    
    
    public String serialize(EntityRecord record, String profile) throws EntityManagementRuntimeException {
	return serialize(record, EntityProfile.valueOf(profile));
    }

    public String serialize(EntityRecord record, EntityProfile profile) throws EntityManagementRuntimeException {
	String res = null;
	switch (profile) {
	case internal:
	    res = serializeInternal(record);
	    break;
	case external:
	    res = serializeExternal(record);
	    break;
	default:
	    throw new EntityManagementRuntimeException("Serialization not supported for profile:" + profile);
	}
	return res;
    }


    private String serializeExternal(EntityRecord record) throws EntityManagementRuntimeException {
	try {
	    return mapper.writeValueAsString(record.getEntity());
//		Aggregation tmpAggregation=record.getIsAggregatedBy();
//		List<EntityProxy> proxies=record.getProxies();
//		record.setIsAggregatedBy(null);
//		record.setProxies(null);
//				
//		StringBuilder builder = new StringBuilder();
//		builder.append("{");
//		builder.append("\"entityRecord\":");
//		builder.append(mapper.writeValueAsString(record));    		
//
//	    //adding the referenced web resource
//	    WebResource additionalElementsToSerialize = record.getEntity().getReferencedWebResource();
//		if (additionalElementsToSerialize!=null)
//		{
//	    	builder.append(",");
//	    	builder.append("\"webResource\":");
//			builder.append(mapper.writeValueAsString(additionalElementsToSerialize));
//		}
//		builder.append("}");
//	    
//		record.setIsAggregatedBy(tmpAggregation);
//		record.setProxies(proxies);
//	    return builder.toString();
	    
	} catch (JsonProcessingException e) {
	    throw new EntityManagementRuntimeException("Unexpected exception occured when serializing entity to external format!",e);
	}
    }

    private String serializeInternal(EntityRecord record) throws EntityManagementRuntimeException {
		try {
			Entity tmpEntity = record.getEntity();
			record.setEntity(null);

			StringBuilder builder = new StringBuilder();
			builder.append("{");
			builder.append("\"entityRecord\":");
			builder.append(mapper.writeValueAsString(record));
			
		    //adding the referenced web resources for the proxy entities
			List<EntityProxy> entityRecordProxies = record.getProxies();		    
		    for (EntityProxy proxy : entityRecordProxies) {
		    	WebResource additionalElementsToSerialize = proxy.getEntity().getReferencedWebResource();
			    if (additionalElementsToSerialize!=null)
			    {
			    	builder.append(",");
			    	builder.append("\"webResource\":");
			    	builder.append(mapper.writeValueAsString(additionalElementsToSerialize));
			    }
		    }
		    builder.append("}");
		    
		    record.setEntity(tmpEntity);
			return builder.toString();
			
		} catch (JsonProcessingException e) {
		    throw new EntityManagementRuntimeException("Unexpected exception occured when serializing entity to external format!",e);
		}
    }

}