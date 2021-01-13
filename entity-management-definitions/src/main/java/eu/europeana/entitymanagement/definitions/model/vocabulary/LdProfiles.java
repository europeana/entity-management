package eu.europeana.entitymanagement.definitions.model.vocabulary;

/**
 * This enumeration is intended for Linked Data profiles
 * 
 * @author GrafR
 *
 */
public enum LdProfiles implements ProfileKeyword {

	MINIMAL(VALUE_PREFER_MINIMAL), STANDARD(VALUE_PREFER_CONTAINEDIRIS), FULL(VALUE_PREFER_FULL);
	
	private String preferHeaderValue;
	

	LdProfiles(String preferHeaderValue){
		this.preferHeaderValue = preferHeaderValue;
	}
	
	@Override
	public String getHeaderValue() {
		return preferHeaderValue;
	}
	
	@Override
	public String toString() {
		return getHeaderValue();
	}

	public String getPreferHeaderValue() {
		return preferHeaderValue;
	}
	
}
