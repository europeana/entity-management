package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ENTITY_URI;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.WIKIDATA_URI;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ZOHO_LABEL;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
  ZOHO_LABEL,
  ENTITY_URI,
  WIKIDATA_URI
})
public class ZohoMapping {

  private String zohoLabel;
  private String entityUri;
  private String wikidataUri;
  
  @JsonGetter(ZOHO_LABEL)
  public String getZohoLabel() {
    return zohoLabel;
  }
  
  @JsonSetter(ZOHO_LABEL)
  public void setZohoLabel(String zohoLabel) {
    this.zohoLabel = zohoLabel;
  }
  
  @JsonGetter(ENTITY_URI)
  public String getEntityUri() {
    return entityUri;
  }
  
  @JsonSetter(ENTITY_URI)
  public void setEntityUri(String entityUri) {
    this.entityUri = entityUri;
  }
  
  @JsonGetter(WIKIDATA_URI)
  public String getWikidataUri() {
    return wikidataUri;
  }
  
  @JsonSetter(WIKIDATA_URI)
  public void setWikidataUri(String wikidataUri) {
    this.wikidataUri = wikidataUri;
  }
  
//  public static String getEntityUriFromName(List<ZohoMapping> list ,String name) {
//    for(ZohoMapping cm : list) {
//      List<String> splittedAndTrimmed = Arrays.stream(cm.getZohoLabel().split(",")).map(String::trim).toList();
//      if(splittedAndTrimmed.contains(name)) {
//        return cm.getEntityUri();
//      }
//    }
//    return null;
//  }
}
