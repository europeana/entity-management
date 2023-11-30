package eu.europeana.entitymanagement.web.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.exception.FunctionalRuntimeException;
import eu.europeana.entitymanagement.exception.ScoringComputationException;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.web.model.scoring.EntityMetrics;
import eu.europeana.entitymanagement.web.model.scoring.MaxEntityMetrics;
import eu.europeana.entitymanagement.web.model.scoring.PageRank;

@Service(AppConfigConstants.BEAN_EM_SCORING_SERVICE)
public class ScoringService {
  private static final Logger logger = LogManager.getLogger(ScoringService.class);

  private final EnrichmentCountQueryService enrichmentCountQueryService;
  private final SolrClient prSolrClient;

  private MaxEntityMetrics maxEntityMetrics;
  private EntityMetrics maxOverallMetrics;

  private static final int RANGE_EXTENSION_FACTOR = 100;

  public static final String WIKIDATA_PREFFIX = "http://www.wikidata.org/entity/";
  
  public ScoringService(
      EnrichmentCountQueryService enrichmentCountQueryService,
      @Qualifier(AppConfigConstants.BEAN_PR_SOLR_CLIENT) SolrClient prSolrClient) {
    this.enrichmentCountQueryService = enrichmentCountQueryService;
    this.prSolrClient = prSolrClient;
  }

  public EntityMetrics computeMetrics(Entity entity)
      throws FunctionalRuntimeException, UnsupportedEntityTypeException {
    EntityMetrics metrics = new EntityMetrics(entity.getEntityId());
    if (entity.getType() != null) {
      metrics.setEntityType(entity.getType());
    } else {
      metrics.setEntityType(EntityTypes.getByEntityId(entity.getEntityId()).name());
    }

    PageRank pr = getPageRank(entity);
    if (pr != null && pr.getPageRank() != null) {
      metrics.setPageRank(pr.getPageRank().intValue());
    }

    metrics.setEnrichmentCount(getEnrichmentCount(entity));
    computeScore(metrics);
    return metrics;
  }

  private void computeScore(EntityMetrics metrics) throws FunctionalRuntimeException {
    int normalizedPR = 0;
    int normalizedEC = 0;

    try {
      normalizedPR = computeNormalizedMetricValue(metrics, "pageRank");
      normalizedEC = computeNormalizedMetricValue(metrics, "enrichmentCount");
    } catch (IOException e) {
      throw new FunctionalRuntimeException(
          "Cannot compute entity score. There is probably a sistem configuration issue", e);
    }

    int score = normalizedPR * normalizedEC;
    metrics.setScore(score);
  }

  private int computeNormalizedMetricValue(EntityMetrics metrics, String metric)
      throws IOException {

    int metricValue;
    int maxValueForType;
    int maxValueOverall;
    float trust = 1;

    switch (metric) {
      case "pageRank":
        metricValue = metrics.getPageRank();
        if (metricValue <= 1) return 1;
        maxValueForType = getMaxEntityMetrics().maxValues(metrics.getEntityType()).getPageRank();
        maxValueOverall = getMaxOverallMetrics().getPageRank();
        break;
      case "enrichmentCount":
        metricValue = metrics.getEnrichmentCount();
        if (metricValue <= 1) return 1;
        maxValueForType =
            getMaxEntityMetrics().maxValues(metrics.getEntityType()).getEnrichmentCount();
        maxValueOverall = getMaxOverallMetrics().getEnrichmentCount();
        break;

      default:
        throw new FunctionalRuntimeException("Unknown/unsuported metric: " + metric);
    }

    long coordinationFactor = (maxValueOverall) / maxValueForType;
    double normalizedValue =
        1
            + (trust
                * RANGE_EXTENSION_FACTOR
                * Math.log(coordinationFactor * ((double) metricValue)));
    return (int) normalizedValue;
  }

  public EntityMetrics getMaxOverallMetrics() throws IOException {
    if (maxOverallMetrics == null) {
      maxOverallMetrics = new EntityMetrics();
      maxOverallMetrics.setEntityType("all");
      maxOverallMetrics.setEntityId("*");

      int maxPR = 1;
      int maxEC = 1;
      int maxHC = 1;

      for (EntityMetrics metrics : getMaxEntityMetrics().getMaxValues()) {
        maxPR = Math.max(metrics.getPageRank(), maxPR);
        maxEC = Math.max(metrics.getEnrichmentCount(), maxEC);
        maxHC = Math.max(metrics.getHitCount(), maxHC);
      }

      maxOverallMetrics.setPageRank(maxPR);
      maxOverallMetrics.setEnrichmentCount(maxEC);
      maxOverallMetrics.setHitCount(maxHC);
    }

    return maxOverallMetrics;
  }

  private Integer getEnrichmentCount(Entity entity) {
    return enrichmentCountQueryService.getEnrichmentCount(entity);
  }

  private PageRank getPageRank(Entity entity) {
    SolrQuery query = new SolrQuery();
    String wikidataUrl = getWikidataUrl(entity);
    String wikidataQId = StringUtils.substringAfterLast(wikidataUrl, "/");

    if (wikidataUrl == null) {
      return null;
    }

    query.setQuery("identifier:" + wikidataQId);

    try {
      Instant start = Instant.now();
      QueryResponse rsp = prSolrClient.query(query);
      List<PageRank> beans = rsp.getBeans(PageRank.class);

      if (logger.isDebugEnabled()) {
        logger.debug(
            "Retrieved pageRank for entityId={} in {}ms",
            entity.getEntityId(),
            Duration.between(start, Instant.now()).toMillis());
      }
      if (beans.isEmpty()) {
        return null;
      } else {
        return beans.get(0);
      }
    } catch (Exception e) {
      throw new ScoringComputationException(
          "Unexpected exception occured when retrieving pagerank: " + wikidataQId, e);
    }
  }

  private String getWikidataUrl(Entity entity) {
    if (entity.getSameReferenceLinks() == null) {
      return null;
    }
    List<String> values = entity.getSameReferenceLinks();

    return values.stream()
        .filter(value -> value.startsWith(WIKIDATA_PREFFIX))
        .findFirst()
        .orElse(null);
  }

  public MaxEntityMetrics getMaxEntityMetrics() throws IOException {
    if (maxEntityMetrics == null) {
      XmlMapper xmlMapper = new XmlMapper();
      try (InputStream inputStream = getClass().getResourceAsStream("/max-entity-metrics.xml");
          BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
        String contents = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        maxEntityMetrics = xmlMapper.readValue(contents, MaxEntityMetrics.class);
      }
    }
    return maxEntityMetrics;
  }
}
