package eu.europeana.entitymanagement.mongo.utils;

import dev.morphia.DeleteOptions;
import dev.morphia.mapping.DiscriminatorFunction;
import dev.morphia.mapping.MapperOptions;
import dev.morphia.mapping.NamingStrategy;

public class MorphiaUtils {

    private MorphiaUtils() {
        // private constructor to prevent instantiation
    }

    public static final MapperOptions MAPPER_OPTIONS = MapperOptions
            .builder()
            // use legacy settings for backwards-compatibility
            .discriminatorKey(Fields.ENTITY_ID)
            .discriminator(DiscriminatorFunction.className())
            .fieldNaming(NamingStrategy.identity())
            .build();

    // Morphia deletes the first matching document by default. This is required for deleting all matches.
    public static final DeleteOptions MULTI_DELETE_OPTS = new DeleteOptions().multi(true);


    // Collection field names
    public static class Fields {

        private Fields() {
            // private constructor to prevent instantiation
        }

        public static final String ENTITY_ID = "entityId";    }

}
