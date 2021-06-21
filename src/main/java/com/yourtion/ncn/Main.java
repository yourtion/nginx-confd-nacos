package com.yourtion.ncn;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.yourtion.ncn.utils.ConfigUtils;

import java.util.Properties;

/**
 * @author Yourtion
 */
public class Main {
    public static void main(String[] args) {
        Properties conf = ConfigUtils.loadConfig();
        System.out.println(conf);
        try {
            Properties properties = new Properties();
            properties.put("serverAddr", conf.getProperty("serverAddr"));
            if (conf.get("namespace") != null) {
                properties.put("namespace", conf.getProperty("namespace"));
            }
            ConfigService configService = NacosFactory.createConfigService(properties);
            String content = configService.getConfig(conf.getProperty("dataId"), conf.getProperty("group"), 5000);
            ConfigUtils.addConfig(content);
            System.out.println(conf);
        } catch (NacosException e) {
            e.printStackTrace();
        }

    }
}
