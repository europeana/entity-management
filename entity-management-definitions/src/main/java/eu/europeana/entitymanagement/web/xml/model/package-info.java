/**
 * This file is <b>REQUIRED</b> for JAXB qualifying elements.
 *
 * <p>It provides a mapping of a namespace uri and it's prefix value. Classes that contain xml
 * elements and define a namespace will be mapped to the prefix that is declared here.
 */
@XmlSchema(
    xmlns = {
      @XmlNs(prefix = "metis", namespaceURI = "http://www.europeana.eu/schemas/metis"),
      @XmlNs(prefix = "edm", namespaceURI = "http://www.europeana.eu/schemas/edm/"),
      @XmlNs(prefix = "skos", namespaceURI = "http://www.w3.org/2004/02/skos/core#"),
      @XmlNs(prefix = "dcterms", namespaceURI = "http://purl.org/dc/terms/"),
      @XmlNs(prefix = "rdf", namespaceURI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
      @XmlNs(prefix = "rdfs", namespaceURI = "http://www.w3.org/2000/01/rdf-schema"),
      @XmlNs(prefix = "cc", namespaceURI = "http://creativecommons.org/ns"),
      @XmlNs(prefix = "foaf", namespaceURI = "http://xmlns.com/foaf/0.1/"),
      @XmlNs(prefix = "wgs84_pos", namespaceURI = "http://www.w3.org/2003/01/geo/wgs84_pos#"),
      @XmlNs(prefix = "owl", namespaceURI = "http://www.w3.org/2002/07/owl#"),
      @XmlNs(prefix = "xml", namespaceURI = "http://www.w3.org/XML/1998/namespace"),
      @XmlNs(prefix = "dc", namespaceURI = "http://purl.org/dc/elements/1.1/"),
      @XmlNs(prefix = "vcard", namespaceURI = "http://www.w3.org/2006/vcard/ns#"),
      @XmlNs(prefix = "rdaGr2", namespaceURI = "http://rdvocab.info/ElementsGr2/"),
      @XmlNs(prefix = "ore", namespaceURI = "http://www.openarchives.org/ore/terms/")
    },
    namespace = "http://www.europeana.eu/schemas/edm/",
    elementFormDefault = XmlNsForm.QUALIFIED)
// @formatter:on
package eu.europeana.entitymanagement.web.xml.model;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
