package eu.europeana.entitymanagement.common.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "source")
public class DataSource {

  public static final String FREQ_STATIC = "static";
  public static final String EUROPEANA_ID = "europeana";
  public static final String ZOHO_ID = "zoho-crm";
  public static final String ZOHO_HOST = "crm.zoho.com";

  @JacksonXmlProperty(isAttribute = true)
  private String url;

  @JacksonXmlProperty(isAttribute = true)
  private String rights;

  @JacksonXmlProperty(isAttribute = true)
  private String id;

  @JacksonXmlProperty(isAttribute = true)
  private String frequency;

  public String getUrl() {
    return url;
  }

  public String getRights() {
    return rights;
  }

  public String getId() {
    return id;
  }

  public String getFrequency() {
    return frequency;
  }

  public boolean isStatic() {
    return FREQ_STATIC.equals(getFrequency());
  }
}
