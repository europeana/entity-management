package eu.europeana.entitymanagement;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

/** Basic test for loading context */
@SpringBootTest
@Disabled("Excluded from automated runs")
@ActiveProfiles("test")//enable application-test.yml
class EntityManagementAppTest {

  @Autowired
  private ApplicationContext applicationContext;
  
  @Test
  void contextLoads() {
    assertNotNull(applicationContext.getApplicationName());
  }
}
