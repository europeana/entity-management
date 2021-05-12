package eu.europeana.entitymanagement.definitions.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;

import dev.morphia.annotations.Embedded;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;

@Embedded
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({WebEntityFields.ID, WebEntityFields.SOURCE, WebEntityFields.THUMBNAIL})
public class WebResource {

    public WebResource() {
        super();
        // TODO Auto-generated constructor stub
    }

    String source;
    String id;
    String thumbnail;
    String type;

    @JsonGetter(WebEntityFields.ID)
    public String getId() {
        return id;
    }

    @JsonGetter(WebEntityFields.SOURCE)
    public String getSource() {
        return source;
    }

    @JsonGetter(WebEntityFields.THUMBNAIL)
    public String getThumbnail() {
        return thumbnail;
    }

    
    @JsonGetter(WebEntityFields.TYPE)
    public String getType() {
        return type;
    }

    
    @JsonSetter(WebEntityFields.THUMBNAIL)
    public void setThumbnail(String thumbnailParam) {
        thumbnail=thumbnailParam;
    }

    
    @JsonSetter(WebEntityFields.SOURCE)
    public void setSource(String sourceParam) {
        source=sourceParam;
    }

    
    @JsonSetter(WebEntityFields.TYPE)
    public void setType(String typeParam) {
        type=typeParam;
    }

    
    @JsonSetter(WebEntityFields.ID)
    public void setId(String idParam) {
        id=idParam;
    }

    
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

    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((thumbnail == null) ? 0 : thumbnail.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        return result;
    }

}
