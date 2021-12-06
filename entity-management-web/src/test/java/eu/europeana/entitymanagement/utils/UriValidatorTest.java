package eu.europeana.entitymanagement.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UriValidatorTest {

  @Test
  void shouldValidateURNs() {
    // test cases gotten from https://en.wikipedia.org/wiki/Uniform_Resource_Name#Examples
    assertTrue(UriValidator.isValidUrn("urn:isbn:0451450523"));
    assertTrue(UriValidator.isValidUrn("urn:isan:0000-0000-2CEA-0000-1-0000-0000-Y"));
    assertTrue(UriValidator.isValidUrn("urn:epc:id:sscc:0614141.1234567890"));
    assertTrue(UriValidator.isValidUrn("urn:epc:id:imovn:9176187"));
    assertTrue(UriValidator.isValidUrn("urn:microsoft:adfs:claimsxray"));

    // from agent 146432 sameAs
    assertTrue(UriValidator.isValidUrn("urn:uuid:387a0a33-bc8e-4bfd-8bbc-439691b63546"));

    // invalid values
    assertFalse(
        UriValidator.isValidUrn("urn:randomvalue"), "URN validation passes for invalid value");
    assertFalse(
        UriValidator.isValidUrn("http://randomvalue"), "URN validation passes for invalid value");
  }

  @Test
  void shouldValidateURLs() {
    assertTrue(UriValidator.isValidUrl("http://dbpedia.org/ontology/#book"));
    assertTrue(UriValidator.isValidUrl("https://europeana.eu?testvalue=random"));
    // multiple subdomains /  paths
    assertTrue(UriValidator.isValidUrl("https://g.a.co/kg/m/0bt_c3"));
    assertTrue(UriValidator.isValidUri("http://%C3%BCbersolve.com"));

    // invalid values
    // scheme required
    assertFalse(UriValidator.isValidUrl("www.google.com"));
    // schemes not supported
    assertFalse(UriValidator.isValidUrl("ldap://europeana.eu"));
    assertTrue(UriValidator.isValidUrl("ftp://iconclass.org/49M32"));
    assertTrue(UriValidator.isValidUrl("ftps://zbw.eu/stw/descriptor/13650-3"));
    // non-latin chars must be percent-encoded
    assertFalse(UriValidator.isValidUrl("http://überawesome.com"));
  }
}
