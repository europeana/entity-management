package eu.europeana.entitymanagement.web;

import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.entitymanagement.exception.EntityApiAccessException;
import eu.europeana.entitymanagement.exception.UsageStatsException;
import eu.europeana.entitymanagement.model.Metric;
import eu.europeana.entitymanagement.serialization.JsonLdSerializer;
import eu.europeana.entitymanagement.vocabulary.UsageStatsFields;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@RestController
@Validated
public class UsageStatsController extends BaseRest {

    /**
     * Method to generate metric for entity management
     *
     * @param wskey
     * @param request
     * @return
     */
    @ApiOperation(value = "Generate Stats", nickname = "generateStats", response = java.lang.Void.class)
    @GetMapping(value = "/entity/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> generateUsageStats(
            @RequestParam(value = CommonApiConstants.PARAM_WSKEY) String wskey,
            HttpServletRequest request) throws HttpException, EuropeanaApiException {
    verifyReadAccess(request);
    return getEntitiesStats(wskey);
    }

    /**
     * Get the usage statistics for entity Management
     *
     * @return
     */
    private ResponseEntity<String> getEntitiesStats(String apikey) throws EntityApiAccessException, UsageStatsException {
    Metric metric = new Metric();
    metric.setType(UsageStatsFields.OVERALL_TOTAL_TYPE);
    metric.setDescription(UsageStatsFields.DESCRIPTION_VALUE);
    getUsageStatsService().getStatsForLang(metric, apikey);
    metric.setTimestamp(new Date());
    return new ResponseEntity<>(serializeMetricView(metric), HttpStatus.OK);
    }

    /**
     * serialises the metric into json
     * @param metricData
     * @return
     */
    private String serializeMetricView(Metric metricData) {
    JsonLdSerializer serializer = new JsonLdSerializer();
    return serializer.serializeMetric(metricData);
    }
}
