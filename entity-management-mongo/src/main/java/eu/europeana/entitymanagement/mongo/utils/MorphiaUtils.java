package eu.europeana.entitymanagement.mongo.utils;

import dev.morphia.DeleteOptions;
import dev.morphia.UpdateOptions;

public class MorphiaUtils {

    // Morphia deletes the first matching document by default. This is required for
    // deleting all matches.
    public static final DeleteOptions MULTI_DELETE_OPTS = new DeleteOptions().multi(true);

    // Indicates that an update query should be executed as an "upsert",
    // ie. creates new records if they do not already exist, or updates them if they do.
    public static final UpdateOptions UPSET_OPTS = new UpdateOptions().upsert(true);

    private MorphiaUtils() {
	// private constructor to prevent instantiation
    }
}
