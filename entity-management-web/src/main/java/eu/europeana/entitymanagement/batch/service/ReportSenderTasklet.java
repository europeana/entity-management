package eu.europeana.entitymanagement.batch.service;

import eu.europeana.entitymanagement.batch.model.EntityUpdateStats;
import eu.europeana.entitymanagement.definitions.batch.model.TaskType;
import eu.europeana.entitymanagement.web.service.SlackConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * Class to send update reports for the Batch processing
 *
 * @author srishti singh
 * @since 15 September 2025
 */
public class ReportSenderTasklet implements Tasklet {

    private static final Logger logger = LogManager.getLogger(ReportSenderTasklet.class);

    public static final String ASYNC_STATUS_REPORT = """
{"text" : " %s entites were scheduled for %s from external source with the following results: agents: %s, concepts: %s, places: %s, timespans: %s, failed: %s.
See <%s|here> which entities have failed update. "}
                 """;

    private final EntityUpdateStats stats;
    private final String entityMnagmntUrl;
    private final SlackConnection slackConnection;

    public ReportSenderTasklet(EntityUpdateStats stats, String entityMnagmntUrl, SlackConnection slackConnection) {
        this.stats = stats;
        this.entityMnagmntUrl = entityMnagmntUrl;
        this.slackConnection = slackConnection;
    }

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext)
            throws Exception {
        if (stats.getTotalEntitiesForUpdate() > 0) {
            String entityMangmtFailedUrl = entityMnagmntUrl + "/entity/management/failed?pageSize=60";
            slackConnection.publishStatusReport(String.format(ASYNC_STATUS_REPORT,
                    stats.getTotalEntitiesForUpdate(),
                    stats.getTaskType(),
                    stats.getAgents(),
                    stats.getConcepts(),
                    stats.getPlaces(),
                    stats.getTimespans(),
                    stats.getFailed(),
                    entityMangmtFailedUrl));
        } else {
            logger.debug("Status report not sent !! As there are no entities for update type {}", stats.getTaskType());
        }
        return RepeatStatus.FINISHED;
    }
}
