package eu.europeana.entitymanagement.util;

import static org.junit.jupiter.api.Assertions.*;

import eu.europeana.entitymanagement.utils.UriValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UriValidatorTest {

  @Test
  void shouldValidateURNs() {
    // test cases gotten from https://en.wikipedia.org/wiki/Uniform_Resource_Name#Examples
    Assertions.assertTrue(UriValidator.isUri("urn:isbn:0451450523"));
    assertTrue(UriValidator.isUri("urn:isan:0000-0000-2CEA-0000-1-0000-0000-Y"));
    assertTrue(UriValidator.isUri("urn:epc:id:sscc:0614141.1234567890"));
    assertTrue(UriValidator.isUri("urn:epc:id:imovn:9176187"));
    assertTrue(UriValidator.isUri("urn:microsoft:adfs:claimsxray"));

    // from agent 146432 sameAs
    assertTrue(UriValidator.isUri("urn:uuid:387a0a33-bc8e-4bfd-8bbc-439691b63546"));
  }

  @Test
  void shouldValidateURLs() {
    assertTrue(UriValidator.isUri("http://user:password@europeana.eu:80?testvalue=random"));
    assertTrue(UriValidator.isUri("http://dbpedia.org/ontology/#book"));
    assertTrue(UriValidator.isUri("https://europeana.eu?testvalue=random"));
    // multiple subdomains /  paths
    assertTrue(UriValidator.isUri("https://g.a.co/kg/m/0bt_c3"));
    assertTrue(UriValidator.isUri("http://%C3%BCbersolve.com"));
    // latin chars supported
    assertTrue(UriValidator.isUri("http://überawesome.com"));

    /* invalid values */
    // scheme required
    assertFalse(UriValidator.isUri("www.google.com"));
    // free text
    assertFalse(UriValidator.isUri("the current state of the world"));
    // schemes not supported
    assertFalse(UriValidator.isUri("ldap://europeana.eu"));
    assertFalse(UriValidator.isUri("ftp://iconclass.org/49M32"));
    assertFalse(UriValidator.isUri("ftps://zbw.eu/stw/descriptor/13650-3"));
  }
}
