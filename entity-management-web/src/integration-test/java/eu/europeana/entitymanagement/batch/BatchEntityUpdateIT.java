package eu.europeana.entitymanagement.batch;

import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.batch.config.MongoBatchConfigurer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class BatchEntityUpdateIT extends AbstractIntegrationTest implements InitializingBean {

    @Autowired
    private BatchEntityUpdateConfig batchEntityUpdateConfig;

    @Autowired
    private MongoBatchConfigurer batchConfigurer;

    private JobLauncher jobLauncher;

    @Override
    public void afterPropertiesSet() throws Exception {
        // use a blocking launcher to we can assert exit status in test
        this.jobLauncher = getSynchronousLauncher();
    }


    @AfterEach
    public void cleanUp() {
        batchConfigurer.clearRepository();
    }


    @Test
    void updateSingleEntityJobShouldRun() throws Exception {
        // given
        JobParameters jobParameters = createJobParameters("http://data.europeana.eu/agent/1", new Date());
        // when
        JobExecution jobExecution = jobLauncher.run(batchEntityUpdateConfig.updateSingleEntity(), jobParameters);
        // then
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());

        //TODO: write assertions for EntityRecord
    }


    private JobParameters createJobParameters(String entityId, Date runTime) {
        return new JobParametersBuilder()
                .addString(JobParameter.ENTITY_ID.key(), entityId)
                .addDate(JobParameter.RUN_TIME.key(), runTime)
                .toJobParameters();
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