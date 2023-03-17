package eu.europeana.entitymanagement.vocabulary;

import java.lang.reflect.Field;

public interface ValidationEntity {
  
  Object getFieldValue(Field field) throws IllegalArgumentException, IllegalAccessException;
  void setFieldValue(Field field, Object value) throws IllegalArgumentException, IllegalAccessException;

}
