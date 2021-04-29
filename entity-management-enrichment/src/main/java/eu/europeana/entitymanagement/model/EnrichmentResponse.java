package eu.europeana.entitymanagement.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.europeana.entitymanagement.utils.EnrichmentConstants;
import java.text.SimpleDateFormat;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EnrichmentResponse {

    private EnrichmentPublished successful;
    private EnrichmentPublished failed;

    public EnrichmentResponse(EnrichmentPublished successful, EnrichmentPublished failed) {
        this.successful = successful;
        this.failed = failed;
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
}
