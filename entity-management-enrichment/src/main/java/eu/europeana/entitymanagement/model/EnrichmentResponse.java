package eu.europeana.entitymanagement.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.europeana.entitymanagement.utils.EnrichmentConstants;
import java.text.SimpleDateFormat;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EnrichmentResponse {

    private EnrichmentPublished successful;
    private EnrichmentPublished failed;
    private String timestamp;

    public EnrichmentResponse(EnrichmentPublished successful, EnrichmentPublished failed) {
        this.successful = successful;
        this.failed = failed;
        this.timestamp = new SimpleDateFormat(EnrichmentConstants.DATE_FORMAT).format(new Date());
    }

    public EnrichmentPublished getSuccessful() {
        return successful;
    }

    public void setSuccessful(EnrichmentPublished successful) {
        this.successful = successful;
    }

    public EnrichmentPublished getFailed() {
        return failed;
    }

    public void setFailed(EnrichmentPublished failed) {
        this.failed = failed;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
