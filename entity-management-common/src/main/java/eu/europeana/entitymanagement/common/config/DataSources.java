package eu.europeana.entitymanagement.common.config;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "config")
public class DataSources {
	
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "source")
	private List<DataSource> datasources;

	public List<DataSource> getDatasources() {
		return datasources;
	}

	/**
	 * Checks if a datasource is configured for the given entity ID
	 *
	 * @param id id to match
	 * @return true if a Datasource match is configured, false otherwise.
	 */
	public boolean checkSourceExists(String id) {
		return datasources.stream().anyMatch(s -> id.contains(s.getUrl()));
	}

}
