package eu.europeana.entitymanagement.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Configure Jackson serialization output.
 */
@Configuration
public class SerializationConfig {

    //TODO: confirm date format with PO
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX");

    @Primary
    @Bean(AppConfigConstants.BEAN_JSON_MAPPER)
    public ObjectMapper mapper() {
        return new Jackson2ObjectMapperBuilder()
                .defaultUseWrapper(false)
                .dateFormat(dateFormat)
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .build();
    }

    @Bean(AppConfigConstants.BEAN_XML_MAPPER)
    public XmlMapper xmlMapper() {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.setDateFormat(dateFormat);
        return xmlMapper;
    }

    @Bean
    public com.fasterxml.jackson.databind.Module jaxbAnnotationModule() {
        return new JaxbAnnotationModule();
    }
}