package eu.europeana.entitymanagement.vocabulary;

import eu.europeana.entitymanagement.definitions.exceptions.EntityValidationException;

public enum EntityFieldsTypes {

    id(EntityFieldsTypes.FIELD_TYPE_URI,  false, EntityFieldsTypes.FIELD_CARDINALITY_1_1),
    entityId(EntityFieldsTypes.FIELD_TYPE_URI,  false, EntityFieldsTypes.FIELD_CARDINALITY_1_1),
    type(EntityFieldsTypes.FIELD_TYPE_KEYWORD, false, EntityFieldsTypes.FIELD_CARDINALITY_1_1),
    depiction(EntityFieldsTypes.FIELD_TYPE_URI, false, EntityFieldsTypes.FIELD_CARDINALITY_0_1),
    referencedWebResource(EntityFieldsTypes.FIELD_TYPE_WEB_RESOURCE, false, EntityFieldsTypes.FIELD_CARDINALITY_0_1),
    prefLabel(EntityFieldsTypes.FIELD_TYPE_TEXT, true, EntityFieldsTypes.FIELD_CARDINALITY_0_1),
    altLabel(EntityFieldsTypes.FIELD_TYPE_TEXT, true, EntityFieldsTypes.FIELD_CARDINALITY_0_INFINITE),
    hiddenLabel(EntityFieldsTypes.FIELD_TYPE_TEXT, true, EntityFieldsTypes.FIELD_CARDINALITY_0_INFINITE),
    name(EntityFieldsTypes.FIELD_TYPE_TEXT, true, EntityFieldsTypes.FIELD_CARDINALITY_0_INFINITE),
    begin(EntityFieldsTypes.FIELD_TYPE_DATE, false, EntityFieldsTypes.FIELD_CARDINALITY_0_1),
    end(EntityFieldsTypes.FIELD_TYPE_DATE, false, EntityFieldsTypes.FIELD_CARDINALITY_0_1),
    dateOfBirth(EntityFieldsTypes.FIELD_TYPE_DATE, false, EntityFieldsTypes.FIELD_CARDINALITY_0_1),
    dateOfEstablishment(EntityFieldsTypes.FIELD_TYPE_DATE, false, EntityFieldsTypes.FIELD_CARDINALITY_0_1),
    dateOfDeath(EntityFieldsTypes.FIELD_TYPE_DATE, false, EntityFieldsTypes.FIELD_CARDINALITY_0_1),
    dateOfTermination(EntityFieldsTypes.FIELD_TYPE_DATE, false, EntityFieldsTypes.FIELD_CARDINALITY_0_1),
    date(EntityFieldsTypes.FIELD_TYPE_DATE_OR_URI, false, EntityFieldsTypes.FIELD_CARDINALITY_0_INFINITE),
    placeOfBirth(EntityFieldsTypes.FIELD_TYPE_TEXT_OR_URI, true, EntityFieldsTypes.FIELD_CARDINALITY_0_1),
    placeOfDeath(EntityFieldsTypes.FIELD_TYPE_TEXT_OR_URI, true, EntityFieldsTypes.FIELD_CARDINALITY_0_1),
    gender(EntityFieldsTypes.FIELD_TYPE_TEXT, false, EntityFieldsTypes.FIELD_CARDINALITY_0_1),
    /*
     * TODO: change the professionOrOccupation field to be multilingual if the type of the field in the class changes to Map.
     * According to the specifications this field is multilingual but for now we keep it not.
     */    
    professionOrOccupation(EntityFieldsTypes.FIELD_TYPE_TEXT_OR_URI, false, EntityFieldsTypes.FIELD_CARDINALITY_0_INFINITE),
    biographicalInformation(EntityFieldsTypes.FIELD_TYPE_TEXT, true, EntityFieldsTypes.FIELD_CARDINALITY_0_INFINITE),
    note(EntityFieldsTypes.FIELD_TYPE_TEXT, true, EntityFieldsTypes.FIELD_CARDINALITY_0_INFINITE),
    hasPart(EntityFieldsTypes.FIELD_TYPE_URI, false, EntityFieldsTypes.FIELD_CARDINALITY_0_INFINITE),
    isPartOf(EntityFieldsTypes.FIELD_TYPE_URI, false, EntityFieldsTypes.FIELD_CARDINALITY_0_INFINITE),
    hasMet(EntityFieldsTypes.FIELD_TYPE_URI, false, EntityFieldsTypes.FIELD_CARDINALITY_0_INFINITE),
    isRelatedTo(EntityFieldsTypes.FIELD_TYPE_URI, false, EntityFieldsTypes.FIELD_CARDINALITY_0_INFINITE),
    wasPresentAt(EntityFieldsTypes.FIELD_TYPE_URI, false, EntityFieldsTypes.FIELD_CARDINALITY_0_INFINITE),
    identifier(EntityFieldsTypes.FIELD_TYPE_KEYWORD, false, EntityFieldsTypes.FIELD_CARDINALITY_0_INFINITE),
    sameAs(EntityFieldsTypes.FIELD_TYPE_URI, false, EntityFieldsTypes.FIELD_CARDINALITY_0_INFINITE),
    latitude(EntityFieldsTypes.FIELD_TYPE_FLOAT, false, EntityFieldsTypes.FIELD_CARDINALITY_0_1),
    longitude(EntityFieldsTypes.FIELD_TYPE_FLOAT, false, EntityFieldsTypes.FIELD_CARDINALITY_0_1),
    altitude(EntityFieldsTypes.FIELD_TYPE_FLOAT, false, EntityFieldsTypes.FIELD_CARDINALITY_0_1),
    isNextInSequence(EntityFieldsTypes.FIELD_TYPE_URI, false, EntityFieldsTypes.FIELD_CARDINALITY_0_INFINITE),
    notation(EntityFieldsTypes.FIELD_TYPE_TEXT, false, EntityFieldsTypes.FIELD_CARDINALITY_0_INFINITE),
    broader(EntityFieldsTypes.FIELD_TYPE_URI, false, EntityFieldsTypes.FIELD_CARDINALITY_0_INFINITE),
    narrower(EntityFieldsTypes.FIELD_TYPE_URI, false, EntityFieldsTypes.FIELD_CARDINALITY_0_INFINITE),
    related(EntityFieldsTypes.FIELD_TYPE_URI, false, EntityFieldsTypes.FIELD_CARDINALITY_0_INFINITE),
    broadMatch(EntityFieldsTypes.FIELD_TYPE_URI, false, EntityFieldsTypes.FIELD_CARDINALITY_0_INFINITE),
    narrowMatch(EntityFieldsTypes.FIELD_TYPE_URI, false, EntityFieldsTypes.FIELD_CARDINALITY_0_INFINITE),
    exactMatch(EntityFieldsTypes.FIELD_TYPE_URI, false, EntityFieldsTypes.FIELD_CARDINALITY_0_INFINITE),
    relatedMatch(EntityFieldsTypes.FIELD_TYPE_URI, false, EntityFieldsTypes.FIELD_CARDINALITY_0_INFINITE),
    closeMatch(EntityFieldsTypes.FIELD_TYPE_URI, false, EntityFieldsTypes.FIELD_CARDINALITY_0_INFINITE),
    inScheme(EntityFieldsTypes.FIELD_TYPE_URI, false, EntityFieldsTypes.FIELD_CARDINALITY_0_INFINITE),
    acronym(EntityFieldsTypes.FIELD_TYPE_TEXT, true, EntityFieldsTypes.FIELD_CARDINALITY_0_INFINITE),
    description(EntityFieldsTypes.FIELD_TYPE_TEXT, true, EntityFieldsTypes.FIELD_CARDINALITY_0_1),
    logo(EntityFieldsTypes.FIELD_TYPE_URI, false, EntityFieldsTypes.FIELD_CARDINALITY_0_1),
    europeanaRole(EntityFieldsTypes.FIELD_TYPE_KEYWORD, true, EntityFieldsTypes.FIELD_CARDINALITY_1_INFINITE),
    organizationDomain(EntityFieldsTypes.FIELD_TYPE_KEYWORD, true, EntityFieldsTypes.FIELD_CARDINALITY_1_INFINITE),
    geographicLevel(EntityFieldsTypes.FIELD_TYPE_KEYWORD, true, EntityFieldsTypes.FIELD_CARDINALITY_1_1),
    country(EntityFieldsTypes.FIELD_TYPE_TEXT, false, EntityFieldsTypes.FIELD_CARDINALITY_1_1),
    homepage(EntityFieldsTypes.FIELD_TYPE_URI, false, EntityFieldsTypes.FIELD_CARDINALITY_0_1),
    phone(EntityFieldsTypes.FIELD_TYPE_KEYWORD, false, EntityFieldsTypes.FIELD_CARDINALITY_0_INFINITE),
    mbox(EntityFieldsTypes.FIELD_TYPE_EMAIL, false, EntityFieldsTypes.FIELD_CARDINALITY_0_INFINITE),
    hasAddress(EntityFieldsTypes.FIELD_TYPE_URI, false, EntityFieldsTypes.FIELD_CARDINALITY_0_1),
    streetAddress(EntityFieldsTypes.FIELD_TYPE_TEXT, false, EntityFieldsTypes.FIELD_CARDINALITY_0_1),
    postalCode(EntityFieldsTypes.FIELD_TYPE_KEYWORD, false, EntityFieldsTypes.FIELD_CARDINALITY_0_1),
    postBox(EntityFieldsTypes.FIELD_TYPE_KEYWORD, false, EntityFieldsTypes.FIELD_CARDINALITY_0_1),
    locality(EntityFieldsTypes.FIELD_TYPE_TEXT, false, EntityFieldsTypes.FIELD_CARDINALITY_0_1),
    countryName(EntityFieldsTypes.FIELD_TYPE_TEXT, false, EntityFieldsTypes.FIELD_CARDINALITY_1_1),
    hasGeo(EntityFieldsTypes.FIELD_TYPE_URI, false, EntityFieldsTypes.FIELD_CARDINALITY_1_1),
    source(EntityFieldsTypes.FIELD_TYPE_URI, false, EntityFieldsTypes.FIELD_CARDINALITY_1_1),
    thumbnail(EntityFieldsTypes.FIELD_TYPE_URI, false, EntityFieldsTypes.FIELD_CARDINALITY_1_1),
    created(EntityFieldsTypes.FIELD_TYPE_DATE, false, EntityFieldsTypes.FIELD_CARDINALITY_1_1),
    modified(EntityFieldsTypes.FIELD_TYPE_DATE, false, EntityFieldsTypes.FIELD_CARDINALITY_1_1),
    pageRank(EntityFieldsTypes.FIELD_TYPE_INTEGER, false, EntityFieldsTypes.FIELD_CARDINALITY_1_1),
    recordCount(EntityFieldsTypes.FIELD_TYPE_INTEGER, false, EntityFieldsTypes.FIELD_CARDINALITY_1_1),
    score(EntityFieldsTypes.FIELD_TYPE_INTEGER, false, EntityFieldsTypes.FIELD_CARDINALITY_1_1),
    aggregates(EntityFieldsTypes.FIELD_TYPE_URI, false, EntityFieldsTypes.FIELD_CARDINALITY_1_1),
    proxyFor(EntityFieldsTypes.FIELD_TYPE_URI, false, EntityFieldsTypes.FIELD_CARDINALITY_1_1),  
	TMP_KEY(EntityFieldsTypes.FIELD_TYPE_TEXT, false, EntityFieldsTypes.FIELD_CARDINALITY_1_1);
	
