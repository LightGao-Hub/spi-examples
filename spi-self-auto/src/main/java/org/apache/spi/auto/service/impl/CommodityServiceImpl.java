package org.apache.spi.auto.service.impl;

import com.google.auto.service.AutoService;
import org.apache.spi.auto.service.Service;

@AutoService(Service.class)
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
