package eu.europeana.entitymanagement.job;

import eu.europeana.entitymanagement.EntityManagementApp;
import eu.europeana.entitymanagement.batch.config.MongoBatchDatasourceConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class JobConfigurer {

    private static final Logger logger = LogManager.getLogger(JobConfigurer.class);

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Step helloWorldStep() {
        return this.stepBuilderFactory.get("step1")
                .tasklet((contribution, chunkContext) -> {
                    logger.info("Hello, World!");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("job")
                .start(helloWorldStep())
                .build();
    }
}