	public static final String FIELD_TYPE_URI = "URI";
	public static final String FIELD_TYPE_INTEGER = "Integer";
	public static final String FIELD_TYPE_DATE = "Date";
	public static final String FIELD_TYPE_TEXT = "Text";
	public static final String FIELD_TYPE_KEYWORD = "Keyword";
	public static final String FIELD_TYPE_EMAIL = "Email";
	public static final String FIELD_TYPE_FLOAT = "Float";
	public static final String FIELD_TYPE_TEXT_OR_URI = "Text or URI";
	public static final String FIELD_TYPE_DATE_OR_URI = "Date or URI";
	public static final String FIELD_TYPE_WEB_RESOURCE = "WebResource";
	
	public static final String FIELD_CARDINALITY_1_1 = "1..1";
	public static final String FIELD_CARDINALITY_0_1 = "0..1";
	public static final String FIELD_CARDINALITY_0_INFINITE = "0..*";
	public static final String FIELD_CARDINALITY_1_INFINITE = "1..*";
	
    private final String fieldType;
    private final boolean fieldIsmultilingual;
    private final String fieldCardinality;

    private EntityFieldsTypes (String fieldType, boolean fieldIsmultilingual, String fieldCardinality) {
        this.fieldType = fieldType;
        this.fieldIsmultilingual = fieldIsmultilingual;
        this.fieldCardinality = fieldCardinality;
    }

