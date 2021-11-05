package eu.europeana.entitymanagement.serialization;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.BEAN_JSON_MAPPER;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.batch.model.FailedTask;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.vocabulary.EntityProfile;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component(AppConfigConstants.BEAN_EM_JSONLD_SERIALIZER)
public class JsonLdSerializer {

  private final ObjectMapper mapper;

  public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  @Autowired
  public JsonLdSerializer(@Qualifier(BEAN_JSON_MAPPER) ObjectMapper objectMapper) {
    mapper = objectMapper.copy();
    SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
    mapper.setDateFormat(df);
  }

  public String serialize(
      EntityRecord record,
      List<EntityProfile> profiles,
      boolean includeFailure,
      Optional<FailedTask> failure)
      throws IOException {

    String response;
    // profile must contain either internal or external, but not both
    if (profiles.contains(EntityProfile.internal)) {
      response = SerializationUtils.serializeInternalJson(mapper, record, includeFailure, failure);
    } else {
      response = SerializationUtils.serializeExternalJson(mapper, record, includeFailure, failure);
    }
    return response;
  }
}
