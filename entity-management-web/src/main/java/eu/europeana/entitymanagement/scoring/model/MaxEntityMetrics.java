package eu.europeana.entitymanagement.scoring.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.europeana.entitymanagement.vocabulary.EntityTypes;

@JacksonXmlRootElement(localName = "metrics")
public class MaxEntityMetrics {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "maxValues")
    List<EntityMetrics> maxValues = new ArrayList<EntityMetrics>();

    public MaxEntityMetrics() {

    }

    public List<EntityMetrics> getMaxValues() {
	return maxValues;
    }

    public void setMaxValues(List<EntityMetrics> maxValues) {
	this.maxValues = maxValues;
    }

    public void addMetrics(EntityMetrics maxValuesForType) {
	this.maxValues.add(maxValuesForType);
    }

    public EntityMetrics maxValues(EntityTypes entityType) {
	return maxValues(entityType.name());
    }

    public EntityMetrics maxValues(String entityType) {
	if (entityType == null) {
	    return null;
	}

	return maxValues.stream().filter(metrics -> entityType.equals(metrics.getEntityType())).findAny().orElse(null);
    }

}
