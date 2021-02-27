package eu.europeana.entitymanagement.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.text.SimpleDateFormat;

/**
 * Configure Jackson serialization output.
 */
@Configuration
public class SerializationConfig {

    @Bean
    public ObjectMapper mapper() {
        return new Jackson2ObjectMapperBuilder()
                .defaultUseWrapper(false)
                //TODO: confirm date format with PO
                .dateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX"))
                .createXmlMapper(true)
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .build();
    }

    @Bean
    public com.fasterxml.jackson.databind.Module jaxbAnnotationModule() {
        return new JaxbAnnotationModule();
    }
}