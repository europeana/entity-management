package eu.europeana.entitymanagement.serialization;

public class PositiveNumberFilter {

  @Override
  public boolean equals(Object other) {
    // Trick required to be compliant with the Jackson Custom attribute processing
    if (other == null) {
      return true;
    }

    if (other instanceof Integer) {
      return (Integer) other < 0;
    }

    if (other instanceof Double) {
      return (Double) other < 0;
    }

    return false;
  }
}