package org.apache.spi.realize.jdbc.mysql;

import org.apache.spi.employ.jdbc.Connection;

public class MysqlConnection implements Connection {
    @Override
    public String getName() {
        return "MysqlConnection";
    }
}
