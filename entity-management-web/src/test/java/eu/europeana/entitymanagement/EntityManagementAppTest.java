package eu.europeana.entitymanagement;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Basic test for loading context
 */
@SpringBootTest
@Disabled("Excluded from automated runs")
class EntityManagementAppTest {

    @SuppressWarnings("squid:S2699") // we are aware that this test doesn't have any assertion
    @Test
    void contextLoads() {
    }

}
