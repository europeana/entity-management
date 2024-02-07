package eu.europeana.entitymanagement;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Basic test for loading context */
@SpringBootTest
@Disabled("Excluded from automated runs")
@ActiveProfiles("test")//enable application-test.yml
class EntityManagementAppTest {

  @SuppressWarnings("squid:S2699") // we are aware that this test doesn't have any assertion
  @Test
  void contextLoads() {}
}
