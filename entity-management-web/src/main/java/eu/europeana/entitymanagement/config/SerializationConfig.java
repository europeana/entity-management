package eu.europeana.entitymanagement.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.web.xml.model.RdfBaseWrapper;
import eu.europeana.entitymanagement.web.xml.model.metis.EnrichmentResultList;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
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
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX");

    @Primary
    @Bean(AppConfigConstants.BEAN_JSON_MAPPER)
    public ObjectMapper mapper() {
        return new Jackson2ObjectMapperBuilder()
                .defaultUseWrapper(false)
                .dateFormat(dateFormat)
                .featuresToEnable(
                    DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
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

    /**
     * Create a {@link JAXBContext} for use across the application.
     * JAXBContext is thread-safe, however its marshaller and unmarshaller are not, so
     * they need to be properly set up for multithreaded use.
     * @return JAXBContext
     * @throws JAXBException on exception
     */
    @Bean
    public JAXBContext jaxbContext() throws JAXBException {
        // args are wrapper classes for Deserializing Metis Response and Serializing API output
        return JAXBContext.newInstance(EnrichmentResultList.class, RdfBaseWrapper.class);
    }
}