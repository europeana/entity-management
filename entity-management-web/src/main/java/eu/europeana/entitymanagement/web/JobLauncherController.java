package eu.europeana.entitymanagement.web;

import eu.europeana.entitymanagement.batch.BatchEntityUpdateConfig;
import eu.europeana.entitymanagement.batch.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * Temporary controller to test Spring Batch integration.
 */
@RestController
@RequestMapping(path = "/jobs")
public class JobLauncherController {

    private final BatchEntityUpdateConfig batchEntityUpdateConfig;
    private final BatchConfigurer batchConfigurer;

    @Autowired
    public JobLauncherController(BatchConfigurer batchConfigurer, BatchEntityUpdateConfig batchEntityUpdateConfig) {
        this.batchEntityUpdateConfig = batchEntityUpdateConfig;
        this.batchConfigurer = batchConfigurer;
    }

    /**
     * Temporary endpoint to test Spring Batch integration
     * This triggers a simple job that logs to the console
     */
    @PostMapping("/run")
    public ResponseEntity<String> handle(@RequestBody String entityId) throws Exception {
        // launcher is async, so this is non-blocking
        JobLauncher jobLauncher = batchConfigurer.getJobLauncher();
        JobParameters jobParameters = new JobParametersBuilder()
                .addDate(JobParameter.RUN_TIME.key(), new Date())
                .addString(JobParameter.ENTITY_ID.key(), entityId)
                .toJobParameters();

        jobLauncher.run(batchEntityUpdateConfig.updateSingleEntity(), jobParameters);
        return ResponseEntity.ok("Job successfully triggered");
    }
}