package org.apache.spi.realize.jdbc.oracle;

import org.apache.spi.employ.jdbc.Connection;

public class OracleConnection implements Connection {
    @Override
    public String getName() {
        return "OracleConnection";
    }
}
