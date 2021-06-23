package eu.europeana.entitymanagement.config;

import com.mongodb.client.MongoClients;
import eu.europeana.enrichment.service.dao.EnrichmentDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = {"classpath:entitymanagement.properties", "classpath:entitymanagement.user.properties"}, ignoreResourceNotFound = true)
public class EnrichmentConfig {

    private static final Logger LOG = LogManager.getLogger(EnrichmentConfig.class);

    @Value("${mongo.enrichment.connectionUrl}")
    private String enrichmenthostUri;

    @Value("${mongo.enrichment.database}")
    private String enrichmentDatabase;

    public String getEnrichmentDatabase() {
        return this.enrichmentDatabase;
    }

    @Bean
    @Lazy
    EnrichmentDao getEnrichmentDao() {
        LOG.info("Creating EnrichmentDao bean with Mongo client - {}, database - {} ", enrichmenthostUri, enrichmentDatabase);
        return new EnrichmentDao(MongoClients.create(enrichmenthostUri), enrichmentDatabase);
    }
}
