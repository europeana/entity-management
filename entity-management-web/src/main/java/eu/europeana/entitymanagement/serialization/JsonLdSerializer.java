package eu.europeana.entitymanagement.serialization;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.exceptions.EntityManagementRuntimeException;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.vocabulary.EntityProfile;

@Component(AppConfigConstants.BEAN_EM_JSONLD_SERIALIZER)
public class JsonLdSerializer {

    ObjectMapper mapper;
    public static final String DATE_FORMAT  = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    
    public JsonLdSerializer() {
	mapper = new ObjectMapper();
	SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
	mapper.setDateFormat(df);
	
	SimpleFilterProvider dummy = new SimpleFilterProvider();
    dummy.setFailOnUnknownId(false);
    mapper.setFilterProvider(dummy);
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
		final StringWriter buffer = new StringWriter();
		try {
			SerializationUtils.serializeExternalJson(buffer, mapper, record);
		} catch (IOException e) {
			throw new EntityManagementRuntimeException("Unexpected exception occurred when serializing entity to external format",e);
		}
		return buffer.toString();
	}

    private String serializeInternal(EntityRecord record) throws EntityManagementRuntimeException {
			final StringWriter buffer = new StringWriter();
			try {
				SerializationUtils.serializeInternalJson(buffer, mapper, record);
			} catch (IOException e) {
				throw new EntityManagementRuntimeException("Unexpected exception occurred when serializing entity to internal format!",e);

			}
			return buffer.toString();
    }

}