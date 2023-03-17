package eu.europeana.entitymanagement.vocabulary;

//TODO: This class exists also in the set-api, xonsider moving to the api-commons
public abstract class ProfileConstants {

    public static final String VALUE_LD_CONTAINEDIRIS = "http://www.w3.org/ns/oa#PreferContainedIRIs";
    public static final String VALUE_LD_MINIMAL = "http://www.w3.org/ns/ldp#PreferMinimalContainer";
    public static final String VALUE_LD_ITEM_DESCRIPTIONS = "http://www.w3.org/ns/oa#PreferContainedDescription";
    public static final String VALUE_PARAM_FACETS = "facets";
    public static final String VALUE_PARAM_DEBUG = "debug";
    public static final String VALUE_PARAM_MINIMAL = "minimal";
    public static final String VALUE_PARAM_STANDARD = "standard";
    public static final String VALUE_PARAM_ITEMDESCRIPTIONS = "itemDescriptions";


    public static final String COMMON_STRING = "return=representation;include=\"";

    public static final String VALUE_PREFER_CONTAINEDIRIS = COMMON_STRING + VALUE_LD_CONTAINEDIRIS + "\"";
    public static final String VALUE_PREFER_MINIMAL = COMMON_STRING + VALUE_LD_MINIMAL + "\"";
    public static final String VALUE_PREFER_ITEM_DESCRIPTIONS = COMMON_STRING + VALUE_LD_ITEM_DESCRIPTIONS + "\"";

    private ProfileConstants() {}
}
