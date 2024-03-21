package org.apache.spi.employ.jdbc;

import java.util.ServiceLoader;

public class DriverManager {

    public static void getConnection() {
        ServiceLoader<Connection> connectionLoader = ServiceLoader.load(Connection.class);
        for (Connection connection : connectionLoader) {
            System.out.println(connection.getName());
        }
    }
}
