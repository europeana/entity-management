package eu.europeana.entitymanagement.web;

import eu.europeana.entitymanagement.batch.HelloJobConfigurer;
import eu.europeana.entitymanagement.batch.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * Temporary controller to test Spring Batch integration.
 * Functionality will be moved to {@link EMController}
 */
@RestController
@RequestMapping(path = "/jobs")
public class JobLauncherController {

    private final HelloJobConfigurer helloJobConfigurer;

    private final BatchConfigurer batchConfigurer;

    @Autowired
    public JobLauncherController(HelloJobConfigurer helloJobConfigurer, BatchConfigurer batchConfigurer) {
        this.helloJobConfigurer = helloJobConfigurer;
        this.batchConfigurer = batchConfigurer;
    }

    /**
     * Temporary endpoint to test Spring Batch integration
     * This triggers a simple job that logs to the console
     */
    @PostMapping("/run")
    public ResponseEntity<String> handle() throws Exception {
        // launcher is async, so this is non-blocking
        JobLauncher jobLauncher = batchConfigurer.getJobLauncher();
        JobParameters jobParameters = new JobParametersBuilder().addDate(JobParameter.RUN_ID.key(), new Date()).toJobParameters();

        jobLauncher.run(helloJobConfigurer.helloWorldJob(), jobParameters);

        return ResponseEntity.ok("Job successfully triggered");
    }
}