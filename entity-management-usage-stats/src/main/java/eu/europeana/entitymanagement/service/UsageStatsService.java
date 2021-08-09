package eu.europeana.entitymanagement.service;

import eu.europeana.entitymanagement.exception.EntityApiAccessException;
import eu.europeana.entitymanagement.exception.UsageStatsException;
import eu.europeana.entitymanagement.model.EntitiesPerLanguage;
import eu.europeana.entitymanagement.model.Entity;
import eu.europeana.entitymanagement.model.Metric;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Usage Statistics Service class
 *
 * @author Srishti Singh (srishti.singh@europeana.eu)
 * @since 2021-07-29
 */
public class UsageStatsService {

    private static final Logger LOG = LogManager.getLogger(UsageStatsService.class);
    private static final List<String> languages = new ArrayList<>(Arrays.asList("en" , "de", "fr", "fi", "it", "es", "sv", "nl", "pl", "pt", "bg",
            "cs", "da", "hu", "ro", "el", "lt", "sk", "et", "hr", "lv", "sl", "ga", "mt"));

    private EntityApiClient entityApiClient = new EntityApiClient();

    public EntityApiClient getEntityApiClient() {
        return entityApiClient;
    }

    /**
     *  Retrieves the entity api response per language per type in percentages.
     *
     * @param metric
     * @param apikey
     * @throws EntityApiAccessException
     */
    public void getStatsForLang(Metric metric, String apikey) throws EntityApiAccessException, UsageStatsException {
        Entity entityTotal = getEntityApiClient().getEntity(apikey);
        List<EntitiesPerLanguage> entitiesPerLanguageList = new ArrayList<>();
        for(String lang : languages) {
            EntitiesPerLanguage entitiesPerLanguage = getEntityApiClient().getEntitiesForLanguage(lang, apikey, entityTotal);
            if(entitiesPerLanguage != null) {
                entitiesPerLanguageList.add(entitiesPerLanguage);
            } else {
                LOG.error("No stats found for lang {} in entity api", lang);
            }
        }
        metric.setEntities(entitiesPerLanguageList);
    }
}
