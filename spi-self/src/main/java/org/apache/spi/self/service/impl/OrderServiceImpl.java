package org.apache.spi.self.service.impl;

import org.apache.spi.self.service.Service;

public class OrderServiceImpl implements Service {
    @Override
    public void execute() {
        System.out.println("OrderServiceImpl");
    }

    @Override
    public String getIdentity() {
        return "OrderServiceImpl";
    }
}
