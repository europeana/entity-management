package eu.europeana.entitymanagement.mongo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.impl.BaseTimespan;
import eu.europeana.entitymanagement.definitions.model.mongo.impl.EntityRecordImpl;
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;

/**
 * JUnit tests to insert and retrieve the entities from the MongoDB
 */
public class InsertAndRetrieveEntityToAndFromMongoDB {

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
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        String dateInString = "2014-10-05T15:23:01Z";
        try {
            Date date = formatter.parse(dateInString.replaceAll("Z$", "+0000"));
            entity.setTimestamp(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }	    	
    	EntityRecordImpl entityRecordImpl = new EntityRecordImpl();
    	entityRecordImpl.setEntity(entity);
    	entityRecordImpl.setEntityId(entity.getEntityId());
    	
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
