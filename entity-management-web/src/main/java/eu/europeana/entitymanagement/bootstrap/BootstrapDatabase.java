package eu.europeana.entitymanagement.bootstrap;

import eu.europeana.entitymanagement.batch.config.MongoBatchConfigurer;
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * This class bootstraps the databases for the application. It is only triggered when the "dev" profile
 * is active.
 */
@Profile("dev")
@Component
public class BootstrapDatabase implements CommandLineRunner {

    private static final Logger logger = LogManager.getLogger(BootstrapDatabase.class);

    private final MongoBatchConfigurer batchConfigurer;
    private final EntityRecordRepository entityRecordRepository;


    @Value("${mongo.batch.database.reset}")
    private boolean resetBatchDatabase;

    @Value("${mongo.em.database.reset}")
    private boolean resetEntityDatabase;


    @Autowired
    public BootstrapDatabase(MongoBatchConfigurer batchConfigurer, EntityRecordRepository entityRecordRepository) {
        this.batchConfigurer = batchConfigurer;
        this.entityRecordRepository = entityRecordRepository;
    }


    @Override
    public void run(String... args) throws Exception {

        if (resetBatchDatabase) {
            logger.info("Clearing Spring Batch JobRepository");
            batchConfigurer.clearRepository();
        }

        if (resetEntityDatabase) {
            logger.info("Clearing EntityRecord repository");
            entityRecordRepository.dropCollection();
        }

    }
}
