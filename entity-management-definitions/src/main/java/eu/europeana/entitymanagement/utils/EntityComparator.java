package eu.europeana.entitymanagement.utils;

import java.io.Serializable;
import java.util.Comparator;
import eu.europeana.entitymanagement.definitions.model.Entity;

public class EntityComparator implements Comparator<Entity>, Serializable {

  private static final long serialVersionUID = 4897357254139647262L;

//  private static final Logger LOGGER = LogManager.getLogger(EntityComparator.class);
//  private static final String MAP_ERROR_MESSAGE = "Map 2 contains different values for key {}!";

  @Override
  public int compare(Entity e1, Entity e2) {

    //check for null values and class equality
    if (e1 == null && e2 == null) {
      // both null
      return 0;
    } else if (e1 == null || e2 == null) {
      // only one of them is null
      return 1;
    } else if (!e1.getClass().equals(e2.getClass())) {
      // not same class
      return 1;
    }
    
    return e1.equals(e2) ? 0 : 1;
  }

}
