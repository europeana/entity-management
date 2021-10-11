package eu.europeana.entitymanagement.serialization;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.BEAN_JSON_MAPPER;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.exceptions.EntityManagementRuntimeException;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.vocabulary.EntityProfile;
import eu.europeana.entitymanagement.vocabulary.FormatTypes;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component(AppConfigConstants.BEAN_EM_JSONLD_SERIALIZER)
public class JsonLdSerializer {

  private final ObjectMapper mapper;

  public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  public JsonLdSerializer(@Qualifier(BEAN_JSON_MAPPER) ObjectMapper objectMapper) {
    mapper = objectMapper.copy();
    SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
    mapper.setDateFormat(df);
  }

  public String serialize(EntityRecord record, FormatTypes format, String profile)
      throws EntityManagementRuntimeException {
    return serialize(record, format, EntityProfile.valueOf(profile));
  }

  public String serialize(EntityRecord record, FormatTypes format, EntityProfile profile)
      throws EntityManagementRuntimeException {
    String res = null;
    switch (profile) {
      case internal:
        res = serializeInternal(record, format);
        break;
      case external:
        res = serializeExternal(record, format);
        break;
      default:
        throw new EntityManagementRuntimeException(
            "Serialization not supported for profile:" + profile);
    }
    return res;
  }

  private String serializeExternal(EntityRecord record, FormatTypes format)
      throws EntityManagementRuntimeException {
    final StringWriter buffer = new StringWriter();
    try {
      SerializationUtils.serializeExternalJson(buffer, mapper, record, format);
    } catch (IOException e) {
      throw new EntityManagementRuntimeException(
          "Unexpected exception occurred when serializing entity to the external format", e);
    }
    return buffer.toString();
  }

  private String serializeInternal(EntityRecord record, FormatTypes format)
      throws EntityManagementRuntimeException {
    final StringWriter buffer = new StringWriter();
    try {
      SerializationUtils.serializeInternalJson(buffer, mapper, record, format);
    } catch (IOException e) {
      throw new EntityManagementRuntimeException(
          "Unexpected exception occurred when serializing entity to the internal format!", e);
    }
    return buffer.toString();
  }
}
