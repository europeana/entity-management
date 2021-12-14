package eu.europeana.entitymanagement.testutils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.apache.commons.io.IOUtils;

public class UnitTestUtils {

  public static final String ORGANIZATION_VALIDATE_FIELDS_JSON =
      "/validation/organization-validation.json";
  public static final String AGENT_VALIDATE_FIELDS_JSON = "/validation/agent-validation.json";
  public static final String AGENT_VALIDATE_FIELDS_EMPTY_PREFLABEL_JSON =
      "/validation/agent-validation_empty_preflabel.json";

  public static String loadFile(String resourcePath) throws IOException {
    return IOUtils.toString(
            Objects.requireNonNull(UnitTestUtils.class.getResourceAsStream(resourcePath)),
            StandardCharsets.UTF_8)
        .replace("\n", "");
  }
}
