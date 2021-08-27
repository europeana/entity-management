package eu.europeana.entitymanagement.wikidata;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import eu.europeana.entitymanagement.web.xml.model.WikidataOrganization;
import eu.europeana.entitymanagement.zoho.utils.WikidataAccessException;

/**
 * Wikidata Dao class
 *
 * @author Srishti Singh (srishti.singh@europeana.eu)
 * @since 2021-07-06
 */
public class WikidataAccessDao {

    private Transformer transformer;

    /*
     * Warning: Reading the .xsl template file as InputStream might introduce problems when anything is done 
     * with the stream before it is used to create the Transformer object (e.g. transforming it to String, etc.),
     * because the stream can only be manipulated once and the so-called MARK will be at the end 
     * (see e.g. https://community.oracle.com/tech/developers/discussion/1627333/premature-end-of-file-error-using-same-inputstream-from-two-functions)
     */
    public WikidataAccessDao() throws WikidataAccessException, IOException {

            InputStream transformerXSL = WikidataAccessDao.class.getResourceAsStream("/wkd2org.xsl");
            TransformerFactory transformerFactory = new net.sf.saxon.TransformerFactoryImpl();
            try {
                StreamSource xslt = new StreamSource(transformerXSL);
                this.transformer = transformerFactory.newTransformer(xslt);
                this.transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                this.transformer.setParameter("deref", Boolean.TRUE);
                this.transformer.setParameter("address", Boolean.TRUE);
            } catch (TransformerConfigurationException var4) {
                throw new WikidataAccessException("Transformer could not be initialized.", var4);
            }
        
    }

    public StringBuilder getEntity(String uri) throws WikidataAccessException {
        StringBuilder res = new StringBuilder();
        StreamResult wikidataRes = new StreamResult(new StringBuilderWriter(res));
        String entityResponse = getEntityFromURL(uri);
        // transform the response
        if(entityResponse != null) {
            try (InputStream stream = new ByteArrayInputStream(entityResponse.getBytes(StandardCharsets.UTF_8))) {
                this.transformer.setParameter("rdf_about", uri);
                this.transformer.transform(new StreamSource(stream), wikidataRes);

            } catch (TransformerException | IOException e) {
                throw new WikidataAccessException("Error by transforming of Wikidata in RDF/XML.", e);
            }
        }
        return res;

    }

    /**
     * Method to get the RDF/xml response from wikidata using entityId
     * GET : <http://www.wikidata.org/entity/xyztesting>
     *
     * @param urlToRead
     * @return
     * @throws WikidataAccessException
     */
    public String getEntityFromURL(String urlToRead) throws WikidataAccessException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()){
            HttpGet request = new HttpGet(urlToRead);
            request.addHeader("Accept", "application/xml");
            CloseableHttpResponse response = httpClient.execute(request);
            try {
                if (response.getStatusLine().getStatusCode() != 200) {
                    return null;
                }
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                	return EntityUtils.toString(entity);
                }
            } finally {
                response.close();
            }
        } catch (IOException e) {
            throw new WikidataAccessException("Error executing the request for uri "+urlToRead, e);
        }
        return null;

    }

    public WikidataOrganization parse(File xmlFile) throws JAXBException, IOException {
        String xml = FileUtils.readFileToString(xmlFile, StandardCharsets.UTF_8);
        return this.parse(xml);
    }

    public WikidataOrganization parse(InputStream xmlStream) throws JAXBException, IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(xmlStream, writer, StandardCharsets.UTF_8);
        String wikidataXml = writer.toString();
        System.out.println(wikidataXml);
        return this.parse(wikidataXml);
    }

    public WikidataOrganization parse(String xml) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(new Class[]{WikidataOrganization.class});
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        return (WikidataOrganization)unmarshaller.unmarshal(stream);
    }

    public WikidataOrganization parseWikidataOrganization(File inputFile) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(new Class[]{WikidataOrganization.class});
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        return (WikidataOrganization)unmarshaller.unmarshal(inputFile);
    }
}

