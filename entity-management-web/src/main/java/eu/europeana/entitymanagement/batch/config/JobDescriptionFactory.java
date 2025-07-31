package eu.europeana.entitymanagement.batch.config;

import eu.europeana.entitymanagement.batch.model.JobDescription;
import eu.europeana.entitymanagement.batch.model.JobType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.JOB_DESCRIPTION_FACTORY;

/**
 * Job description factory class
 * @author srishti singh
 * @since 29 July 2025
 */
@Configuration
public class JobDescriptionFactory extends HashMap<JobType, JobDescription> {

    public void register(JobDescription jobDescription) {
        this.put(jobDescription.getJobType(), jobDescription);
    }

    @Bean(name = JOB_DESCRIPTION_FACTORY)
    public JobDescriptionFactory jobDescriptionProvider() {
        JobDescriptionFactory factory = new JobDescriptionFactory();
        factory.register(
                new JobDescription(
                        JobType.FULL_UPDATE
                        , JobDescription.PROCESSORS_FULL_UPDATE
                        , JobDescription.PERSISTENCE_ITEM_WRITERS));

        factory.register(
                new JobDescription(
                        JobType.META_UPDATE
                        , JobDescription.PROCESSORS_META_UPDATE
                        , JobDescription.PERSISTENCE_ITEM_WRITERS));
        return factory;
    }
}
