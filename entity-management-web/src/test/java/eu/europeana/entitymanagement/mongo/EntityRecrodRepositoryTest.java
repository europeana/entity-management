package eu.europeana.entitymanagement.mongo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.WebResource;
import eu.europeana.entitymanagement.definitions.model.impl.AggregationImpl;
import eu.europeana.entitymanagement.definitions.model.impl.EntityProxyImpl;
import eu.europeana.entitymanagement.definitions.model.impl.EntityRecordImpl;
import eu.europeana.entitymanagement.definitions.model.impl.TimespanImpl;
import eu.europeana.entitymanagement.definitions.model.impl.WebResourceImpl;
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;
import eu.europeana.entitymanagement.normalization.ValidEntityFieldsValidator;

/**
 * JUnit tests to insert and retrieve the entities from the MongoDB
 */
public class EntityRecrodRepositoryTest {

	@Test
	public void insertEntityToMongoDB() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.scan("eu.europeana.entitymanagement");
		context.refresh();
		System.out.println("Refreshing the spring context");
		EntityRecordRepository entityRecordRepository = context.getBean(EntityRecordRepository.class);
		ValidEntityFieldsValidator entityValidator = context.getBean(ValidEntityFieldsValidator.class);
    	
		TimespanImpl entity = new TimespanImpl();
    	entity.setEntityId("http://data.europeana.eu/timespan/base/3");
    	entity.setInternalType("Timespan");
    	entity.setBeginString("0001-01-01");
    	entity.setEndString("0100-12-31");
    	Map<String, String> prefLabelTest = new HashMap<String, String>();
    	/*
    	 * putting the "." in the name of the field like prefLabelTest.put("perfLabel.pl", "I wiek") causes problems during saving to the mongodb
    	 */
    	//note the not allowed space added in the prefLabel key, for the validation puroposes
    	prefLabelTest.put("perfLabel "+entityValidator.languageSeparator+"pl", "I wiek");
    	prefLabelTest.put("perfLabel"+entityValidator.languageSeparator+"da", "1. århundrede");	
    	entity.setPrefLabelStringMap(prefLabelTest);
    	Map<String,List<String>> altLabel = new HashMap<String, List<String>>();
    	List<String> altLabelValue1 = new ArrayList<String>();
    	altLabelValue1.add("1st century");
    	altLabelValue1.add("1st century AD");
    	//note the not allowed space added in the altLabel key, for the validation puroposes
    	altLabel.put("altLabel "+entityValidator.languageSeparator+"en", altLabelValue1);
    	List<String> altLabelValue2 = new ArrayList<String>();
    	altLabelValue2.add("1. Jahrhundert AD");
    	altLabel.put("altLabel"+entityValidator.languageSeparator+"deu", altLabelValue2);
    	List<String> altLabelValue3 = new ArrayList<String>();
    	altLabelValue3.add("1. vek AD");
    	altLabel.put("altLabel"+entityValidator.languageSeparator+"sr", altLabelValue3);
    	entity.setAltLabel(altLabel);    	
    	
    	WebResource webResource = new WebResourceImpl();
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
    	entity.setIsAggregatedBy(aggregation);
    	
    	List<EntityProxy> proxies = new ArrayList<>();
    	EntityProxy proxy = new EntityProxyImpl();
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
    	entityRecordImpl.setProxies(proxies);
    	
    	entityRecordRepository.save(entityRecordImpl);
    	
    	context.close();
	}
	
	@Test
	public void retrieveEntityFromMongoDB() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.scan("eu.europeana.entitymanagement");
		context.refresh();
		System.out.println("Refreshing the spring context");
		EntityRecordRepository entityRecordRepository = context.getBean(EntityRecordRepository.class);
    	String entityId = "http://data.europeana.eu/timespan/base/1";
		EntityRecord er = entityRecordRepository.findByEntityId(entityId);		
		assertEquals(er.getEntityId(), entityId);
		
    	context.close();
	}

}
