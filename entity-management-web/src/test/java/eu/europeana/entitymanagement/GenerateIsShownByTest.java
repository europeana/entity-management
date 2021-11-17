package eu.europeana.entitymanagement;

import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * This class acts as a convenience script for populating isShownBy data on entities.
 *
 * <p>To use, put correct values in baseUrl and jwtToken, then run the submitIsShownBy() test
 */
public class GenerateIsShownByTest {

  private static final Logger logger = LogManager.getLogger(GenerateIsShownByTest.class);
  private static final String IS_SHOWN_BY_CSV = "src/test/resources/is_shown_by.csv";

  // replace this with url to Entity Management instance
  private final String baseUrl = "http://entity-management-test.eanadev.org/";

  // put token here
  private final String jwtToken = "";

  private final WebClient webClient =
      WebClient.builder()
          .baseUrl(baseUrl)
          .defaultHeaders(header -> header.setBearerAuth(jwtToken))
          .build();

  @Test
  void submitIsShownBy() throws IOException {
    try (Reader inputCsv = new FileReader(IS_SHOWN_BY_CSV, StandardCharsets.UTF_8);
        CSVParser csvParser = new CSVParser(inputCsv, CSVFormat.DEFAULT)) {
      for (CSVRecord record : csvParser) {
        String entityId = record.get(0);
        String isShownById = record.get(1);
        String isShownBySource = record.get(2);
        String isShownByThumbnail = record.get(3);

        String requestPath = EntityRecordUtils.getEntityRequestPath(entityId);

        try {
          webClient
              .put()
              .uri("/entity/" + requestPath)
              .accept(MediaType.APPLICATION_JSON)
              .contentType(MediaType.APPLICATION_JSON)
              .body(
                  BodyInserters.fromValue(
                      createRequestBody(
                          entityId, isShownById, isShownBySource, isShownByThumbnail)))
              .retrieve()
              .toBodilessEntity()
              .block();
        } catch (WebClientResponseException we) {
          logger.error(
              "Error from POST request entityId={}; statusCode={}; response={}",
              entityId,
              we.getRawStatusCode(),
              we.getResponseBodyAsString());
        }
      }
    }
  }

  private String createRequestBody(
      String entityId, String isShownById, String isShownBySource, String isShownByThumbnail) {
    return String.format(
        "{"
            + "\"@context\": \"http://www.europeana.eu/schemas/context/entity.jsonld\", "
            + "\"type\": \"%s\","
            + "\"isShownBy\": {"
            + "\"id\": \"%s\","
            + "\"source\": \"%s\","
            + "\"thumbnail\": \"%s\""
            + "}"
            + "}",
        getEntityType(entityId), isShownById, isShownBySource, isShownByThumbnail);
  }

  private String getEntityType(String entityId) {
    String[] parts = entityId.split("/");
    return StringUtils.capitalize(parts[parts.length - 2]);
  }
}
