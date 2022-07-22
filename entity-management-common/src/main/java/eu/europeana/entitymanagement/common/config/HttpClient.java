package eu.europeana.entitymanagement.common.config;

import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class HttpClient {

  public static String httpGetClient(String url, Map<String, String> headers, Map<String, String> params) throws Exception {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpGet httpGet = new HttpGet(url);
    if(headers!=null) {
      for(Map.Entry<String, String> entry : headers.entrySet()) {
        httpGet.addHeader(entry.getKey(), entry.getValue());
      }
    }
    
    URIBuilder uriBuilder = new URIBuilder(httpGet.getURI());
    if(params!=null) {
      for(Map.Entry<String, String> entry : params.entrySet()) {
        uriBuilder.addParameter(entry.getKey(), entry.getValue());
      }
    }    
    ((HttpRequestBase) httpGet).setURI(uriBuilder.build());
    
    CloseableHttpResponse response = httpClient.execute(httpGet);
    if (response.getStatusLine().getStatusCode() != 200) {
      return null;
    }
    HttpEntity entity = response.getEntity();
    if (entity != null) {
      return EntityUtils.toString(entity);
    }
    return null;

  }
}
