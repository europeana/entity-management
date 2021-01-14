package eu.europeana.entitymanagement.definitions.model.search;

public enum SearchProfiles {
	
	facets;

	public static boolean contains(String value) {

	    for (SearchProfiles c : SearchProfiles.values()) {
	        if(c.name().equalsIgnoreCase(value))
	        	return true;
	    }

	    return false;
	}	

	@Override
	public String toString() {
		return name().toLowerCase();
	}
}