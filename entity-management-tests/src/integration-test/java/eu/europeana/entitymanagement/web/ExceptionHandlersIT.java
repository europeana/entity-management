package eu.europeana.entitymanagement.web;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.testutils.IntegrationTestUtils;
import org.springframework.http.*;

/**
 * Spring Boot's error handling is based on the Servlet container error mappings that result in an error 
 * dispatch to an ErrorController. MockMvc however is container-less testing so with no Servlet container.
 * To properly test the exception handlers, the configuration with the RANDOM_PORT web environment and 
 * TestRestTemplate is needed. Please note also the configuration properties in the test resources application.properties:
 * spring.mvc.throw-exception-if-no-handler-found=true
 * spring.resources.add-mappings=false
 * Links: https://stackoverflow.com/questions/61455449/unit-test-mockhttpservletrequest-not-returning-content-type,
 * https://github.com/spring-projects/spring-boot/issues/7321
 * @author StevaneticS
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExceptionHandlersIT extends AbstractIntegrationTest{

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void wrongUrlRequestShouldReturn404WithSuccessFalse() {
      String emptyJson = "{}";
      HttpHeaders headers = new HttpHeaders();
      List<MediaType> mt = new ArrayList<>();
      mt.add(MediaType.APPLICATION_JSON);
      headers.setAccept(mt);
      HttpEntity<String> entity = new HttpEntity<>(emptyJson, headers);
      String url = IntegrationTestUtils.BASE_SERVICE_URL + "000000" + IntegrationTestUtils.BASE_ADMIN_URL + "/enrich";
      
      ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
      JSONObject obj = new JSONObject(response.getBody());
      Assertions.assertEquals(obj.get("success"), false);
      Assertions.assertEquals(obj.get("status"), HttpStatus.NOT_FOUND.value());
  }

}
