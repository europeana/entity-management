package eu.europeana.entitymanagement.mongo;

import eu.europeana.entitymanagement.AbstractContainerTest;
import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.impl.*;
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class EntityRecordRepositoryIT extends AbstractContainerTest {

    @Autowired
    private EntityRecordRepository entityRecordRepository;

    @BeforeEach
    public void setup() {
        entityRecordRepository.dropCollection();
    }

    @Test
    public void shouldCorrectlyInsertAndRetrieve() {

        String entityId = "http://data.europeana.eu/timespan/base/1";

        TimespanImpl entity = new TimespanImpl();
        entity.setEntityId(entityId);
        entity.setType("Timespan");
        entity.setBeginString("0001-01-01");
        entity.setEndString("0100-12-31");
        Map<String, String> prefLabelTest = new HashMap<String, String>();
        /*
         * putting the "." in the name of the field like prefLabelTest.put("perfLabel.pl", "I wiek") causes problems during saving to the mongodb
         */
        prefLabelTest.put("perfLabel_pl", "I wiek");
        prefLabelTest.put("perfLabel_da", "1. århundrede");
        entity.setPrefLabelStringMap(prefLabelTest);
        WebResourceImpl webResource = new WebResourceImpl();
        webResource.setId("http://www.sbc.org.pl/Timespan/16573/doc.pdf");
        webResource.setSource("http://data.europeana.eu/item/7284673/_nnd7fT5");
        webResource.setThumbnail("https://api.europeana.eu/api/v2/thumbnail-by-url.json?uri=http%3A%2F%2Fwww.sbc.org.pl%2FTimespan%2F79368%2Fdoc.pdf&type=TEXT");
        entity.setIsShownBy(webResource);
        Aggregation aggregation = new AggregationImpl();
        aggregation.setCreated(new Date());
        aggregation.setRecordCount(1);
        List<String> aggregartes = new ArrayList<>();
        aggregartes.add("http://data.europeana.eu/timespan/base/1#aggr_europeana");
        aggregation.setAggregates(aggregartes);

        List<EntityProxy> proxies = new ArrayList<>();
        EntityProxyImpl proxy = new EntityProxyImpl();
        proxy.setEntity(entity);
        Aggregation aggregation2 = new AggregationImpl();
        aggregation2.setCreated(new Date());
        aggregation2.setRecordCount(1);
        List<String> aggregartes2 = new ArrayList<>();
        aggregartes2.add("http://data.europeana.eu/proxy/base/1#aggr_europeana");
        aggregation2.setAggregates(aggregartes2);
        proxy.setProxyIn(aggregation2);
        proxies.add(proxy);

        EntityRecord entityRecordImpl = new EntityRecordImpl();
        entityRecordImpl.setEntity(entity);
        entityRecordImpl.setEntityId(entity.getEntityId());
        entityRecordImpl.getEntity().setIsAggregatedBy(aggregation);
        entityRecordImpl.setProxies(proxies);

        entityRecordRepository.save(entityRecordImpl);

        EntityRecord er = entityRecordRepository.findByEntityId(entityId);
        assertEquals(er.getEntityId(), entityId);
    }
}