    public String getFieldType() {
        return fieldType;
    }
    
    public static boolean hasTypeDefinition(String fieldName) {
        try {
            valueOf(fieldName);
            return true;
        }catch (IllegalArgumentException e){
            return false;
        }
    }
    
    
    public static String getFieldType(String fieldName) {
        try {
            return valueOf(fieldName).getFieldType();
        }catch (IllegalArgumentException e){
            throw new EntityValidationException("Unknown field: " + fieldName, e);
        }
    }

    public static boolean isMultilingual(String fieldName) {
        try {
            return valueOf(fieldName).getFieldIsmultilingual();     
        }catch (IllegalArgumentException e){
            throw new EntityValidationException("Unknown field: " + fieldName, e);
        }
        
    }
    
    public static boolean isList(String fieldName) {
        try {
            String cardinality = valueOf(fieldName).getFieldCardinality();
            return FIELD_CARDINALITY_0_INFINITE.equals(cardinality) || FIELD_CARDINALITY_1_INFINITE.equals(cardinality);
        }catch (IllegalArgumentException e){
            throw new EntityValidationException("Unknown field: " + fieldName, e);
        }
    }
    
    boolean getFieldIsmultilingual() {
        return fieldIsmultilingual;
    }
    
    public String getFieldCardinality() {
        return fieldCardinality;
    }

    
}
