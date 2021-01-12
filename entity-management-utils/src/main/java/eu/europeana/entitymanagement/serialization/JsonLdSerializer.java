package eu.europeana.entitymanagement.serialization;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.europeana.entity.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.exception.EntityManagementRuntimeException;
import eu.europeana.entitymanagement.vocabulary.EntityProfile;

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
	    serializeInternal(record);
	    break;
	case external:
	    serializeExternal(record);
	    break;
	default:
	    throw new EntityManagementRuntimeException("Serialization not supported for profile:" + profile);
	}
	return res;
    }


    private String serializeExternal(EntityRecord record) throws EntityManagementRuntimeException {
	try {
	    return mapper.writeValueAsString(record.getEntity());
	} catch (JsonProcessingException e) {
	    throw new EntityManagementRuntimeException("Unexpected exception occured when serializing entity to external format!",e);
	}
    }

    private void serializeInternal(EntityRecord record) throws EntityManagementRuntimeException {
	try {
	StringBuilder builder = new StringBuilder();
	String metadata = mapper.writeValueAsString(record.getIsAggregatedBy());
	//remove the last }
	builder.append(StringUtils.chomp(metadata));
	String aggregation = mapper.writeValueAsString(record.getIsAggregatedBy());
	builder.append(",\n");
	builder.append("\"isAggregatedBy: \"").append(aggregation);
	builder.append(",\n");
	String proxies = mapper.writeValueAsString(record.getProxies());
	builder.append("\"proxies: \"").append(proxies);
	builder.append("\n}");
	} catch (JsonProcessingException e) {
	    throw new EntityManagementRuntimeException("Unexpected exception occured when serializing entity to external format!",e);
	}
    }

}