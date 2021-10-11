package eu.europeana.entitymanagement.definitions.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EntityIdResponse {

    private long expected;
    private List<String> successful;
    private List<String> skipped;
    private List<String> failed;

    public EntityIdResponse(long expected, List<String> successful, List<String> failed, List<String> skipped) {
        this.expected = expected;
        this.successful = successful;
        this.skipped = skipped;
        this.failed = failed;
    }

    public long getExpected() {
        return expected;
    }

    public List<String> getSuccessful() {
        return successful;
    }

    public List<String> getFailed() {
        return failed;
    }

    public List<String> getSkipped() {
        return skipped;
    }
}
