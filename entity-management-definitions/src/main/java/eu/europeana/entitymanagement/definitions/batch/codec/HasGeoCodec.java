package eu.europeana.entitymanagement.definitions.batch.codec;

import com.mongodb.MongoClient;
import eu.europeana.entitymanagement.definitions.model.HasGeo;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.*;

public class HasGeoCodec implements Codec<HasGeo> {

    @Override
    public HasGeo decode(BsonReader reader, DecoderContext decoderContext) {
        HasGeo hasGeo = new HasGeo();
        hasGeo.setId(reader.readString());
        return hasGeo;
    }

    @Override
    public void encode(BsonWriter writer, HasGeo hasGeo, EncoderContext encoderContext) {
        if (hasGeo != null) {
            writer.writeStartDocument();
            writer.writeString(ID, hasGeo.getId());
            writer.writeString(LATITUDE, hasGeo.getLatitude());
            writer.writeString(LONGITUDE, hasGeo.getLongitude());
        }
    }

    @Override
    public Class<HasGeo> getEncoderClass() {
        return HasGeo.class;
    }
}

