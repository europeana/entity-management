package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ID;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.SOURCE;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.THUMBNAIL;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import dev.morphia.annotations.Embedded;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import java.util.Objects;

@Embedded
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ID, WebEntityFields.TYPE, SOURCE, THUMBNAIL})
public class WebResource {

  public static final String TYPE = "WebResource";
  private String source;
  private String id;
  private String thumbnail;

  public WebResource() {
    super();
  }

  public WebResource(WebResource copy) {
    this.source = copy.getSource();
    this.id = copy.getId();
    this.thumbnail = copy.getThumbnail();
  }

  public WebResource(String id, String source, String thumbnail) {
    this.id = id;
    this.source = source;
    this.thumbnail = thumbnail;
  }

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

  @JsonGetter(WebEntityFields.TYPE)
  public String getType() {
    return TYPE;
  }

  @JsonSetter(THUMBNAIL)
  public void setThumbnail(String thumbnailParam) {
    thumbnail = thumbnailParam;
  }

  @JsonSetter(SOURCE)
  public void setSource(String sourceParam) {
    source = sourceParam;
  }

  @JsonSetter(ID)
  public void setId(String idParam) {
    id = idParam;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    WebResource that = (WebResource) o;

    if (!Objects.equals(source, that.source)) return false;
    if (!id.equals(that.id)) return false;
    return Objects.equals(thumbnail, that.thumbnail);
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
