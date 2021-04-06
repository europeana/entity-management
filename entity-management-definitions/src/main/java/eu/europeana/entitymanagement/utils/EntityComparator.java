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

	@SuppressWarnings("unchecked")
	@Override
	public int compare(Entity e1, Entity e2) {
		try {
			if (e1==null && e2==null) {
				return 0;
			}
			if ((e1==null && e2!=null) || (e2==null && e1!=null)) {
				return 1;
			}
			
		    List<Field> allObjectFieldsE1 = new ArrayList<>();
		    EntityUtils.getAllFields(allObjectFieldsE1, e1.getClass());
	
		    List<Field> allObjectFieldsE2 = new ArrayList<>();
		    EntityUtils.getAllFields(allObjectFieldsE2, e2.getClass());
		    
		    if (allObjectFieldsE1.size()!=allObjectFieldsE2.size()) {
		    	return 1;
		    }
	
		    for (Field fieldE1 : allObjectFieldsE1) {
	
				Class<?> fieldTypeE1 = fieldE1.getType();
				String fieldNameE1 = fieldE1.getName();
				boolean theSameFieldExists = false;
				for (Field fieldE2 : allObjectFieldsE2) {
					if (fieldTypeE1.equals(fieldE2.getType()) && fieldNameE1.equals(fieldE2.getName())) {
						theSameFieldExists = true;
						break;
					}
				}
				if (theSameFieldExists) {
					if (fieldTypeE1.isArray()) {					  
						Object[] f1ObjectArrayValue = (Object[]) e1.getFieldValue(fieldE1);						
					    Object[] f2ObjectArrayValue = (Object[]) e2.getFieldValue(fieldE1);
					    List<Object> f1ObjectListValue = null;
					    List<Object> f2ObjectListValue = null;
					    if (f1ObjectArrayValue != null) {
					    	f1ObjectListValue = new ArrayList<>(Arrays.asList(f1ObjectArrayValue));
					    }
					    if (f2ObjectArrayValue != null) {
					    	f2ObjectListValue = new ArrayList<>(Arrays.asList(f2ObjectArrayValue));
					    }

					    if(!compareLists(f1ObjectListValue, f2ObjectListValue)) {
					    	return 1;
					    }

					}
					else if (Map.class.isAssignableFrom(fieldTypeE1)) {
						Map<Object, Object> f1ObjectMapValue = (Map<Object, Object>) e1.getFieldValue(fieldE1);
						Map<Object, Object> f2ObjectMapValue = (Map<Object, Object>) e2.getFieldValue(fieldE1);
					    if(!compareMaps(f1ObjectMapValue, f2ObjectMapValue)) {
					    	return 1;
					    }
					}
					else if (List.class.isAssignableFrom(fieldTypeE1)) {

					    List<Object> f1ObjectListValue = (List<Object>) e1.getFieldValue(fieldE1);
					    List<Object> f2ObjectListValue = (List<Object>) e2.getFieldValue(fieldE1);
					    if(!compareLists(f1ObjectListValue, f2ObjectListValue)) {
					    	return 1;
					    }
					}
					else {
					    Object f1ObjectValue = e1.getFieldValue(fieldE1);
					    Object f2ObjectValue = e2.getFieldValue(fieldE1);
					    if(f1ObjectValue!=null && !f1ObjectValue.equals(f2ObjectValue)) {
					    	return 1;
					    }
					    else if(f2ObjectValue!=null && !f2ObjectValue.equals(f1ObjectValue)) {
					    	return 1;
					    }
					}
	
				}
				else {
					return 1;
				}
		    }
		}
		catch (IllegalArgumentException e) {
			logger.error("During the comparison of the entity objects an illegal or inappropriate argument has been passed to some method.",e);
			return 1;
		} catch (IllegalAccessException e) {
			logger.error("During the comparison of the entity objects an illegal access to some field has been detected.",e);
			return 1;
		}
   
		return 0;
	    
	}
	
	private boolean compareLists (List<Object> l1, List<Object> l2) {
		if((l1==null || (l1!=null && l1.size()==0)) && (l2==null || (l2!=null && l2.size()==0))){
			return true;
		}
		else if ((l1==null || (l1!=null && l1.size()==0)) || (l2==null || (l2!=null && l2.size()==0))) {
			return false;
		}
		
		if (l1.size()!=l2.size()) {
			return false;
		}
		
    	for (Object l1Elem : l1) {
	    		if (!l2.contains(l1Elem)) {
	    			return false;
	    		}
    	}

    	return true;
	}
	
	@SuppressWarnings("unchecked")
	private boolean compareMaps (Map<Object,Object> m1, Map<Object,Object> m2) {
		if((m1==null || (m1!=null && m1.size()==0)) && (m2==null || (m2!=null && m2.size()==0))){
			return true;
		}
		else if ((m1==null || (m1!=null && m1.size()==0)) || (m2==null || (m2!=null && m2.size()==0))) {
			return false;
		}
		
		if (m1.size()!=m2.size()) {
			return false;
		}		

    	for (Map.Entry<Object, Object> m1Elem : m1.entrySet()) {
    		if (m2.containsKey(m1Elem.getKey())) {
    			if (List.class.isAssignableFrom(m1Elem.getValue().getClass())) {
    				if (!compareLists((List<Object>)(m1Elem.getValue()),(List<Object>)(m2.get(m1Elem.getKey())))) {
    					return false;
    				}
    			}
    			else {
    				if (!m1Elem.getValue().equals(m2.get(m1Elem.getKey()))) {
    					return false;
    				}
    			}
    		}
    		else {
    			return false;
    		}
    	}

    	return true;
	}

}
