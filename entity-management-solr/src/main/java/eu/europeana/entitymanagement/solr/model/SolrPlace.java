package eu.europeana.entitymanagement.solr.model;

import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;
import eu.europeana.entitymanagement.vocabulary.PlaceSolrFields;
import java.util.ArrayList;
import java.util.List;
import org.apache.solr.client.solrj.beans.Field;

/*
 * TODO:see how to save the wasPresentAt, referencedWebResource, isAggregatedBy, and entityIdentifier fields
 */
public class SolrPlace extends SolrEntity<Place> {

  @Field(EntitySolrFields.SAME_AS)
  private List<String> sameAs;

  @Field(PlaceSolrFields.IS_NEXT_IN_SEQUENCE)
  private List<String> isNextInSequence;

  @Field(PlaceSolrFields.LATITUDE)
  private Float latitude;

  @Field(PlaceSolrFields.LONGITUDE)
  private Float longitude;

  @Field(PlaceSolrFields.ALTITUDE)
  private Float altitude;

  public SolrPlace() {
    super();
  }

  public SolrPlace(Place place) {
    super(place);

    if (place.getIsNextInSequence() != null)
      this.isNextInSequence = new ArrayList<>(place.getIsNextInSequence());
    this.latitude = place.getLatitude();
    this.longitude = place.getLongitude();
    this.altitude = place.getAltitude();
    if (place.getSameReferenceLinks() != null) {
      this.sameAs = new ArrayList<>(place.getSameReferenceLinks());
    }
  }

  public List<String> getIsNextInSequence() {
    return isNextInSequence;
  }

  public Float getLatitude() {
    return latitude;
  }

  public Float getLongitude() {
    return longitude;
  }

  public Float getAltitude() {
    return altitude;
  }

  @Override
  protected void setSameReferenceLinks(ArrayList<String> uris) {
    this.sameAs = uris;
  }
}
