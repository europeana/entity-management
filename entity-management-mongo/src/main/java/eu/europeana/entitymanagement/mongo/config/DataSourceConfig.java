package eu.europeana.entitymanagement.mongo.config;

import com.mongodb.Block;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.ConnectionPoolSettings;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.MapperOptions;
import eu.europeana.batch.entity.JobExecutionEntity;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.batch.codec.ScheduledTaskTypeCodec;
import eu.europeana.entitymanagement.definitions.batch.codec.ScheduledTaskTypeCodecProvider;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(
    value = {"classpath:entitymanagement.properties", "classpath:entitymanagement.user.properties"},
    ignoreResourceNotFound = true)
public class DataSourceConfig {

  private static final Logger logger = LogManager.getLogger(DataSourceConfig.class);
  long DEFAULT_MONGO_MAX_IDLE_TIME_MILLISEC = 60000;

  @Value("${mongo.connectionUrl}")
  private String hostUri;

  @Value("${mongo.max.idle.time.millisec}")
  private long mongoMaxIdleTimeMillisec;

  @Value("${mongo.em.database}")
  private String emDatabase;

  @Value("${mongo.batch.database}")
  private String batchDatabase;

  @Bean
  public MongoClient mongoClient() {
    ConnectionString connectionString = new ConnectionString(hostUri);

    // Configure custom codecs
    CodecProvider pojoCodecProvider =
        PojoCodecProvider.builder().register(new ScheduledTaskTypeCodecProvider()).build();

    CodecRegistry codecRegistry =
        CodecRegistries.fromRegistries(
            CodecRegistries.fromCodecs(new ScheduledTaskTypeCodec()),
            CodecRegistries.fromProviders(pojoCodecProvider),
            MongoClientSettings.getDefaultCodecRegistry());

    if (mongoMaxIdleTimeMillisec <= 0) {
      mongoMaxIdleTimeMillisec = DEFAULT_MONGO_MAX_IDLE_TIME_MILLISEC;
    }

    Block<ConnectionPoolSettings.Builder> connectionPoolSettingsBlockBuilder =
        new Block<ConnectionPoolSettings.Builder>() {
          @Override
          public void apply(final ConnectionPoolSettings.Builder builder) {
            builder.maxConnectionIdleTime(mongoMaxIdleTimeMillisec, TimeUnit.MILLISECONDS);
          }
        };

    return MongoClients.create(
        MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .applyToConnectionPoolSettings(connectionPoolSettingsBlockBuilder)
            .codecRegistry(codecRegistry)
            .build());
  }

  @Primary
  @Bean(AppConfigConstants.BEAN_EM_DATA_STORE)
  public Datastore emDataStore(MongoClient mongoClient) {
    logger.info("Configuring Entity Management database: {}", emDatabase);
    Datastore datastore =
        Morphia.createDatastore(
            mongoClient, emDatabase, MapperOptions.builder().mapSubPackages(true).build());
    // EA-2520: explicit package mapping required to prevent EntityDecoder error
    datastore.getMapper().mapPackage("eu.europeana.entitymanagement.definitions.model");
    datastore.ensureIndexes();
    return datastore;
  }

  /**
   * Configures Morphia data store for the batch job repository
   *
   * @param mongoClient Mongo connection
   * @return data store for Spring batch JobRepository
   */
  @Bean(name = AppConfigConstants.BEAN_BATCH_DATA_STORE)
  public Datastore batchDataStore(MongoClient mongoClient) {
    logger.info("Configuring Batch database: {}", batchDatabase);
    Datastore datastore = Morphia.createDatastore(mongoClient, batchDatabase);
    // Indexes aren't created unless Entity classes are explicitly mapped. Class arbitrarily picked
    // from package
    datastore.getMapper().mapPackage(JobExecutionEntity.class.getPackageName());
    datastore.ensureIndexes();
    return datastore;
  }
}
