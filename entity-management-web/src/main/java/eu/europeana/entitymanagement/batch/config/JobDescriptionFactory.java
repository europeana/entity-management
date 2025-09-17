package eu.europeana.entitymanagement.batch.config;

import eu.europeana.entitymanagement.batch.model.JobDescription;
import eu.europeana.entitymanagement.definitions.batch.model.TaskType;

import java.util.HashMap;

/**
 * Job description factory class
 * @author srishti singh
 * @since 29 July 2025
 */
public class JobDescriptionFactory extends HashMap<TaskType, JobDescription> {

    public void register(JobDescription jobDescription) {
        this.put(jobDescription.getTaskType(), jobDescription);
    }
}
