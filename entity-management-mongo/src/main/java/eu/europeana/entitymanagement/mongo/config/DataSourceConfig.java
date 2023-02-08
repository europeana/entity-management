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
import eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.batch.codec.ScheduledTaskTypeCodec;
import eu.europeana.entitymanagement.definitions.batch.codec.ScheduledTaskTypeCodecProvider;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
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

  @Value("${mongo.connectionUrl}")
  private String hostUri;

  @Value("${mongo.max.idle.time.millisec: 10000}")
  private long mongoMaxIdleTimeMillisec;

  @Value("${mongo.em.database}")
  private String emDatabase;

  @Value("${mongo.batch.database}")
  private String batchDatabase;

  @Value("${mongo.truststore:}")
  private String truststorePath;

  @Value("${mongo.truststore.pwd:}")
  private String truststorePwd;

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

    Block<ConnectionPoolSettings.Builder> connectionPoolSettingsBlockBuilder =
        (ConnectionPoolSettings.Builder builder) ->
            builder.maxConnectionIdleTime(mongoMaxIdleTimeMillisec, TimeUnit.MILLISECONDS);

    try {
      // build SSL context that uses separate truststore (needs to be copied and specified via
      // mongo.truststore property)
      if (!truststorePath.isBlank()) {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(truststorePath), truststorePwd.toCharArray());
        LogManager.getLogger(DataSourceConfig.class)
            .info("Read truststore file {}", truststorePath);

        TrustManagerFactory trustFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustFactory.init(ks);

        SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
        sslContext.init(null, trustFactory.getTrustManagers(), null);
        return MongoClients.create(
            MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applyToConnectionPoolSettings(connectionPoolSettingsBlockBuilder)
                .codecRegistry(codecRegistry)
                .applyToSslSettings(builder -> builder.enabled(true).context(sslContext))
                .build());
      }
    } catch (IOException e) {
      throw new RuntimeException("Error reading truststore file", e);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Error loading TLS protocol", e);
    } catch (KeyStoreException e) {
      throw new RuntimeException("Error initializing truststore", e);
    } catch (CertificateException e) {
      throw new RuntimeException("Error loading truststore", e);
    } catch (KeyManagementException e) {
      throw new RuntimeException("Error initializing SSL context", e);
    }

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
