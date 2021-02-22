package eu.europeana.entitymanagement.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import eu.europeana.entitymanagement.common.config.SocksProxyConfig;
import eu.europeana.entitymanagement.config.SocksProxyActivator;

/**
 * Test if activating usage of a sock proxy works fine
 */
public class SockProxyActivatorTest {

    @Test
    public void testPropertiesNotPresent() {
        assertFalse(SocksProxyActivator.activate(new SocksProxyConfig("notpresent.properties")));
    }

    @Test
    public void testPropertiesDisabled() {
        assertFalse(SocksProxyActivator.activate(new SocksProxyConfig("socks_config_disabled.properties")));
    }

    @Test
    public void testPropertiesEnabled() {
        assertTrue(SocksProxyActivator.activate(new SocksProxyConfig("socks_config_enabled.properties")));
    }
}
