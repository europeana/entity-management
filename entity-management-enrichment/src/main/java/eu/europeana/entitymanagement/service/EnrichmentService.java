package eu.europeana.entitymanagement.service;

import com.mongodb.MongoException;
import eu.europeana.enrichment.internal.model.EnrichmentTerm;
import eu.europeana.enrichment.service.dao.EnrichmentDao;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.model.EnrichmentEntity;
import eu.europeana.entitymanagement.utils.EnrichmentConstants;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class EnrichmentService {

  private static final Logger LOG = LogManager.getLogger(EnrichmentService.class);

  private EnrichmentDao enrichmentDao;

  @Lazy
  @Autowired
  public EnrichmentService(EnrichmentDao enrichmentDao) {
    this.enrichmentDao = enrichmentDao;
  }

  /**
   * Saves the EnrichmentTerm into the EnrichmentTerm database If the enrichment already exists, get
   * the _id from the existing enrichment and add it to the enrichment to be saved. This will update
   * the existing enrichment.
   *
   * @param entityRecord
   */
  public void saveEnrichment(EntityRecord entityRecord) {
    try {
      EnrichmentTerm enrichmentTerm = new EnrichmentTerm();
      EnrichmentEntity enrichmentEntity = new EnrichmentEntity();

      // set entity
      enrichmentEntity.setAbout(entityRecord.getEntityId());
      enrichmentEntity.setPrefLabel(
          Entity.toStringListMap(entityRecord.getEntity().getPrefLabel()));
      enrichmentEntity.setAltLabel(entityRecord.getEntity().getAltLabel());
      Map<String, List<String>> hiddenLabelEnrichment = new HashMap<String, List<String>>();
      hiddenLabelEnrichment.put(
          "def", new ArrayList<String>(entityRecord.getEntity().getHiddenLabel()));
      enrichmentEntity.setHiddenLabel(hiddenLabelEnrichment);
      enrichmentEntity.setNote(entityRecord.getEntity().getNote());
      if (!CollectionUtils.isEmpty(entityRecord.getEntity().getSameReferenceLinks())) {
        enrichmentEntity.setOwlSameAs(entityRecord.getEntity().getSameReferenceLinks());
      }
      enrichmentEntity.setIsPartOf(String.valueOf(entityRecord.getEntity().getIsPartOfArray()));
      enrichmentEntity.setFoafDepiction(
          entityRecord.getEntity().getDepiction() != null
              ? entityRecord.getEntity().getDepiction().getId()
              : null);

      enrichmentTerm.setEntityType(
          EntityType.valueOf(entityRecord.getEntity().getType().toUpperCase()));
      enrichmentTerm.setEnrichmentEntity(enrichmentEntity);
      // check if it already exists
      Optional<EnrichmentTerm> existingEnrichment =
          enrichmentDao.getEnrichmentTermByField(
              EnrichmentConstants.ENRICHMENT_ABOUT, entityRecord.getEntityId());
      if (existingEnrichment.isEmpty()) {
        enrichmentTerm.setCreated(new Date());
        enrichmentTerm.setUpdated(new Date());
      } else {
        LOG.info(
            "Enrichment already exist for entity {}. Updating the Enrichment",
            entityRecord.getEntityId());
        enrichmentTerm.setCreated(existingEnrichment.get().getCreated());
        enrichmentTerm.setUpdated(new Date());
        // To update - set the existing ID in the enrichment term and save
        enrichmentTerm.setId(existingEnrichment.get().getId());
      }
      enrichmentDao.saveEnrichmentTerm(enrichmentTerm);
    } catch (MongoException e) {
      LOG.error(
          "Error publishing entity {} - {}", entityRecord.getEntity().getAbout(), e.getMessage());
    }
  }
}
