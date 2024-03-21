package org.apache.spi.example.jdbc;

public class OracleConnection implements Connection {
    @Override
    public String getName() {
        return "OracleConnection";
    }
}
