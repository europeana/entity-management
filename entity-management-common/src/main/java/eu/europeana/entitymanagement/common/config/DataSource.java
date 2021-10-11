package eu.europeana.entitymanagement.common.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "source")
public class DataSource {

	@JacksonXmlProperty(isAttribute = true)
	private String url;
	
	public String getUrl() {
		return url;
	}

	@JacksonXmlProperty(isAttribute = true)
	private String rights;

	public String getRights() {
		return rights;
	}

	@JacksonXmlProperty(isAttribute = true)
	private String id;

	public String getId() {
		return id;
	}
}
