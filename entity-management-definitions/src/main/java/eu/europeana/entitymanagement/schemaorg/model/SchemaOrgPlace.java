package eu.europeana.entitymanagement.schemaorg.model;

import eu.europeana.corelib.edm.model.schemaorg.ContextualEntity;
import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.utils.SchemaOrgUtils;

public class SchemaOrgPlace extends SchemaOrgEntity<Place> {

    private final eu.europeana.corelib.edm.model.schemaorg.Place schemaOrgPlace;

    public SchemaOrgPlace(Place place) {
        schemaOrgPlace = new eu.europeana.corelib.edm.model.schemaorg.Place();
        SchemaOrgUtils.processEntity(place, schemaOrgPlace);        

//        if (place.getLatitude() != null && place.getLongitude() != null && place.getAltitude() != null) {
//            GeoCoordinates geo = new GeoCoordinates();
//            geo.addLatitude(new Text(String.valueOf(place.getLatitude())));
//            geo.addLongitude(new Text(String.valueOf(place.getLongitude())));
//            geo.addElevation(new Text(String.valueOf(place.getAltitude())));
//            schemaOrgPlace.addGeo(geo);
//        }
//
//        setCommonProperties(schemaOrgPlace, place);
    }

    @Override
    public ContextualEntity get() {
        return schemaOrgPlace;
    }
}
