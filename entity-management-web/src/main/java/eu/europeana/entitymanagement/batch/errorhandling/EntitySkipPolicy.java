package eu.europeana.entitymanagement.batch.errorhandling;

import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.lang.NonNull;

public class EntitySkipPolicy implements SkipPolicy {

    public boolean shouldSkip(@NonNull final Throwable t, final int skipCount) throws SkipLimitExceededException {
        // do not fail job because of errors, since we can tackle them later
        return true;
    }

}