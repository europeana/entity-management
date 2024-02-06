package eu.europeana.entitymanagement.vocabulary;

import org.apache.commons.lang3.StringUtils;

public enum EntityProfile {
  internal,
  external,
  debug,
  dereference;
  
  public static boolean isDereference(EntityProfile profile) {
    return EntityProfile.dereference.equals(profile);
  }
  
  public static boolean hasDereferenceProfile(String profiles) {
    return StringUtils.isNotEmpty(profiles) && profiles.contains(EntityProfile.dereference.name());
  }
}

