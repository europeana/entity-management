package eu.europeana.entitymanagement.normalization;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.europeana.entitymanagement.common.config.LanguageCodes;
import eu.europeana.entitymanagement.definitions.exceptions.EntityManagementRuntimeException;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.utils.EntityUtils;
import eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes;

import static eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes.FIELD_TYPE_DATE;

public class EntityFieldsCleaner {

    private static final Logger logger = LogManager.getLogger(EntityFieldsCleaner.class);

    LanguageCodes emLanguageCodes;

    public EntityFieldsCleaner(LanguageCodes emLanguageCodes) {
	this.emLanguageCodes = emLanguageCodes;
    }

    public void initialize(EntityFieldsCompleteValidatorInterface constraint) {
    }

    @SuppressWarnings("unchecked")
    public void cleanAndNormalize(Entity entity) {

	List<Field> entityFields = EntityUtils.getAllFields(entity.getClass());

	try {
	    for (Field field : entityFields) {
		Object fieldValue = entity.getFieldValue(field);
		if (fieldValue == null)
		    continue;
		Class<?> fieldType = field.getType();
		if (fieldType.isAssignableFrom(String.class)) {
		    // remove spaces from the String fields
		    normalizeTextField(field, (String) fieldValue, entity);
		} else if (fieldType.isAssignableFrom(String[].class)) {
		    // remove spaces from the String[] fields
		    List<String> normalizedList = normalizeValues(field.getName(),Arrays.asList((String[]) fieldValue));
		    String[] normalized = normalizedList.toArray(new String[normalizedList.size()]);
		    entity.setFieldValue(field, normalized);
		} else if (fieldType.isAssignableFrom(List.class)) {
		 // remove spaces from the List<String> fields
		    entity.setFieldValue(field, normalizeValues(field.getName(),(List<String>) fieldValue));
		} else if (fieldType.isAssignableFrom(Map.class)) {
		    @SuppressWarnings("rawtypes")
		    Map normalized = normalizeMapField(field, (Map) fieldValue, entity);
		    entity.setFieldValue(field, normalized);
		}

	    }
	} catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InvocationTargetException
		| InstantiationException e) {
	    throw new EntityManagementRuntimeException(
		    "Unexpected exception occured during the normalization and cleaning of entity metadata ", e);
	}
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Map normalizeMapField(Field field, Map fieldValue, Entity entity)
	    throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
	if (fieldValue == null) {
	    return null;
	}
	if (isSingleValueStringMap(field)) {
	    return normalizeSingleValueMap((Map<String, String>) fieldValue);
	} else if (isMultipleValueStringMap(field)) {
	    return normalizeMultipleValueMap(field.getName(), (Map<String, List<String>>) fieldValue);
	} else {
		if(logger.isTraceEnabled()) {
			logger.trace("normalization not supported for maps of type: {}", field.getGenericType());
		}
	    return fieldValue;
	}
    }

    @SuppressWarnings({ "unchecked" })
    Map<String, String> normalizeSingleValueMap(Map<String, String> singleValueMap)
	    throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
	// remove spaces from the keys in the Map fields, normalize and remove trailing
	// spaces for the value, apply language normalization and filtering
	// value must be restricted to the 24 language codes that Europeana supports
	Map<String, String> normalizedMap = ConstructorUtils.invokeConstructor(singleValueMap.getClass(),
		singleValueMap.size());
	for (Map.Entry<String, String> mapEntry : singleValueMap.entrySet()) {
	    String normalizedKey = normalizeMapKey(mapEntry.getKey());
	    if (normalizedKey == null) {
		// skip invalid language codes
		continue;
	    }
	    // remove trailing spaces in the value
	    normalizedMap.put(normalizedKey, mapEntry.getValue().trim());
	}
	return normalizedMap;
    }

    @SuppressWarnings({ "unchecked" })
    private Map<String, List<String>> normalizeMultipleValueMap(String fieldName, Map<String, List<String>> singleValueMap)
	    throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
	// remove spaces from the keys in the Map fields
//	normalize and remove trailing spaces for the value
//	 apply language normalization and filtering
//	 value must be restricted to the 24 language codes that Europeana supports
	Map<String, List<String>> normalizedMap = ConstructorUtils.invokeConstructor(singleValueMap.getClass(),
		singleValueMap.size());
	for (Map.Entry<String, List<String>> mapEntry : singleValueMap.entrySet()) {

	    String normalizedKey = normalizeMapKey(mapEntry.getKey());
	    if (normalizedKey == null) {
		// skip invalid language codes
		continue;
	    }

	    List<String> normalizedValues = normalizeValues(fieldName, mapEntry.getValue());
	    // remove trailing spaces in the value
	    normalizedMap.put(normalizedKey, normalizedValues);
	}
	return normalizedMap;

    }

    private List<String> normalizeValues(String fieldName, List<String> values) {
    	List<String> normalized;
    	if(EntityFieldsTypes.getFieldType(fieldName).equals(FIELD_TYPE_DATE)) {
    		normalized = new ArrayList<String>();
	    	for(String value : values) {
	    		String normalizedValue = normalizeTextValue(fieldName,value);
	    		normalized.add(normalizedValue);
	    	}
    	}
    	else {
    		normalized = values.stream().map(String::trim).collect(Collectors.toList());
    	}
    	return normalized;
    }

    String normalizeMapKey(String key) {
	if (key == null) {
	    return null;
	}
	String normalizedKey = key;
	if (key.contains(" ")) {
	    normalizedKey = key.replaceAll("\\s+", "");
	}

	if (normalizedKey.length() < 2) {
	    // invalid Key
	    return null;
	} else if (normalizedKey.length() == 2) {
	    if (!emLanguageCodes.isValidLanguageCode(normalizedKey)) {
		return null;
	    }
	} else {
	    // normalize by altLabel
	    normalizedKey = emLanguageCodes.getByAlternativeCode(normalizedKey);
	}

	return normalizedKey;
    }

    private boolean isMultipleValueStringMap(Field field) {
	Type genericType = field.getGenericType();
	return genericType.getTypeName().contains("Map<java.lang.String, java.util.List<java.lang.String>>");
    }

    private boolean isSingleValueStringMap(Field field) {
	Type genericType = field.getGenericType();
	return genericType.getTypeName().contains("Map<java.lang.String, java.lang.String>");
    }

    private void normalizeTextField(Field field, String fieldValue, Entity entity)
	    throws IllegalArgumentException, IllegalAccessException {
	if (fieldValue != null) {
	    String normalizedValue = normalizeTextValue(field.getName(),fieldValue);
	    if (normalizedValue != fieldValue) {
		entity.setFieldValue(field, normalizedValue);
	    }
	}
    }

    String normalizeTextValue(String fieldName, String fieldValue) {
	if (fieldValue != null) {
	    // remove trailing spaces
	    String normalizedValue = fieldValue.trim();
	    if(EntityFieldsTypes.getFieldType(fieldName).equals(FIELD_TYPE_DATE)) {
	    	normalizedValue=convertDatetimeToDate(normalizedValue);
	    }
	    if (fieldValue != normalizedValue) {
		return normalizedValue;
	    }
	    // TODO: clean fields that are URI or URLS
	}
	return fieldValue;
    }
    
    String convertDatetimeToDate(String fieldValue) {
    	try {
            if(fieldValue.contains("T")) {
                //converts from ISO Date time format to ISO Date format
                return LocalDate.parse(fieldValue, java.time.format.DateTimeFormatter.ISO_DATE_TIME).toString();
            }
        } catch (DateTimeParseException e) {
            return fieldValue;
        }
    	return fieldValue;
    }

}
