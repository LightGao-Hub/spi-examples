package org.apache.spi.example.jdbc;

public class MysqlConnection implements Connection {
    @Override
    public String getName() {
        return "MysqlConnection";
    }
}
