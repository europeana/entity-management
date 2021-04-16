package eu.europeana.entitymanagement.common.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "languages")
public class LanguageCodes {

	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "language")
	private List<Language> languages;

	private Map<String, String> altLangMap = new HashMap<String, String>();
	
	public List<Language> getLanguages() {
		return languages;
	}
	
	public Map<String, String> getAltLangMap(){
	    if(altLangMap.isEmpty()) {
		for (Language language : getLanguages()) {
		    for (Language altLang : language.getAlternativeLanguages()) {
			altLangMap.put(altLang.getCode(), language.getCode());
		    }
		}
	    }
	    
	    return altLangMap;
	}
	
	public boolean isValidLanguageCode(String lang) {
	    if(lang == null) {
		return false;
	    }
	    return  getLanguages().stream()
		    .anyMatch(language -> language.getCode().equals(lang));
	}
	
	public String getByAlternativeCode(String altLang) {
	    if(altLang == null || !getAltLangMap().containsKey(altLang)) {
		return null;
	    }
	    
	    return getAltLangMap().get(altLang);
	}

//	@JacksonXmlRootElement(localName = "language")
	public static class Language {
		

		@JacksonXmlProperty(isAttribute = true, localName="code")
		private String code;

		public String getCode() {
			return code;
		}
		
		@JacksonXmlElementWrapper(useWrapping = false)
		@JacksonXmlProperty(localName = "altLang")
		private List<Language> alternativeLanguages;

		public List<Language> getAlternativeLanguages() {
			return alternativeLanguages;		}
		
		@Override
		public String toString() {
		    return getCode(); 
		}

	}

}
