package eu.europeana.entitymanagement.web.service;

import eu.europeana.entitymanagement.common.config.DataSources;
import eu.europeana.entitymanagement.dereference.Dereferencer;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.wikidata.WikidataAccessService;
import eu.europeana.entitymanagement.zoho.organization.ZohoDereferenceService;
import eu.europeana.entitymanagement.zoho.utils.WikidataUtils;
import eu.europeana.entitymanagement.zoho.utils.ZohoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DereferenceServiceLocator {

  private final MetisDereferenceService metisDereferenceService;
  private final ZohoDereferenceService zohoDereferenceService;
  private final WikidataAccessService wikidataAccessService;

  @Autowired
  public DereferenceServiceLocator(
      MetisDereferenceService metisDereferenceService,
      ZohoDereferenceService zohoDereferenceService,
      WikidataAccessService wikidataAccessService) {
    this.metisDereferenceService = metisDereferenceService;
    this.zohoDereferenceService = zohoDereferenceService;
    this.wikidataAccessService = wikidataAccessService;
  }

  /**
   * Gets the {@link Dereferencer} implementation that is applicable for the given id and type
   */
  public Dereferencer getDereferencer(String id, String entityType) {

    if (WikidataUtils.isWikidataOrganization(id, entityType)) {
      return wikidataAccessService;
    }

    if (ZohoUtils.isZohoOrganization(id, entityType)) {
      return zohoDereferenceService;
    }

    return metisDereferenceService;
  }
}
