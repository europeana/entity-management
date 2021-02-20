package eu.europeana.entitymanagement.batch.entity;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.bson.types.ObjectId;
import org.springframework.batch.item.ExecutionContext;

@Entity
public class ExecutionContextEntity {
    @Id
    private ObjectId _id;

    private ExecutionContext executionContext;
}
