package eu.europeana.entitymanagement.batch.errorhandling;


import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import java.util.Date;
import org.bson.types.ObjectId;

@Entity
public class EntityUpdateFailure {

  @Id
  private ObjectId dbId;

  @Indexed
  private String entityId;


  private Date created;
  private String errorMessage;
  private String stackTrace;



}
