package eu.europeana.entitymanagement.mongo.utils;

import dev.morphia.DeleteOptions;
import dev.morphia.UpdateOptions;

public class MorphiaUtils {

  // Morphia updates / deletes the first matching document by default. This is required for
  // deleting / updating all matches.
  public static final DeleteOptions MULTI_DELETE_OPTS = new DeleteOptions().multi(true);
  public static final UpdateOptions MULTI_UPDATE_OPTS = new UpdateOptions().multi(true);

  // Indicates that an update query should be executed as an "upsert",
  // ie. creates new records if they do not already exist, or updates them if they do.
  public static final UpdateOptions UPSERT_OPTS = new UpdateOptions().upsert(true);

  private MorphiaUtils() {
    // private constructor to prevent instantiation
  }
}
