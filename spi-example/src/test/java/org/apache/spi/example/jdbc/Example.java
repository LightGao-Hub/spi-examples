package org.apache.spi.example.jdbc;

import org.junit.Test;

import java.util.ServiceLoader;

public class Example {
    @Test
    public void example01() {
        ServiceLoader<Connection> serviceLoader = ServiceLoader.load(Connection.class);
        for (Connection search : serviceLoader) {
            System.out.println(search.getName());
        }
    }

}
