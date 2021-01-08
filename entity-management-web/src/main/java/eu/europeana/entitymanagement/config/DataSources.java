package eu.europeana.entitymanagement.config;

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


    public DataSources() {
        // explicit default constructor
    }

    public DataSources(List<DataSource> datasources) {
        this.datasources = datasources;
    }



    public List<DataSource> getDatasources() {
        return datasources;
    }

}
