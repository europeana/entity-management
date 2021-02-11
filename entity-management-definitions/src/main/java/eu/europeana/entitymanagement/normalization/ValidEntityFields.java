package eu.europeana.entitymanagement.normalization;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Check that the entity fields values are valid.
 * @author StevaneticS
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=ValidEntityFieldsValidator.class)
public @interface ValidEntityFields {
	
	String message() default "The entity fields values are valid.";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
    
}
