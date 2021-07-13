package eu.europeana.entitymanagement.common.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "languages")
public class LanguageCodes {

	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "language")
	private List<Language> languages;
	
	Set<String> supportedLangCodes; 

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
	    return  getSupportedLangCodes().contains(lang.toLowerCase());
	}
	
	
	public boolean isValidAltLanguageCode(String altLangCode) {
            if(altLangCode == null) {
                return false;
            }
            return  getAltLangMap().containsKey(altLangCode.toLowerCase());
        }
	
	public Set<String> getSupportedLangCodes() {
	        if (supportedLangCodes == null) {
	            supportedLangCodes = new HashSet<String>(languages.size());
	            for (Language language : languages) {
	                supportedLangCodes.add(language.getCode());
                    }	          
	        }
	        return supportedLangCodes;
	}
	
	public String getByAlternativeCode(String altLang) {
	    if(altLang == null || !getAltLangMap().containsKey(altLang.toLowerCase())) {
		return null;
	    }
	    
	    return getAltLangMap().get(altLang.toLowerCase());
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
