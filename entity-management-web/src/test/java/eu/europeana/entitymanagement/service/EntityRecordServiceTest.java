package eu.europeana.entitymanagement.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import eu.europeana.entitymanagement.definitions.model.impl.AgentImpl;
import eu.europeana.entitymanagement.definitions.model.impl.TimespanImpl;
import eu.europeana.entitymanagement.definitions.model.impl.WebResourceImpl;
import eu.europeana.entitymanagement.web.service.impl.EntityRecordService;
import eu.europeana.entitymanagement.web.service.impl.MetisDereferenceService;

/**
 * JUnit tests to merge the data from a different entity (a so called entity proxy) into the given entity
 */
public class EntityRecordServiceTest {

	private static final Logger logger = LogManager.getLogger(MetisDereferenceService.class);
	
	@Test void mergeEntitiesFromXml() {
		XmlMapper xmlMapper = new XmlMapper();
    	try (InputStream inputStream = getClass().getResourceAsStream("/entityAPI-output-xmls/agents.xml");
		    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {    		    
		    String contents = reader.lines()
		      .collect(Collectors.joining(System.lineSeparator()));
		    AgentImpl agent = xmlMapper.readValue(contents, AgentImpl.class);
		    assertNotNull(agent);
		} catch (JsonMappingException e) {
			logger.error("Exeption occured: ",e);
		} catch (JsonProcessingException e) {
			logger.error("Exeption occured: ",e);
		} catch (IOException e) {
			logger.error("Exeption occured: ",e);
		}
	}
	
	@Test
	public void mergeEntities() {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
		    context.scan("eu.europeana.entitymanagement");
		    context.refresh();
		    System.out.println("Refreshing the spring context");
		    EntityRecordService entityRecordService = context.getBean(EntityRecordService.class);

		    //creating the first entity
		    TimespanImpl entity = new TimespanImpl();
  	entity.setEntityId("http://data.europeana.eu/timespan/base/1");
  	entity.setInternalType("Timespan");
  	entity.setBeginString("0001-01-01");
  	entity.setEndString("0100-12-31");
		    entity.setIsNextInSequence(new String[]{"http://data.europeana.eu/timespan/3"});

  	Map<String, String> prefLabelTest = new HashMap<String, String>();
  	/*
  	 * putting the "." in the name of the field like prefLabelTest.put("perfLabel.pl", "I wiek") causes problems during saving to the mongodb
  	 */
  	prefLabelTest.put("perfLabel_pl", "I wiek");
  	prefLabelTest.put("perfLabel_da", "1. århundrede");	
  	entity.setPrefLabelStringMap(prefLabelTest);
      
  	Map<String, List<String>> altLabelTest = new HashMap<>();
  	String[] altLabelTestRu = {"I век"};
  	String[] altLabelTestLt = {"I amžius"};
  	//String[] altLabelTestPl = {"I wiek"};
  	altLabelTest.put("altLabel_ru", Arrays.asList(altLabelTestRu));
  	altLabelTest.put("altLabel_lt", Arrays.asList(altLabelTestLt));
  	entity.setAltLabel(altLabelTest);
  	
  	WebResourceImpl webResource = new WebResourceImpl();
  	webResource.setId("http://www.sbc.org.pl/Timespan/16573/doc.pdf");
  	webResource.setSource("http://data.europeana.eu/item/7284673/_nnd7fT5");
  	webResource.setThumbnail("https://api.europeana.eu/api/v2/thumbnail-by-url.json?uri=http%3A%2F%2Fwww.sbc.org.pl%2FTimespan%2F79368%2Fdoc.pdf&type=TEXT");
  	entity.setIsShownBy(webResource);
  	
  	
  	//creating the second entity to merge into the first one
		    TimespanImpl entityProxy = new TimespanImpl();
		    entityProxy.setEntityId("http://data.europeana.eu/timespan/base/1-1");
		    entityProxy.setInternalType("Timespan");
		    entityProxy.setBeginString("0001-01-01");
		    entityProxy.setEndString("0100-12-31");
		    entityProxy.setIsNextInSequence(new String[]{"http://data.europeana.eu/timespan/2"});
  	Map<String, String> prefLabelTestProxy = new HashMap<String, String>();
  	/*
  	 * putting the "." in the name of the field like prefLabelTest.put("perfLabel.pl", "I wiek") causes problems during saving to the mongodb
  	 */
  	prefLabelTestProxy.put("perfLabel_pl", "1. wiek");//note that this field is different from the first entity and according to the mapping rules should be moved to the alternative label
  	prefLabelTestProxy.put("perfLabel_da", "1. århundrede");
  	prefLabelTestProxy.put("perfLabel_hr", "1. stoljeće");//note that this is not present in the first entity and according to the mapping rules should be added to it during the merge
  	entityProxy.setPrefLabelStringMap(prefLabelTestProxy);
  	
  	Map<String, List<String>> altLabelTestProxy = new HashMap<>();
  	String[] altLabelTestRuProxy = {"1. век"};
  	String[] altLabelTestPlProxy = {"I wiek"};
  	//String[] altLabelTestPl = {"I wiek"};
  	altLabelTestProxy.put("altLabel_ru", Arrays.asList(altLabelTestRuProxy));
  	altLabelTestProxy.put("altLabel_pl", Arrays.asList(altLabelTestPlProxy));
  	entityProxy.setAltLabel(altLabelTestProxy);

  	WebResourceImpl webResourceProxy = new WebResourceImpl();
  	webResourceProxy.setId("http://www.sbc.org.pl/Timespan/20394/doc.pdf");
  	webResourceProxy.setSource("http://data.europeana.eu/item/4738926/_frg5gT8");
  	webResourceProxy.setThumbnail("https://api.europeana.eu/api/v2/thumbnail-by-url.json?uri=http%3A%2F%2Fwww.sbc.org.pl%2FTimespan%2F79368%2Fdoc.pdf&type=TEXT");
  	entityProxy.setIsShownBy(webResourceProxy);
  	
  	entityRecordService.mergeEntity(entity,entityProxy);
  	
  	logger.info("Reconceliated entity: {}", entity);
		}
	}
	


}
