package eu.europeana.entitymanagement.schemaorg.model;

import eu.europeana.corelib.edm.model.schemaorg.ContextualEntity;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.utils.SchemaOrgUtils;

public class SchemaOrgAgent extends SchemaOrgEntity<Agent> {

    private final eu.europeana.corelib.edm.model.schemaorg.Person schemaOrgAgent;

    public SchemaOrgAgent(Agent agent) {
    	schemaOrgAgent = new eu.europeana.corelib.edm.model.schemaorg.Person();
    	SchemaOrgUtils.processEntity(agent, schemaOrgAgent);
		
//    	/*
//		 * TODO: create the MultilingualString constructor in the corelib with 
//		 * the language and value parameters and call it in all places where the MultilingualString
//		 * object is instantiated
//		 */
//		if(agent.getName()!=null) {
//			for (Entry<String, String> nameEntry : agent.getName().entrySet()) {
//				MultilingualString nameEntrySchemaOrg = new MultilingualString();
//				nameEntrySchemaOrg.setLanguage(nameEntry.getKey());
//				nameEntrySchemaOrg.setValue(nameEntry.getValue());
//				schemaOrgAgent.addName(nameEntrySchemaOrg);
//			}
//		}
//		
//		if(agent.getDateOfBirth()!=null) {
//			for (String dateOfBirthEach : agent.getDateOfBirth()) {
//				schemaOrgAgent.addBirthDate(new Text(dateOfBirthEach));
//			}
//		}
//		
//		if(agent.getDateOfDeath()!=null) {
//			for (String dateOfDeathEach : agent.getDateOfDeath()) {
//				schemaOrgAgent.addDeathDate(new Text(dateOfDeathEach));
//			}
//		}
//		
//		if(agent.getGender()!=null) 
//		{
//			schemaOrgAgent.addGender(new Text(agent.getGender()));
//		}
//		
//		if(agent.getPlaceOfBirth()!=null) {
//			for (String placeOfBirthEach : agent.getPlaceOfBirth()) {
//				MultilingualString placeOfBirthEntrySchemaOrg = new MultilingualString();
//				/*
//				 * TODO: change the place of birth field in the corelib schemaorg model to be compatible with 
//				 * the given entity management field (without the language parameter)
//				 */
//				placeOfBirthEntrySchemaOrg.setLanguage("");
//				placeOfBirthEntrySchemaOrg.setValue(placeOfBirthEach);
//				schemaOrgAgent.addBirthPlace(placeOfBirthEntrySchemaOrg);
//			}
//		}
//		
//		if(agent.getPlaceOfDeath()!=null) {
//			for (String placeOfDeathEach : agent.getPlaceOfDeath()) {
//				MultilingualString placeOfDeathEntrySchemaOrg = new MultilingualString();
//				/*
//				 * TODO: the same as the comment for the place of birth field
//				 */
//				placeOfDeathEntrySchemaOrg.setLanguage("");
//				placeOfDeathEntrySchemaOrg.setValue(placeOfDeathEach);
//				schemaOrgAgent.addDeathPlace(placeOfDeathEntrySchemaOrg);
//			}	
//		}
//
//
//        setCommonProperties(schemaOrgAgent, agent);
    }

    @Override
    public ContextualEntity get() {
        return schemaOrgAgent;
    }
}
