package eu.europeana.entitymanagement.normalization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Check that the entity fields values are valid.
 * @author StevaneticS
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=EntityFieldsMinimalValidator.class)
public @interface EntityFieldsMinimalValidatorInterface {
	
	String message() default "The entity medatada is not valid. See constraint validation list.";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
    
}
