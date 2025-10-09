package eu.europeana.entitymanagement.web.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jboss.logging.Logger;

import java.io.IOException;
import static eu.europeana.api.commons.web.http.HttpHeaders.CONTENT_TYPE;
import static eu.europeana.api.commons.web.http.HttpHeaders.CONTENT_TYPE_JSON_UTF8;

/**
 * Class to connect and publish report to slack
 * @author srishti singh
 * @since 15 September 2025
 */
public class SlackConnection {
    private static final Logger LOG         = Logger.getLogger(SlackConnection.class);

    private String slackWebhook;
    private CloseableHttpClient httpClient;

    /**
     * Construct slack connection instance with webhook
     */
    public SlackConnection(String slackWebhook) {
        this.slackWebhook = slackWebhook;
        this.httpClient = HttpClients.createDefault();

    }

    /**
     * Sends the message to configured slack channel.
     * @param message - message body
     */
    public  void publishStatusReport(String message) {
        LOG.info("Sending Slack Message : " + message);
        try {
            if (StringUtils.isBlank(slackWebhook)) {
                LOG.error("Slack webhook not configured, status report will not be published over Slack.");
                return;
            }

            HttpPost httpPost = new HttpPost(slackWebhook);
            httpPost.setEntity(new StringEntity(message));
            httpPost.setHeader(CONTENT_TYPE, CONTENT_TYPE_JSON_UTF8);
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                LOG.info("Received status " + response.getStatusLine().getStatusCode() + " while calling slack!");
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    LOG.info(" Successfully sent slack message !");
                }
            }
        } catch (IOException e) {
            LOG.warn("Exception occurred while sending slack message !! " + e.getMessage());
        }
    }
}