package com.yourtion.ncn;


import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.yourtion.ncn.utils.ConfUtil;

class MainTest {
    public static void main(String[] args) throws InterruptedException, NacosException {
        ConfUtil.loadConfig();
        NamingFactory.createNamingService(ConfUtil.get("serverAddr")).registerInstance("test", "127.0.0.1", 8888);
        Thread.sleep(10000);
        NamingFactory.createNamingService(ConfUtil.get("serverAddr")).registerInstance("test", "127.0.0.1", 8889);
        Thread.sleep(1000);
        NamingFactory.createNamingService(ConfUtil.get("serverAddr")).registerInstance("test", "127.0.0.1", 8887);
        Thread.sleep(3000);
        NamingFactory.createNamingService(ConfUtil.get("serverAddr")).registerInstance("test", "127.0.0.1", 8886);
        Thread.sleep(1000000);
    }
}