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
import eu.europeana.entitymanagement.definitions.model.impl.BaseAggregation;
import eu.europeana.entitymanagement.definitions.model.impl.BaseEntityProxy;
import eu.europeana.entitymanagement.definitions.model.impl.BaseTimespan;
import eu.europeana.entitymanagement.definitions.model.mongo.impl.EntityRecordImpl;
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;

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
    	
		BaseTimespan entity = new BaseTimespan();
    	entity.setEntityId("http://data.europeana.eu/timespan/base/1");
    	entity.setInternalType("Timespan");
    	Map<String, String> prefLabelTest = new HashMap<String, String>();
    	/*
    	 * putting the "." in the name of the field like prefLabelTest.put("perfLabel.pl", "I wiek") causes problems during saving to the mongodb
    	 */
    	prefLabelTest.put("perfLabel_pl", "I wiek");
    	prefLabelTest.put("perfLabel_da", "1. århundrede");	
    	entity.setPrefLabelStringMap(prefLabelTest);
    	
    	Aggregation aggregation = new BaseAggregation();
    	aggregation.setCreated(new Date());
    	aggregation.setRecordCount(1);
    	List<String> aggregartes = new ArrayList<>();
    	aggregartes.add("http://data.europeana.eu/timespan/base/1#aggr_europeana");
    	aggregation.setAggregates(aggregartes);
    	
    	List<EntityProxy> proxies = new ArrayList<>();
    	BaseEntityProxy proxy = new BaseEntityProxy();
    	proxy.setEntity(entity);
    	Aggregation aggregation2 = new BaseAggregation();
    	aggregation2.setCreated(new Date());
    	aggregation2.setRecordCount(1);
    	List<String> aggregartes2 = new ArrayList<>();
    	aggregartes2.add("http://data.europeana.eu/proxy/base/1#aggr_europeana");
    	aggregation2.setAggregates(aggregartes2);
    	proxy.setProxyIn(aggregation2);
    	proxies.add(proxy);
	    	
    	EntityRecordImpl entityRecordImpl = new EntityRecordImpl();
    	entityRecordImpl.setEntity(entity);
    	entityRecordImpl.setEntityId(entity.getEntityId());
    	entityRecordImpl.setIsAggregatedBy(aggregation);
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
