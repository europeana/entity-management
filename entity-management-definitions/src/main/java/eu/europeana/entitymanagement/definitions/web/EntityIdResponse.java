package eu.europeana.entitymanagement.definitions.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EntityIdResponse {

    private long expected;
    private List<String> successful;
    private List<String> failed;

    public EntityIdResponse(long expected, List<String> successful, List<String> failed) {
        this.expected = expected;
        this.successful = successful;
        this.failed = failed;
    }

    public long getExpected() {
        return expected;
    }

    public void setExpected(long expected) {
        this.expected = expected;
    }

    public List<String> getSuccessful() {
        return successful;
    }

    public void setSuccessful(List<String> successful) {
        this.successful = successful;
    }

    public List<String> getFailed() {
        return failed;
    }

    public void setFailed(List<String> failed) {
        this.failed = failed;
    }
}
