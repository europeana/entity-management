package eu.europeana.entitymanagement.data;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import com.google.gson.Gson;
import eu.europeana.entitymanagement.utils.EntityRecordUtils;

public class FotoConsortiumDataImportTest {

  WebClient emWebClient;
  // test environment
  String emBaseUrl = "https://entity-management-test.eanadev.org";
  // migration environment
//  String emBaseUrl = "https://entity-management-test2.eanadev.org";
  WebClient entityWebClient;
  String entityBaseUrl = "https://entity-api-production.eanadev.org";

  @Test
  public void migrateEntitiesTest() throws IOException, URISyntaxException {

    Map<String, String> entities = getEntityMap();
    for (Map.Entry<String, String> entry : entities.entrySet()) {
      migrateEntity(entry.getKey(), entry.getValue());
    }
  }

  Map<String, String> getEntityMap() throws IOException, URISyntaxException {
    URL fileLocation = getClass().getResource("/fotoconsortium/fotoconsortium_concepts.csv");
    List<String> lines = FileUtils.readLines(new File(fileLocation.toURI()));
    String[] parts = null;
    Map<String, String> entities = new LinkedHashMap<String, String>();
    for (String line : lines) {
      parts = line.split(",");
      entities.put(parts[1], parts[0]);
    }
    return entities;
  }

  private void migrateEntity(String id, String externalId) throws MalformedURLException {
    String identifier = EntityRecordUtils.getIdFromUrl(id);
    String body = "{" + "  \"id\": \"" + externalId + "\"," + "  \"type\":\"Concept\"}";
    // TODO: update token before running the test
//    String token = "<ADMIN TOKEN>";
    String token = "";
    

    String apiResponse;
    apiResponse = getEmWebClient().post()
        .uri(uriBuilder -> uriBuilder.path("/entity/concept/" + identifier + "/management").build())
        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + token).bodyValue(body).retrieve()
        // return 500 for everything else
        .bodyToMono(String.class)
        .onErrorReturn(WebClientException.class, "Failed to migrate entity: " + identifier).block();
    System.out.println(apiResponse);
  }

  @Test
  public void generateEntitiesTest() throws IOException, URISyntaxException {
    Map<String, String> entities = getEntityMap();
    for (Map.Entry<String, String> entry : entities.entrySet()) {
      generateEntityFile(entry.getKey(), entry.getValue());
    }
  }


  private void generateEntityFile(String id, String externalId) throws IOException {
    String identifier = EntityRecordUtils.getIdFromUrl(id);
    // TODO: update before running the script
    String apiKey = "<APIKEY>";
    String apiResponse = getEntityWebClient().get()
        .uri(uriBuilder -> uriBuilder.path("/entity/concept/base/" + identifier)
            .queryParam("wskey", apiKey).build())
        .accept(MediaType.APPLICATION_JSON).retrieve()
        // return 500 for everything else
        .bodyToMono(String.class).onErrorReturn(WebClientException.class,
            "Failed to generate json file for entity: " + identifier)
        .block();
    // remove /base/ from urls
    apiResponse = apiResponse.replaceAll("\\/base\\/", "\\/");

    GsonJsonParser parser = new GsonJsonParser();
    Map<String, Object> entity = parser.parseMap(apiResponse);
    entity.remove("id");
    entity.put("id", externalId);
    @SuppressWarnings("unchecked")
    List<String> exactMatch = (List<String>) entity.get("exactMatch");
    if (exactMatch.contains(externalId)) {
      exactMatch.remove(externalId);
      if (exactMatch.isEmpty()) {
        entity.remove("exactMatch");
      }
    }
    Gson gson = new Gson();
    String serializedEntity = gson.toJson(entity);
    File outFile = new File("/tmp", "" + identifier + ".json");
    FileUtils.write(outFile, serializedEntity, StandardCharsets.UTF_8);
    System.out.println("serialized entity to file:" + outFile.getAbsolutePath());
  }

  @Test
  public void updateEntitiesTest() throws IOException, URISyntaxException {
    Map<String, String> entities = getEntityMap();
    for (Map.Entry<String, String> entry : entities.entrySet()) {
      updateEntity(entry.getKey(), entry.getValue());
    }
  }


  private void updateEntity(String id, String externalId) throws IOException, URISyntaxException {
    String identifier = EntityRecordUtils.getIdFromUrl(id);

    String body = getEntityMetadata(id);

    // TODO: update the ticket wit hthe correct value when running the script
    String token = "";

    String apiResponse = getEmWebClient().put()
        .uri(uriBuilder -> uriBuilder.path("/entity/concept/" + identifier).build())
        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + token).bodyValue(body).retrieve()
        // return 500 for everything else
        .bodyToMono(String.class)
        .onErrorReturn(WebClientException.class, "Failed to update entity: " + identifier).block();
    System.out.println(apiResponse);
  }

  private String getEntityMetadata(String id) throws IOException, URISyntaxException {
    String identifier = EntityRecordUtils.getIdFromUrl(id);
    URL fileLocation = getClass().getResource("/fotoconsortium/" + identifier + ".json");
    return FileUtils.readFileToString(new File(fileLocation.toURI()), StandardCharsets.UTF_8);
  }

  private WebClient configureWebClient(String baseUrl) throws MalformedURLException {
    WebClient.Builder webClientBuilder = WebClient.builder();
    webClientBuilder.baseUrl(baseUrl);
    return webClientBuilder.build();
  }

  public WebClient getEmWebClient() throws MalformedURLException {
    if (emWebClient == null) {
      emWebClient = configureWebClient(emBaseUrl);
    }
    return emWebClient;
  }

  public WebClient getEntityWebClient() throws MalformedURLException {
    if (entityWebClient == null) {
      entityWebClient = configureWebClient(entityBaseUrl);
    }
    return entityWebClient;
  }
  
//  @Test
  public void generateMigratedEntyUrls() throws IOException, URISyntaxException {
    Map<String, String> entities = getEntityMap();
    //TODO: update the value before running the script
    String apiKey = "<APIKEY>";
    for (Map.Entry<String, String> entry : entities.entrySet()) {
      String identifier = EntityRecordUtils.getIdFromUrl(entry.getKey());
      System.out.println(emBaseUrl + "/entity/concept/" + identifier + ".jsonld?profile=internal,debug&wskey="+apiKey );
    } 
  }
}
