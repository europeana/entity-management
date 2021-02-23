package eu.europeana.entitymanagement.batch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Stub job configurer to test that Spring Batch integration works
 */
@Component
public class DummyJobConfigurer {

    private static final Logger logger = LogManager.getLogger(DummyJobConfigurer.class);

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    @Autowired
    public DummyJobConfigurer(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    public Job helloWorldJob() {
        logger.info("Starting HelloWorld job");
        return this.jobBuilderFactory.get("helloWorldJob")
                .incrementer(parametersIncrementer())
                .start(step1())
                .build();
    }

    private Step step1() {
        return this.stepBuilderFactory.get("step1")
                .tasklet((stepContribution, chunkContext) -> {
                    logger.info("Hello World Step1 successfully ran");
                    return RepeatStatus.FINISHED;
                }).build();
    }


    // TODO: make this a bean
    private JobParametersIncrementer parametersIncrementer() {
        return jobParameters -> {
            if (jobParameters == null || jobParameters.isEmpty()) {
                return new JobParametersBuilder().addDate(JobParameter.RUN_ID.key(), new Date(System.currentTimeMillis())).toJobParameters();
            }
            Date id = jobParameters.getDate(JobParameter.RUN_ID.key(), new Date(System.currentTimeMillis()));
            return new JobParametersBuilder().addDate(JobParameter.RUN_ID.key(), id).toJobParameters();
        };
    }
}
