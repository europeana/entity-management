package eu.europeana.entitymanagement.definitions.model;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;

/**
 * Mongo generates a hexadecimal value by default for ObjectIds This class (and its corresponding
 * Mongo collection) help to generate auto-incremented numerical values instead.
 */
@Entity(useDiscriminator = false, value = "EntityIdGenerator")
public class EntityIdGenerator {

  // @Indexed annotation added so Morphia can map this collection on startup (if it doesn't
  // already exist)
  @Id @Indexed private String internalType;

  private long value = 1L;

  public EntityIdGenerator() {
    // default constructor
  }

  public EntityIdGenerator(final String internalType) {
    this.internalType = internalType;
  }

  public EntityIdGenerator(final String internalType, final long startValue) {
    this(internalType);
    value = startValue;
  }

  public long getValue() {
    return value;
  }
}
