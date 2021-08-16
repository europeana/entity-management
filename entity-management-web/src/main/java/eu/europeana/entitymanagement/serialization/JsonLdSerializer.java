package eu.europeana.entitymanagement.serialization;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.BEAN_JSON_MAPPER;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import eu.europeana.corelib.edm.model.schemaorg.Text;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.exceptions.EntityManagementRuntimeException;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.vocabulary.EntityProfile;
import eu.europeana.entitymanagement.vocabulary.FormatTypes;

@Component(AppConfigConstants.BEAN_EM_JSONLD_SERIALIZER)
public class JsonLdSerializer {

    private final ObjectMapper mapper;
    
	public static final String DATE_FORMAT  = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    
    public JsonLdSerializer(@Qualifier(BEAN_JSON_MAPPER) ObjectMapper objectMapper) {
		mapper = objectMapper.copy();
		SimpleModule module = new SimpleModule();
		/*
		 * TODO: change the TextSerializer constructor in the corelib module to be public
		 * and use it here instead of the EMTextSerializer which is a workaround
		 */
		module.addSerializer(Text.class, new EMTextSerializer(Text.class));
		mapper.registerModule(module);
		SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
		mapper.setDateFormat(df);
		
		/*
		 * necessary because of the @JsonFilter annotations if we want to include all fields,
		 * otherwise the fields we want to exclude can be specified in the given FilerProvider
		 */
		
		SimpleFilterProvider dummy = new SimpleFilterProvider();
	    dummy.setFailOnUnknownId(false);
	    mapper.setFilterProvider(dummy);
    }
    
    
    public String serialize(EntityRecord record, FormatTypes format, String profile) throws EntityManagementRuntimeException {
	return serialize(record, format, EntityProfile.valueOf(profile));
    }

    public String serialize(EntityRecord record, FormatTypes format, EntityProfile profile) throws EntityManagementRuntimeException {
	String res = null;
	switch (profile) {
	case internal:
	    res = serializeInternal(record, format);
	    break;
	case external:
	    res = serializeExternal(record, format);
	    break;
	default:
	    throw new EntityManagementRuntimeException("Serialization not supported for profile:" + profile);
	}
	return res;
    }


	private String serializeExternal(EntityRecord record, FormatTypes format) throws EntityManagementRuntimeException {
		final StringWriter buffer = new StringWriter();
		try {
			SerializationUtils.serializeExternalJson(buffer, mapper, record, format);
		} catch (IOException e) {
			throw new EntityManagementRuntimeException("Unexpected exception occurred when serializing entity to the external format",e);
		}
		return buffer.toString();
	}

    private String serializeInternal(EntityRecord record, FormatTypes format) throws EntityManagementRuntimeException {
			final StringWriter buffer = new StringWriter();
			try {
				SerializationUtils.serializeInternalJson(buffer, mapper, record, format);
			} catch (IOException e) {
				throw new EntityManagementRuntimeException("Unexpected exception occurred when serializing entity to the internal format!",e);

			}
			return buffer.toString();
    }
}