package eu.europeana.entitymanagement.definitions.model.impl;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import eu.europeana.entitymanagement.definitions.model.Timespan;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.vocabulary.XmlFields;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class TimespanImpl extends BaseEntity implements Timespan, eu.europeana.corelib.definitions.edm.entity.Timespan {

	private String[] isNextInSequence;
	private String begin;
	private String end;

    @JsonProperty(WebEntityFields.IS_NEXT_IN_SEQUENCE)
    @JacksonXmlProperty(localName = XmlFields.XML_EDM_IS_NEXT_IN_SEQUENCE)
    public String[] getIsNextInSequence() {
	return isNextInSequence;
    }

    public void setIsNextInSequence(String[] isNextInSequence) {
	this.isNextInSequence = isNextInSequence;
    }

    public void setBeginString(String begin) {
        this.begin = begin;
    }

    public void setEndString(String end) {
        this.end = end;
    }

    
    @Override
    @JsonProperty(WebEntityFields.BEGIN)
    @JacksonXmlProperty(localName = XmlFields.XML_EDM_BEGIN)
    public String getBeginString() {
	return begin;
    }

    @Override
    @JsonProperty(WebEntityFields.END)
    @JacksonXmlProperty(localName = XmlFields.XML_EDM_END)
    public String getEndString() {
	return end;
    }
    
    @Override
    @Deprecated
    public void setIsPartOf(Map<String, List<String>> isPartOf) {
	// TODO Auto-generated method stub
    }

    @Override
    @Deprecated
    public Map<String, List<String>> getBegin() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    @Deprecated
    public Map<String, List<String>> getDctermsHasPart() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    @Deprecated
    public Map<String, List<String>> getEnd() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    @Deprecated
    public Map<String, List<String>> getIsPartOf() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    @Deprecated
    public void setBegin(Map<String, List<String>> arg0) {
	// TODO Auto-generated method stub
	
    }

    @Override
    @Deprecated
    public void setDctermsHasPart(Map<String, List<String>> arg0) {
	// TODO Auto-generated method stub
	
    }

    @Override
    @Deprecated
    public void setEnd(Map<String, List<String>> arg0) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public String getInternalType() {
        return "Timespan";
    }
    
	@Override
	public Object getFieldValue(Field field) throws IllegalArgumentException, IllegalAccessException {
		//TODO:in case of the performance overhead cause by using the reflecion code, change this method to call the getters for each field individually
		return field.get(this);
	}

	@Override
	public void setFieldValue(Field field, Object value) throws IllegalArgumentException, IllegalAccessException {
		//TODO:in case of the performance overhead cause by using the reflecion code, change this method to call the setter for each field individually
		field.set(this, value);
	}

}
