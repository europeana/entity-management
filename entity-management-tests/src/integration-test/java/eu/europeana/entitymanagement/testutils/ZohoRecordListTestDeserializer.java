package eu.europeana.entitymanagement.testutils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.zoho.crm.api.record.Record;

/** Helper class to deserialize JSON into Zoho {@link Record} to make testing easier */
public class ZohoRecordListTestDeserializer extends BaseZohoRecordDeserializer<List<Record>> {

 
  public ZohoRecordListTestDeserializer() {
    this(null);
  }

  public ZohoRecordListTestDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public List<Record> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    JsonNode jsonRootNode = p.getCodec().readTree(p);
    if(!jsonRootNode.isArray()) {
      throw new IOException("Verify json input, it must be an array!");
    }
    
    List<Record> res = new ArrayList<>();
    for (JsonNode jsonNode : jsonRootNode) {
      res.add(deserializeSingleRecord(jsonNode));
    }
    return res;
  }
}
