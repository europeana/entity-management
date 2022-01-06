package eu.europeana.entitymanagement.serialization;

import eu.europeana.corelib.edm.model.schemaorg.Text;
import eu.europeana.corelib.edm.utils.TextSerializer;

public class EMTextSerializer extends TextSerializer {

  private static final long serialVersionUID = -936926496910746163L;

  public EMTextSerializer(Class<Text> t) {
    super(t);
  }
}
