package eu.europeana.entitymanagement.web;

import eu.europeana.entitymanagement.batch.BatchService;
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
     * This triggers a simple job that logs to the console
     */
    @PostMapping("/run")
    public ResponseEntity<String> handle(@RequestBody(required = false) String entityId) throws Exception {
        if (StringUtils.hasLength(entityId)) {
            batchService.launchSingleEntityUpdate(entityId, true);
        } else {
            batchService.launchMultiEntityUpdate();
        }

        return ResponseEntity.ok("Job successfully triggered");
    }




}