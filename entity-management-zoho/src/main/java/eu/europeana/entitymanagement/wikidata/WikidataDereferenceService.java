package eu.europeana.entitymanagement.wikidata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.exceptions.EntityCreationException;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.dereference.Dereferencer;
import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import eu.europeana.entitymanagement.web.xml.model.WikidataOrganization;
import eu.europeana.entitymanagement.zoho.utils.WikidataAccessException;

@Service
public class WikidataDereferenceService implements Dereferencer, InitializingBean {
  private static final Logger logger = LogManager.getLogger(WikidataDereferenceService.class);
  private final JAXBContext jaxbContext;
  private final String wikidataBaseUrl;

  /** Create a separate JAXB unmarshaller and Xml Transformer for each thread */
  private ThreadLocal<Unmarshaller> unmarshaller;

  private ThreadLocal<Transformer> transformer;

  @Autowired
  public WikidataDereferenceService(
      JAXBContext jaxbContext, EntityManagementConfiguration entityManagementConfiguration) {
    this.jaxbContext = jaxbContext;
    this.wikidataBaseUrl = entityManagementConfiguration.getWikidataBaseUrl();
  }

  private void setupJaxb() {
    unmarshaller =
        ThreadLocal.withInitial(
            () -> {
              try {
                return jaxbContext.createUnmarshaller();
              } catch (JAXBException e) {
                throw new RuntimeException("Error creating JAXB unmarshaller ", e);
              }
            });
  }

  @Override
  public void afterPropertiesSet() {
    setupJaxb();
    setupXsltTransformer();
  }

  /*
   * Warning: Reading the .xsl template file as InputStream might introduce problems when anything is done
   * with the stream before it is used to create the Transformer object (e.g. transforming it to String, etc.),
   * because the stream can only be manipulated once and the so-called MARK will be at the end
   * (see e.g. https://community.oracle.com/tech/developers/discussion/1627333/premature-end-of-file-error-using-same-inputstream-from-two-functions)
   */
  private void setupXsltTransformer() {
    TransformerFactory transformerFactory = new net.sf.saxon.TransformerFactoryImpl();

    this.transformer =
        ThreadLocal.withInitial(
            () -> {
              try {
                InputStream transformerXSL =
                    WikidataDereferenceService.class.getResourceAsStream("/wkd2org.xsl");
                Transformer newTransformer =
                    transformerFactory.newTransformer(new StreamSource(transformerXSL));
                newTransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                newTransformer.setParameter("deref", Boolean.TRUE);
                newTransformer.setParameter("address", Boolean.TRUE);
                return newTransformer;
              } catch (TransformerConfigurationException e) {
                throw new RuntimeException("Error creating XML transformer ", e);
              }
            });
  }

  @Override
  public Optional<Entity> dereferenceEntityById(String wikidataUri)
      throws WikidataAccessException, EntityCreationException {
    StringBuilder wikidataXml = null;
    WikidataOrganization wikidataOrganization = null;
    try {
      wikidataXml = getEntity(wikidataUri);
      wikidataOrganization = parse(wikidataXml.toString());
    } catch (JAXBException e) {
      logger.debug("Cannot parse wikidata response: {}", wikidataXml);
      throw new WikidataAccessException(
          "Cannot parse wikidata xml response for uri: " + wikidataUri, e);
    }

    if (wikidataOrganization == null) {
      return Optional.empty();
    }
    return Optional.of(wikidataOrganization.getOrganization().toEntityModel());
  }

  private StringBuilder getEntity(String uri) throws WikidataAccessException {
    StringBuilder res = new StringBuilder();
    StreamResult wikidataRes = new StreamResult(new StringBuilderWriter(res));
    String entityResponse = getEntityFromURL(uri);
    // transform the response
    if (StringUtils.hasLength(entityResponse)) {
      try (InputStream stream =
          new ByteArrayInputStream(entityResponse.getBytes(StandardCharsets.UTF_8))) {
        transformer.get().setParameter("targetId", uri);
        transformer.get().transform(new StreamSource(stream), wikidataRes);

      } catch (TransformerException | IOException e) {
        throw new WikidataAccessException("Error transforming Wikidata response", e);
      }
    }
    return res;
  }

  /**
   * Method to get the RDF/xml response from wikidata using entityId GET :
   * <http://www.wikidata.org/entity/xyztesting>
   *
   * @param urlToRead
   * @return
   * @throws WikidataAccessException
   */
  private String getEntityFromURL(String urlToRead) throws WikidataAccessException {

    // wikidataBaseUrl is only set in integration tests (where a mock Wikidata service is used)
    if (StringUtils.hasLength(wikidataBaseUrl)) {
      urlToRead = wikidataBaseUrl + "/entity/" + EntityRecordUtils.getIdFromUrl(urlToRead);
    }

    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      HttpGet request = new HttpGet(urlToRead);
      request.addHeader("Accept", "application/xml");
      try (CloseableHttpResponse response = httpClient.execute(request)) {
        if (response.getStatusLine().getStatusCode() != 200) {
          return null;
        }
        HttpEntity entity = response.getEntity();
        if (entity != null) {
          return EntityUtils.toString(entity);
        }
      }
    } catch (IOException e) {
      throw new WikidataAccessException("Error executing the request for uri " + urlToRead, e);
    }
    return null;
  }

  private WikidataOrganization parse(String xml) throws JAXBException {
    InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    return (WikidataOrganization) unmarshaller.get().unmarshal(stream);
  }
}
