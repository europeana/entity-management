package eu.europeana.entitymanagement.common.config;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "config")
public class DataSources {

	public final static String EUROPEANA_ID = "europeana";
	public final static String ZOHO_ID = "crm.zoho.com";

	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "source")
	private List<DataSource> datasources;

	public List<DataSource> getDatasources() {
		return datasources;
	}

	/**
	 * Checks if a datasource is configured for the given entity ID
	 *
	 * @param url url to match
	 * @return true if a Datasource match is configured, false otherwise.
	 */
	public boolean hasDataSource(String url) {
		return datasources.stream().anyMatch(s -> url.contains(s.getUrl()));
	}

	/**
	 * Gets the configured data source for the given entity ID
	 *
	 * @param url entity ID
	 * @return Matching data source, or empty Optional if none found
	 */
	public Optional<DataSource> getDatasource(String url) {
		return datasources.stream().filter(s -> url.contains(s.getUrl())).findFirst();
	}

	public Optional<DataSource> getEuropeanaDatasource(){
		return datasources.stream().filter(s -> EUROPEANA_ID.equals(s.getId())).findFirst();
	}

}
