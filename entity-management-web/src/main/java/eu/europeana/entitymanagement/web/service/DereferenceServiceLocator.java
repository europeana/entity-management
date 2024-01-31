package eu.europeana.entitymanagement.web.service;

import eu.europeana.entitymanagement.dereference.Dereferencer;
import eu.europeana.entitymanagement.wikidata.WikidataDereferenceService;
import eu.europeana.entitymanagement.zoho.organization.ZohoDereferenceService;
import eu.europeana.entitymanagement.zoho.utils.WikidataUtils;
import eu.europeana.entitymanagement.zoho.utils.ZohoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DereferenceServiceLocator {

  private final MetisDereferenceService metisDereferenceService;
  private final ZohoDereferenceService zohoDereferenceService;
  private final WikidataDereferenceService wikidataDereferenceService;

  @Autowired
  public DereferenceServiceLocator(
      MetisDereferenceService metisDereferenceService,
      ZohoDereferenceService zohoDereferenceService,
      WikidataDereferenceService wikidataDereferenceService) {
    this.metisDereferenceService = metisDereferenceService;
    this.zohoDereferenceService = zohoDereferenceService;
    this.wikidataDereferenceService = wikidataDereferenceService;
  }

  /** Gets the {@link Dereferencer} implementation that is applicable for the given id and type */
  public Dereferencer getDereferencer(String id, String entityType) {

    if (WikidataUtils.isWikidataOrganization(id, entityType)) {
      return wikidataDereferenceService;
    }

    if (ZohoUtils.isZohoOrganization(id, entityType)) {
      return zohoDereferenceService;
    }
    
    System.out.println();

    return metisDereferenceService;
  }
}
