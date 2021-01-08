package eu.europeana.entitymanagement.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "source")
public class DataSource {

    public DataSource() {
        // explicit default constructor
    }

    public DataSource(String url, String rights) {
        this.url = url;
        this.rights = rights;
    }

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

}
