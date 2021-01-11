package eu.europeana.entitymanagement.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.io.IOException;

/**
 * Deserializes entity de-referencing responses from Metis
 */
public class DereferenceResponseSerializer extends StdDeserializer<DereferenceResponse> {

    public DereferenceResponseSerializer() {
        this(null);
    }

    public DereferenceResponseSerializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public DereferenceResponse deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException {
        JsonNode root = jp.getCodec().readTree(jp);

        // path is enrichmentBaseWrapperList[0].enrichmentBase.exactMath
        JsonNode exactMatchNode = root.at("/enrichmentBaseWrapperList/0/enrichmentBase/exactMatch");

        if (exactMatchNode.getNodeType() != JsonNodeType.STRING) {
            return null;
        }

        return new DereferenceResponse(exactMatchNode.asText());
    }
}
