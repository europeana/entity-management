package eu.europeana.entitymanagement.mongo.repository;

import static dev.morphia.query.Sort.descending;
import org.springframework.stereotype.Repository;
import dev.morphia.query.FindOptions;
import eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants;
import eu.europeana.entitymanagement.web.model.ZohoSyncReport;
import eu.europeana.entitymanagement.web.model.ZohoSyncReportFields;

/** Repository for retrieving the EntityRecord objects. 
 * TODO: move to mongo module together with the object model */
@Repository(AppConfigConstants.BEAN_ZOHO_SYNC_REPO)
public class ZohoSyncRepository extends AbstractRepository {

  /** @return the total number of resources in the database */
  public long count() {
    return getDataStore().find(ZohoSyncReport.class).count();
  }

  /**
   * Deletes all EntityRecord objects that contain the given entityId
   *
   * @param the identifier of the ConceptScheme to be deleted
   * @return the number of deleted objects
   */
//  public long deleteForGood(long identifier) {
//    return getDataStore()
//        .find(ZohoSyncReport.class)
//        .filter(eq(ZohoSyncReportFields.IDENTIFIER, identifier))
//        .delete()
//        .getDeletedCount();
//  }

  /** Drops the ConceptScheme collection. */
  public void dropCollection() {
    getDataStore().getCollection(ZohoSyncReport.class).drop();
  }

  public ZohoSyncReport findLastZohoSyncReport() {
    return getDataStore()
        .find(ZohoSyncReport.class)
        .iterator(
            new FindOptions().sort(descending(ZohoSyncReportFields.START_DATE)))
        .tryNext();
  }

  public ZohoSyncReport save(ZohoSyncReport report) {
    if(report == null || report.getStartDate() == null) {
      throw new IllegalArgumentException("The start date field is mandatory when storing the ZohoSyncReport into the database!");
    }
    return getDataStore().save(report);
  }
}
