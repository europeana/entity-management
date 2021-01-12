package eu.europeana.entitymanagement.mongo.service;

import static eu.europeana.entitymanagement.mongo.utils.MorphiaUtils.MAPPER_OPTIONS;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoClient;

import dev.morphia.Datastore;
import dev.morphia.Morphia;

@Configuration
@PropertySource("classpath:entitymanagement.properties")
//@PropertySource(value = "classpath:entitymanagement.user.properties", ignoreResourceNotFound = true)
public class DataSourceConfig {

    @Bean
    public Datastore datastore(MongoClient mongoClient, MongoProperties mongoProperties) {
        // There can be an alternative database defined via spring.data.mongodb.database, so check if that's the case
        String database = mongoProperties.getDatabase();
        MongoClientURI uri = new MongoClientURI(mongoProperties.getUri());
                
        if (StringUtils.isEmpty(database)) {
            database = uri.getDatabase();
        }
        LogManager.getLogger(DataSourceConfig.class).
                info("Connecting to {} Mongo database on hosts {}...", database, uri.getHosts());

        return Morphia.createDatastore(mongoClient, database, MAPPER_OPTIONS);
    }
}