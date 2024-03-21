package org.apache.spi.auto.plugs;

import org.apache.spi.auto.service.Service;

import java.util.ServiceLoader;

public class PluginDiscovery {

    /** 按类型匹配, 取默认第一个 */
    public static Service discoveryService() {
        ServiceLoader<Service> serviceLoader = ServiceLoader.load(Service.class);
        return serviceLoader.iterator().next();
    }

    /** 按类型及ID匹配 */
    public static Service discoveryService(String id) {
        ServiceLoader<Service> serviceLoader = ServiceLoader.load(Service.class);
        for (Service service : serviceLoader) {
            if (service.getIdentity().equalsIgnoreCase(id)) {
                return service;
            }
        }
        throw new RuntimeException(String.format("not find Id:%s Service Class", id));
    }

}
