package eu.europeana.entitymanagement.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.europeana.entitymanagement.definitions.model.Entity;

public class EntityComparator implements Comparator<Entity> {

    private static final Logger logger = LogManager.getLogger(EntityComparator.class);

    @Override
    public int compare(Entity e1, Entity e2) {

	if (compareClass(e1, e2) > 0) {
	    // verify for null values or different class
	    return 1;
	}

	try {
	    return compareContent(e1, e2);
	} catch (IllegalArgumentException e) {
	    logger.error(
		    "During the comparison of the entity objects an illegal or inappropriate argument has been passed to some method.",
		    e);
	    return 1;
	} catch (IllegalAccessException e) {
	    logger.error(
		    "During the comparison of the entity objects an illegal access to some field has been detected.",
		    e);
	    return 1;
	}
    }

    int compareClass(Object e1, Object e2) {
	if (e1 == null && e2 == null) {
	    //both null
	    return 0;
	} else if(e1 == null || e2 == null) {
	    //only one of them is null
	    return 1;
	} else if (!e1.getClass().equals(e2.getClass())) {
	    //not same class
	    return 1;
	}

	return 0;
    }

    @SuppressWarnings("unchecked")
    int compareContent(Entity e1, Entity e2) throws IllegalAccessException {
	List<Field> allObjectFieldsE1 = new ArrayList<>();
	EntityUtils.getAllFields(allObjectFieldsE1, e1.getClass());

	List<Field> allObjectFieldsE2 = new ArrayList<>();
	EntityUtils.getAllFields(allObjectFieldsE2, e2.getClass());

	for (Field field : allObjectFieldsE1) {

	    if(field.getName().startsWith("tmp")) {
		//ignore corelib adapter fields
		continue;
	    }
		
	    Class<?> fieldType = field.getType();
//	    	String fieldNameE1 = fieldE1.getName();

	    if (fieldType.isArray()) {
		Object[] array1 = (Object[]) e1.getFieldValue(field);
		Object[] array2 = (Object[]) e2.getFieldValue(field);
		if (compareArrays(array1, array2) > 0) {
		    logger.trace("The values of the field {} are not equal!", field.getName());
		    return 1;
		}
	    } else if (Map.class.isAssignableFrom(fieldType)) {
		Map<Object, Object> map1 = (Map<Object, Object>) e1.getFieldValue(field);
		Map<Object, Object> map2 = (Map<Object, Object>) e2.getFieldValue(field);
		if (compareMaps(map1, map2) > 0) {
		    logger.trace("The values of the field {} are not equal!", field.getName());
		    return 1;
		}
	    } else if (List.class.isAssignableFrom(fieldType)) {
		List<Object> list1 = (List<Object>) e1.getFieldValue(field);
		List<Object> list2 = (List<Object>) e2.getFieldValue(field);
		if (compareLists(list1, list2) > 0) {
		    logger.trace("The values of the field {} are not equal!", field.getName());
		    return 1;
		}
	    } else {
		Object obj1 = e1.getFieldValue(field);
		Object obj2 = e2.getFieldValue(field);
		if (compareClass(obj1, obj2) > 0) {
		    // verify for null values or different class
		    logger.trace("The values of the field {} are not equal!", field.getName());
		    return 1;
		} else if (obj1 != null && !obj1.equals(obj2)) {
		    logger.trace("The values of the field {} are not equal!", field.getName());
		    return 1;
		}
	    }

	}
	return 0;

    }

    int compareArrays(Object[] array1, Object[] array2) throws IllegalAccessException {
	   
	List<Object> list1= null, list2 = null; 
	    if (array1 != null) {
		list1 = Arrays.asList(array1);
	    }
	    if (array2 != null) {
		list2 = Arrays.asList(array2);
	    }
	    
	    return compareLists(list1, list2);
	}

    private int compareLists(List<Object> l1, List<Object> l2) {
	if ((l1 == null || (l1 != null && l1.size() == 0)) && (l2 == null || (l2 != null && l2.size() == 0))) {
	    return 0;
	} else if ((l1 == null || (l1 != null && l1.size() == 0)) || (l2 == null || (l2 != null && l2.size() == 0))) {
	    return 1;
	}

	if (l1.size() != l2.size()) {
	    logger.trace("List size is not equal l1 size:{}, l2 size;{}!", l1.size(), l2.size());
	    return 1;
	}

	for (Object l1Elem : l1) {
	    if (!l2.contains(l1Elem)) {
	        logger.trace("List doesn't contain element:{}", l1Elem);
	        return 1;
	    }
	}

	return 0;
    }

    @SuppressWarnings("unchecked")
    private int compareMaps(Map<Object, Object> m1, Map<Object, Object> m2) {
	if ((m1 == null || m1.isEmpty()) && (m2 == null || m2.isEmpty())) {
	    // if both null or empty
	    return 0;
	} else if ((m1 == null || m1.isEmpty()) || m2 == null || m2.isEmpty()) {
	    // if only one map is null or empty
	    return 1;
	}

	if (m1.size() != m2.size()) {
	    // not same size
	    logger.trace("Map size is not equal m1 size:{}, m2 size;{}!", m1.size(), m2.size());
            return 1;
	}

	for (Map.Entry<Object, Object> m1Elem : m1.entrySet()) {
	    if (m2.containsKey(m1Elem.getKey())) {
		Object val1 = m1Elem.getValue();
		Object val2 = m2.get(m1Elem.getKey());
		if (List.class.isAssignableFrom(val1.getClass())) {
		    //value is a list
		    if (compareLists((List<Object>) val1, (List<Object>) val2) > 0) {
		        logger.trace("Map 2 contains different values for key {}!", m1Elem.getKey());     
                        return 1;
		    }
		} else {
		    //value is an object
		    if (!val1.equals(val2)) {
		        logger.trace("Map 2 contains different values for key {}!", m1Elem.getKey());     
	                return 1;
		    }
		}
	    } else {
	        logger.trace("Map 2 doesn't contain key {}!", m1Elem.getKey());     
	        return 1;
	    }
	}

	return 0;
    }

}
