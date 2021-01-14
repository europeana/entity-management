package eu.europeana.entitymanagement.definitions.model.vocabulary;

import eu.europeana.entitymanagement.definitions.exceptions.UnsupportedAlgorithmTypeException;

public enum SuggestAlgorithmTypes {

	suggest, suggestByLabel, monolingual;
	
	public static SuggestAlgorithmTypes getByName(String name) throws UnsupportedAlgorithmTypeException{

		for(SuggestAlgorithmTypes algorithmType : SuggestAlgorithmTypes.values()){
			if(algorithmType.name().equalsIgnoreCase(name))
				return algorithmType;
		}
		throw new UnsupportedAlgorithmTypeException(name);
	}	
	
}
