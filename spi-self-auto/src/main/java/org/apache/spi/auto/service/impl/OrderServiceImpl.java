package org.apache.spi.auto.service.impl;

import com.google.auto.service.AutoService;
import org.apache.spi.auto.service.Service;

@AutoService(Service.class)
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
