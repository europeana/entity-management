package eu.europeana.entitymanagement.common.config;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "languages")
public class LanguageCodes {

	@JacksonXmlRootElement(localName = "language")
	public static class Language {
		
		@JacksonXmlRootElement(localName = "altLang")
		public static class AlternativeLanguage {
			
		  @JacksonXmlProperty(isAttribute = true)
		  private String code;

		  public String getCode() {
			return code;
		  }
		  
		}

		@JacksonXmlProperty(isAttribute = true)
		private String code;

		public String getCode() {
			return code;
		}
		
		@JacksonXmlElementWrapper(useWrapping = false)
		@JacksonXmlProperty(localName = "altLang")
	    private List<AlternativeLanguage> alternativeLanguages;

		public List<AlternativeLanguage> getAlternativeLanguages() {
			return alternativeLanguages;		}


	}
	
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "language")
    private List<Language> languages;

	public List<Language> getLanguages() {
		return languages;
	}

}
