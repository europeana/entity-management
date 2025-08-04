package eu.europeana.entitymanagement.batch.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Job description factory class
 * @author srishti singh
 * @since 29 July 2025
 */
public class JobDescription {

    public static final List<Task> PROCESSORS_FULL_UPDATE   = Arrays.asList(Task.DEREFERENCE, Task.CONSOLIDATION, Task.METRICS, Task.VALIDATION);
    public static final List<Task> PROCESSORS_META_UPDATE   =  Arrays.asList(Task.CONSOLIDATION, Task.VALIDATION);
    public static final List<Task> PERSISTENCE_ITEM_WRITERS = Arrays.asList(Task.DB_UPDATE, Task.SOLR_INSERTION);

    private JobType jobType;
    private List<Task> processors;
    private List<Task> writers;

    public JobDescription(JobType jobType, List<Task> processors, List<Task> writers) {
        this.jobType = jobType;
        this.processors = processors;
        this.writers = writers;
    }

    public List<Task> getProcessors() {
        if (processors != null && !processors.isEmpty()) {
            return processors;
        }
        return Collections.emptyList();
    }

    public List<Task> getWriters() {
        if (writers != null && !writers.isEmpty()) {
            return writers;
        }
        return Collections.emptyList();
    }

    public JobType getJobType() {
        return jobType;
    }

    public boolean isFullUpdate() {
        return this.getJobType() == JobType.FULL_UPDATE;
    }

}
