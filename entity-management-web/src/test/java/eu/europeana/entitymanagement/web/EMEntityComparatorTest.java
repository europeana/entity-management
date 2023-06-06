package eu.europeana.entitymanagement.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.utils.EntityComparator;

@SpringBootTest
@AutoConfigureMockMvc
class EMEntityComparatorTest {

  @Qualifier(AppConfigConstants.BEAN_JSON_MAPPER)
  @Autowired
  private ObjectMapper objectMapper;
  
  private static final String ORGANIZATION_FOR_COMPARISON_1_JSON = "/deserialization/organization_for_comparison_1.json";
  private static final String ORGANIZATION_FOR_COMPARISON_2_JSON = "/deserialization/organization_for_comparison_2.json";

  @Test
  public void compareEntities() throws Exception {
    String org1Json = IOUtils.toString(
        Objects.requireNonNull(EMEntityComparatorTest.class.getResourceAsStream(ORGANIZATION_FOR_COMPARISON_1_JSON)),
        StandardCharsets.UTF_8)
    .replace("\n", "");
    Organization org1 = objectMapper.readValue(org1Json, Organization.class);

    String org2Json = IOUtils.toString(
        Objects.requireNonNull(EMEntityComparatorTest.class.getResourceAsStream(ORGANIZATION_FOR_COMPARISON_2_JSON)),
        StandardCharsets.UTF_8)
    .replace("\n", "");
    Organization org2 = objectMapper.readValue(org2Json, Organization.class);

    EntityComparator entityComparator = new EntityComparator();

    int result = entityComparator.compare(org1, org2);
    
    assertEquals(0, result);
  }

}
