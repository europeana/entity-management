package eu.europeana.entitymanagement.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.europeana.entitymanagement.utils.EnrichmentConstants;
import java.text.SimpleDateFormat;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EnrichmentResponse {

    private String success;
    private int status;
    private String timestamp;
    private long entitiesPublished;

    public EnrichmentResponse(Long entitiesPublished, int status) {
        this.success = EnrichmentConstants.STATUS_SUCCESS;
        this.timestamp = new SimpleDateFormat(EnrichmentConstants.DATE_FORMAT).format(new Date());
        this.status = status;
        this.entitiesPublished = entitiesPublished;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getEntitiesPublished() {
        return entitiesPublished;
    }

    public void setEntitiesPublished(long entitiesPublished) {
        this.entitiesPublished = entitiesPublished;
    }
}
