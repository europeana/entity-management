package eu.europeana.entitymanagement.definitions.model.impl;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import eu.europeana.entitymanagement.definitions.model.WebResource;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.vocabulary.XmlFields;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({WebEntityFields.ID, WebEntityFields.SOURCE, WebEntityFields.THUMBNAIL})
public class WebResourceImpl implements WebResource{

	public WebResourceImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

	String source;
	String id;
	String thumbnail;
	String type;

	@JsonGetter(WebEntityFields.ID)
	@JacksonXmlProperty(isAttribute= true, localName = XmlFields.XML_RDF_ABOUT)
	public String getId() {
		return id;
	}
	
	@JsonGetter(WebEntityFields.SOURCE)
	@JacksonXmlProperty(localName = XmlFields.XML_DC_SOURCE)
	public String getSource() {
		return source;
	}
	
	@JsonGetter(WebEntityFields.THUMBNAIL)
	@JacksonXmlProperty(localName = XmlFields.XML_FOAF_THUMBNAIL)
	public String getThumbnail() {
		return thumbnail;
	}
	
	@Override
	@JsonGetter(WebEntityFields.TYPE)
	@JacksonXmlProperty(localName = XmlFields.XML_RDF_TYPE)
	public String getType() {
		return type;
	}

	@Override
	@JsonSetter(WebEntityFields.THUMBNAIL)
	public void setThumbnail(String thumbnailParam) {
		thumbnail=thumbnailParam;		
	}

	@Override
	@JsonSetter(WebEntityFields.SOURCE)
	public void setSource(String sourceParam) {
		source=sourceParam;
	}

	@Override
	@JsonSetter(WebEntityFields.TYPE)
	public void setType(String typeParam) {
		type=typeParam;
	}

	@Override
	@JsonSetter(WebEntityFields.ID)
	public void setId(String idParam) {
		id=idParam;
	}
	
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        WebResource guest = (WebResource) obj;
        return (id == guest.getId() || (id!=null && id.equals(guest.getId()))) &&
        	   (thumbnail == guest.getThumbnail() || (thumbnail!=null && thumbnail.equals(guest.getThumbnail()))) && 
        	   (source == guest.getSource() || (source!=null && source.equals(guest.getSource())));
        
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((thumbnail == null) ? 0 : thumbnail.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        return result;
    }

}
