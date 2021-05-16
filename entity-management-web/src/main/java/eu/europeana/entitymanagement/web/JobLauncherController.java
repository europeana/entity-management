package eu.europeana.entitymanagement.web;

import eu.europeana.entitymanagement.batch.BatchService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Temporary controller to test Spring Batch integration.
 */
@RestController
@RequestMapping(path = "/jobs")
public class JobLauncherController {

    private final BatchService batchService;

    @Autowired
    public JobLauncherController(BatchService batchService) {
        this.batchService = batchService;
    }

    /**
     * Temporary endpoint to test Spring Batch integration
     */
    @PostMapping("/run")
    public ResponseEntity<String> handle(@RequestBody(required = false) List<String> entityIds) throws Exception {
        if (entityIds != null && !entityIds.isEmpty()) {
            batchService.launchSingleEntityUpdate(entityIds, true);
        } else {
            batchService.launchAllEntityUpdate();
        }

        return ResponseEntity.ok("Job successfully triggered");
    }

    /**
     * Temporary endpoint to test Spring Batch integration
     * Retries updates for Failed Tasks
     */
    @PostMapping("/run/failed")
    public ResponseEntity<String> retryFailure() throws Exception {
        batchService.launchEntityFailureRetryJob();
        return ResponseEntity.ok("Job successfully triggered");
    }
}