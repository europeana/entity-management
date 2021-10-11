package eu.europeana.entitymanagement.mongo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.Timespan;
import eu.europeana.entitymanagement.definitions.model.WebResource;
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureMockMvc
class EntityRecordRepositoryIT extends AbstractIntegrationTest {

  @Autowired private EntityRecordRepository entityRecordRepository;

  @BeforeEach
  public void setup() {
    entityRecordRepository.dropCollection();
  }

  @Test
  public void shouldCorrectlyInsertAndRetrieve() {

    String entityId = "http://data.europeana.eu/timespan/base/1";

    Timespan entity = new Timespan();
    entity.setEntityId(entityId);
    //        entity.setType("TimeSpan");
    entity.setBeginString("0001-01-01");
    entity.setEndString("0100-12-31");
    Map<String, String> prefLabelTest = new HashMap<String, String>();
    /*
     * putting the "." in the name of the field like prefLabelTest.put("perfLabel.pl", "I wiek") causes problems during saving to the mongodb
     */
    prefLabelTest.put("perfLabel_pl", "I wiek");
    prefLabelTest.put("perfLabel_da", "1. århundrede");
    entity.setPrefLabel(prefLabelTest);
    WebResource webResource = new WebResource();
    webResource.setId("http://www.sbc.org.pl/Timespan/16573/doc.pdf");
    webResource.setSource("http://data.europeana.eu/item/7284673/_nnd7fT5");
    webResource.setThumbnail(
        "https://api.europeana.eu/api/v2/thumbnail-by-url.json?uri=http%3A%2F%2Fwww.sbc.org.pl%2FTimespan%2F79368%2Fdoc.pdf&type=TEXT");
    entity.setIsShownBy(webResource);
    Aggregation aggregation = new Aggregation();
    aggregation.setCreated(new Date());
    aggregation.setRecordCount(1);
    List<String> aggregartes = new ArrayList<>();
    aggregartes.add("http://data.europeana.eu/timespan/base/1#aggr_europeana");
    aggregation.setAggregates(aggregartes);

    EntityProxy proxy = new EntityProxy();
    proxy.setEntity(entity);
    Aggregation aggregation2 = new Aggregation();
    aggregation2.setCreated(new Date());
    aggregation2.setRecordCount(1);
    List<String> aggregartes2 = new ArrayList<>();
    aggregartes2.add("http://data.europeana.eu/proxy/base/1#aggr_europeana");
    aggregation2.setAggregates(aggregartes2);
    proxy.setProxyIn(aggregation2);

    EntityRecord entityRecordImpl = new EntityRecord();
    entityRecordImpl.setEntity(entity);
    entityRecordImpl.setEntityId(entity.getEntityId());
    entityRecordImpl.getEntity().setIsAggregatedBy(aggregation);
    entityRecordImpl.addProxy(proxy);

    entityRecordRepository.save(entityRecordImpl);

    EntityRecord er = entityRecordRepository.findByEntityId(entityId);
    assertEquals(er.getEntityId(), entityId);
  }
}
