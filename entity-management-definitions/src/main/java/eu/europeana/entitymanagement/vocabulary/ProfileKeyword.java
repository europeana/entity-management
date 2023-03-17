package eu.europeana.entitymanagement.vocabulary;

//TODO: This class exists also in the set-api, xonsider moving to the api-commons
public interface ProfileKeyword {

  /**
   * Returns the value used in Linked Data profiles
   *
   * @return
   */
  public String getHeaderValue();

  /**
   * Returns the value used to indicate the profile in Prefer Header
   *
   * @return
   */
  public String getPreferHeaderValue();

//  public static final String VALUE_LD_CONTAINEDIRIS = "http://www.w3.org/ns/oa#PreferContainedIRIs";
//  public static final String VALUE_LD_MINIMAL = "http://www.w3.org/ns/ldp#PreferMinimalContainer";
//  public static final String VALUE_LD_FULL = "http://www.w3.org/ns/oa:PreferContainedDescriptions";
//
//  public static final String VALUE_PREFER_CONTAINEDIRIS =
//      "return=representation;include=\"" + VALUE_LD_CONTAINEDIRIS + "\"";
//  public static final String VALUE_PREFER_MINIMAL =
//      "return=representation;include=\"" + VALUE_LD_MINIMAL + "\"";
//  public static final String VALUE_PREFER_FULL =
//      "return=representation;include=\"" + VALUE_LD_FULL + "\"";
}
