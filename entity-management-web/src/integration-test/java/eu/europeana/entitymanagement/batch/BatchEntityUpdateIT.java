package eu.europeana.entitymanagement.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.batch.config.MongoBatchConfigurer;
import eu.europeana.entitymanagement.web.service.impl.EntityRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.BEAN_JSON_MAPPER;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class BatchEntityUpdateIT extends AbstractIntegrationTest implements InitializingBean {

    @Autowired
    private BatchEntityUpdateConfig batchEntityUpdateConfig;

    @Autowired
    private MongoBatchConfigurer batchConfigurer;

    @Qualifier(BEAN_JSON_MAPPER)
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private EntityRecordService entityRecordService;


    private JobLauncher jobLauncher;

    @BeforeEach
    void setUp() {
        //ensure a clean db between test runs
        this.entityRecordService.dropRepository();
        batchConfigurer.clearRepository();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // use a blocking launcher to we can assert exit status in test
        this.jobLauncher = getSynchronousLauncher();
    }

    @Test
    void updateSingleEntityJobShouldRun() throws Exception {
        JobExecution jobExecution = jobLauncher.run(batchEntityUpdateConfig.updateSpecificEntities(), BatchUtils
            .createJobParameters(new String[]{"http://data.europeana.eu/agent/1"}, new Date(), mapper));
        // then
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());

        //TODO: write assertions for EntityRecord
    }

    @Test
    void updateAllEntitiesShouldRun() throws Exception {
        JobExecution jobExecution = jobLauncher.run(batchEntityUpdateConfig.updateAllEntities(), BatchUtils
            .createJobParameters(null, new Date(), mapper));
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
        //TODO: write assertions for EntityRecords
    }


    /**
     * Create a blocking Job launcher for testing, as {@link BatchConfigurer#getJobLauncher()} returns
     * an async one.
     */
    private JobLauncher getSynchronousLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(batchConfigurer.getJobRepository());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

}