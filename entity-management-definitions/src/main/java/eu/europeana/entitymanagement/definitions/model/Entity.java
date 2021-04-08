package eu.europeana.entitymanagement.definitions.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import dev.morphia.annotations.Embedded;
import eu.europeana.corelib.definitions.edm.entity.ContextualClass;
import eu.europeana.entitymanagement.definitions.model.impl.BaseEntity;

@Embedded
@JsonDeserialize(as = BaseEntity.class)
public interface Entity extends ContextualClass {

	public String[] getIdentifier();

	public void setIdentifier(String[] identifier);

	public String[] getIsRelatedTo();

	public void setIsRelatedTo(String[] isRelatedTo);

	public String getEntityId();

	public void setEntityId(String enitityId);

	public String getType();
	
	public void setType(String type);

	public String[] getSameAs();

	public String getDepiction();
	
	public void setDepiction(String depiction);


	/**
	 * Retrieves the preferable label for a contextual class (language,value)
	 * 
	 * @return A Map<String, String> for the preferable labels of a contextual class 
	 *         (one per language)
	 */
	public Map<String, String> getPrefLabelStringMap();
	
	WebResource getReferencedWebResource();
	
	void setReferencedWebResource(WebResource resource);
	
	String getIsShownBy();
	
	void setIsShownBy (WebResource webResource);
	
	Object getFieldValue (Field field) throws IllegalArgumentException, IllegalAccessException;
	
	void setFieldValue (Field field, Object value) throws IllegalArgumentException, IllegalAccessException;

	void setIsAggregatedBy(Aggregation isAggregatedBy);

	Aggregation getIsAggregatedBy();

	void setFoafDepiction(String foafDepiction);

	void setSameAs(String[] sameAs);
	
	public String[] getIsPartOfArray();

	void setIsPartOfArray(String[] isPartOf);
	
	public String[] getHasPart();

	void setHasPart(String[] hasPart);

	void setNote(Map<String, List<String>> note);

	void setHiddenLabel(Map<String, List<String>> hiddenLabel);

	void setAltLabel(Map<String, List<String>> altLabel);

	void setPrefLabelStringMap(Map<String, String> prefLabel);

	void copyShellFrom(Entity entity);
}
