package eu.europeana.entitymanagement.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test if loading sock proxy configuration works fine
 */
public class SockProxyConfigTest {

    @Test
    public void testPropertiesNotPresent() {
        SocksProxyConfig config = new SocksProxyConfig("notpresent.properties");
        assertFalse(config.isSocksEnabled());
    }

    /**
     * Test if loading multiple property files (first one where socks is enabled and then one where it is disabled) works
     */
    @Test
    public void testPropertiesDisabled() {
        SocksProxyConfig config = new SocksProxyConfig("socks_config_enabled.properties",
                "socks_config_disabled.properties");
        assertFalse(config.isSocksEnabled());
    }

    /**
     * Test if loading multiple property files (first one where socks is disabled and then one where it is enabled) works
     */
    @Test
    public void testPropertiesEnabled() {
        SocksProxyConfig config = new SocksProxyConfig("socks_config_disabled.properties",
                "socks_config_enabled.properties");
        assertTrue(config.isSocksEnabled());
        assertEquals("test.com", config.getHost());
        assertEquals("12345", config.getPort());
        assertEquals("user", config.getUser());
        assertEquals("secret", config.getPassword());
    }

}
