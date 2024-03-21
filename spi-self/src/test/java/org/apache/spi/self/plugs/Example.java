package org.apache.spi.self.plugs;

import org.apache.spi.self.service.Service;
import org.junit.Test;


public class Example {
    @Test
    public void example01() {
        Service service = PluginDiscovery.discoveryService();
        service.execute();
    }

    @Test
    public void example02() {
        Service service = PluginDiscovery.discoveryService("OrderServiceImpl");
        service.execute();
    }
}

