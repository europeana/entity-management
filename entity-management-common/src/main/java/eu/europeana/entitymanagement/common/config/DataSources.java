package eu.europeana.entitymanagement.common.config;

import java.util.List;
import java.util.Optional;

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
	public boolean hasDataSource(String id) {
		return datasources.stream().anyMatch(s -> id.contains(s.getUrl()));
	}

	/**
	 * Gets the configured data source for the given entity ID
	 *
	 * @param id entity ID
	 * @return Matching data source, or empty Optional if none found
	 */
	public Optional<DataSource> getDatasource(String id) {
		return datasources.stream().filter(s -> id.contains(s.getUrl())).findFirst();
	}

}
