package org.apache.spi.self.service.impl;

import org.apache.spi.self.service.Service;

public class CommodityServiceImpl implements Service {
    @Override
    public void execute() {
        System.out.println("CommodityServiceImpl");
    }

    @Override
    public String getIdentity() {
        return "CommodityServiceImpl";
    }
}
