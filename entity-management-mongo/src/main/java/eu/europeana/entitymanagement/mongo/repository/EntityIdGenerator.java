package eu.europeana.entitymanagement.mongo.repository;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;

/**
 * Mongo generates a hexadecimal value by default for ObjectIds This class (and its corresponding
 * Mongo collection) help to generate auto-incremented numerical values instead.
 */
@Entity(useDiscriminator = false, value = "EntityIdGenerator")
public class EntityIdGenerator {

  @Id protected String internalType;

  protected Long value = 1L;

  protected EntityIdGenerator() {
    super();
  }

  public EntityIdGenerator(final String internalType) {
    this.internalType = internalType;
  }

  public EntityIdGenerator(final String internalType, final Long startValue) {
    this(internalType);
    value = startValue;
  }

  public Long getValue() {
    return value;
  }
}
