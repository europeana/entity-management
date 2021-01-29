package eu.europeana.entitymanagement.definitions.model;

import dev.morphia.annotations.Embedded;

@Embedded
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
