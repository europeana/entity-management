package eu.europeana.entitymanagement.definitions.model.search.results;

import java.util.Date;

public interface IdBean {

	/**
	 * Retrieve the Europeana object unique Id
	 * 
	 * @return The Europeana object UniqueID
	 */
	String getEntityId();
	
//	/**
//	 * The date the record was created
//	 * @return 
//	 */
//	Date getCreatedTimestamp();
	
	/**
	 * The date the record was updated
	 * @return 
	 */
	Date getTimestampUpdated();
}