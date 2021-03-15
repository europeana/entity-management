package eu.europeana.entitymanagement.definitions.model.impl;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.Timespan;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.vocabulary.XmlFields;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class TimespanImpl extends BaseEntity implements Timespan, eu.europeana.corelib.definitions.edm.entity.Timespan {

	public TimespanImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TimespanImpl(Timespan copy) {
		super(copy);
		this.isNextInSequence = copy.getIsNextInSequence()!=null ? Arrays.copyOf(copy.getIsNextInSequence(),copy.getIsNextInSequence().length) : null;
		this.begin = copy.getBeginString();
		this.end = copy.getEndString();
	}

	private String[] isNextInSequence;
	private String begin;
	private String end;

    @JsonGetter(WebEntityFields.IS_NEXT_IN_SEQUENCE)
    @JacksonXmlProperty(localName = XmlFields.XML_EDM_IS_NEXT_IN_SEQUENCE)
    public String[] getIsNextInSequence() {
	return isNextInSequence;
    }

    @JsonSetter(WebEntityFields.IS_NEXT_IN_SEQUENCE)
    public void setIsNextInSequence(String[] isNextInSequence) {
	this.isNextInSequence = isNextInSequence;
    }

    @JsonSetter(WebEntityFields.BEGIN)
    public void setBeginString(String begin) {
        this.begin = begin;
    }

    @JsonSetter(WebEntityFields.END)
    public void setEndString(String end) {
        this.end = end;
    }

    
    @Override
    @JsonGetter(WebEntityFields.BEGIN)
    @JacksonXmlProperty(localName = XmlFields.XML_EDM_BEGIN)
    public String getBeginString() {
	return begin;
    }

    @Override
    @JsonGetter(WebEntityFields.END)
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
    public String getType() {
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
