package eu.europeana.entitymanagement.config;

import java.text.SimpleDateFormat;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

/**
 * Configure Jackson serialization output.
 * @deprecated not sure if the DateFormat is the correct one and if the global configurations are correct for both xml and json serializations
 */
@Configuration
@Deprecated
public class SerializationConfig {

    @Bean
    public ObjectMapper mapper() {
        return new Jackson2ObjectMapperBuilder()
                .defaultUseWrapper(false)
                .dateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX"))
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .build();
    }

    @Bean
    public com.fasterxml.jackson.databind.Module jaxbAnnotationModule() {
        return new JaxbAnnotationModule();
    }
}