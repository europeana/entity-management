package eu.europeana.entitymanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
public class ApplicationIT extends AbstractIntegrationTest {

    @Test
    public void contextLoads() {
    }
}