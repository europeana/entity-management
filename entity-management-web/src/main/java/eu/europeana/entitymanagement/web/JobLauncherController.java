package eu.europeana.entitymanagement.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.entitymanagement.batch.BatchEntityUpdateConfig;
import eu.europeana.entitymanagement.batch.BatchUtils;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.Date;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.BEAN_JSON_MAPPER;

/**
 * Temporary controller to test Spring Batch integration.
 */
@RestController
@RequestMapping(path = "/jobs")
public class JobLauncherController {

    private final BatchEntityUpdateConfig batchEntityUpdateConfig;
    private final BatchConfigurer batchConfigurer;
    private final ObjectMapper mapper;
    private JobLauncher jobLauncher;


    @Autowired
    public JobLauncherController(BatchConfigurer batchConfigurer, BatchEntityUpdateConfig batchEntityUpdateConfig, @Qualifier(BEAN_JSON_MAPPER) ObjectMapper mapper) {
        this.batchEntityUpdateConfig = batchEntityUpdateConfig;
        this.batchConfigurer = batchConfigurer;
        this.mapper = mapper;
    }

    @PostConstruct
    void setup() throws Exception {
        // launcher is async, so this is non-blocking
        jobLauncher = batchConfigurer.getJobLauncher();
    }

    /**
     * Temporary endpoint to test Spring Batch integration
     * This triggers a simple job that logs to the console
     */
    @PostMapping("/run")
    public ResponseEntity<String> handle(@RequestBody(required = false) String entityId) throws Exception {
        if (StringUtils.hasLength(entityId)) {
            launchSingleEntityUpdate(entityId);
        } else {
            launchMultiEntityUpdate();
        }

        return ResponseEntity.ok("Job successfully triggered");
    }

    private void launchSingleEntityUpdate(String entityId) throws Exception {
        JobParameters jobParameters = BatchUtils.createJobParameters(new String[]{entityId}, new Date(), mapper);
        jobLauncher.run(batchEntityUpdateConfig.updateSpecificEntities(), jobParameters);
    }

    private void launchMultiEntityUpdate() throws Exception {
        JobParameters jobParameters = BatchUtils.createJobParameters(null, new Date(), mapper);
        jobLauncher.run(batchEntityUpdateConfig.updateAllEntities(), jobParameters);
    }
}