package eu.europeana.entitymanagement.config;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.common.config.DataSources;

/**
 * JUnit test to check if DataSources are properly deserialized from XML
 */
public class DataSourcesDeserializationTest {

	@Test
	public void whenJavaGotFromXmlFile_thenCorrect() throws IOException {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.scan("eu.europeana.entitymanagement.config");
		context.refresh();
		System.out.println("Refreshing the spring context");
		DataSources ds = context.getBean(DataSources.class);
		List<DataSource> dsList = ds.getDatasources();
		for (int i=0; i<dsList.size(); i++)
		{
			System.out.println("DataSource " + i + " url: " + dsList.get(i).getUrl());
			System.out.println("DataSource " + i + " rights: " + dsList.get(i).getRights());
		}
		
		// close the spring context
		context.close();
	}

}
