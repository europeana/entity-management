package eu.europeana.entitymanagement.definitions.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import dev.morphia.annotations.Embedded;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ID;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.SOURCE;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.THUMBNAIL;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.TYPE;

@Embedded
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ID, SOURCE, THUMBNAIL})
public class WebResource {

    public WebResource(WebResource copy) {
		this.source = copy.getSource();
		this.id = copy.getId();
		this.thumbnail = copy.getThumbnail();
		this.type = copy.getType();
	}

	public WebResource() {
        super();
        // TODO Auto-generated constructor stub
    }

    String source;
    String id;
    String thumbnail;
    String type;

    @JsonGetter(ID)
    public String getId() {
        return id;
    }

    @JsonGetter(SOURCE)
    public String getSource() {
        return source;
    }

    @JsonGetter(THUMBNAIL)
    public String getThumbnail() {
        return thumbnail;
    }
    
    @JsonGetter(TYPE)
    public String getType() {
        return type;
    }
    
    @JsonSetter(THUMBNAIL)
    public void setThumbnail(String thumbnailParam) {
        thumbnail=thumbnailParam;
    }

    
    @JsonSetter(SOURCE)
    public void setSource(String sourceParam) {
        source=sourceParam;
    }

    
    @JsonSetter(TYPE)
    public void setType(String typeParam) {
        type=typeParam;
    }

    
    @JsonSetter(ID)
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
