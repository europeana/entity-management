package eu.europeana.entitymanagement.definitions.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HasGeo {

    private String id;
    private String type;
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



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
