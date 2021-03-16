package eu.europeana.entitymanagement.definitions.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import dev.morphia.annotations.Embedded;
import eu.europeana.entitymanagement.definitions.model.impl.WebResourceImpl;

@Embedded
@JsonDeserialize(as = WebResourceImpl.class)
public interface WebResource {

    void setThumbnail(String source);

    String getThumbnail();

    void setSource(String source);

    String getSource();
    
    void setType(String type);

    String getType();

    void setId(String id);

    String getId();

}
