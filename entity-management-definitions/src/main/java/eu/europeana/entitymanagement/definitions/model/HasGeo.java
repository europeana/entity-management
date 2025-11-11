package eu.europeana.entitymanagement.definitions.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Transient;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.*;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
        ID,
        TYPE,
        LATITUDE,
        LONGITUDE
})
public class HasGeo {

    private String id;

    @Transient
    private String type = "Location";

    private String latitude;
    private String longitude;


    public HasGeo() {
        super();
    }

    public HasGeo(HasGeo copy) {
        super();
        this.id = copy.getId();
        this.type = copy.getType();
        this.latitude = copy.getLatitude();
        this.longitude = copy.getLongitude();
    }

    @JsonGetter(ID)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonGetter(TYPE)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonGetter(LATITUDE)
    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    @JsonGetter(LONGITUDE)
    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
