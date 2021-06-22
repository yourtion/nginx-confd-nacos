package com.yourtion.ncn;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.yourtion.ncn.utils.ConfUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * @author Yourtion
 */
public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Properties conf = ConfUtil.loadConfig();
        LOGGER.info("Load config: {}", conf);
        try {
            Properties properties = new Properties();
            properties.put("serverAddr", conf.getProperty("serverAddr"));
            if (conf.get("namespace") != null) {
                properties.put("namespace", conf.getProperty("namespace"));
            }
            ConfigService configService = NacosFactory.createConfigService(properties);
            String content = configService.getConfig(conf.getProperty("dataId"), conf.getProperty("group"), 5000);
            ConfUtil.addConfig(content);
            LOGGER.info("Update config: {}", conf);
            NamingService naming = NamingFactory.createNamingService(properties);
            ListView<String> services = naming.getServicesOfServer(0, ConfUtil.getInt("ncn.pageSize", 100));
            LOGGER.info("services: {}", services);
            for (String service : services.getData()) {
                naming.subscribe(service, event -> {
                    if (event instanceof NamingEvent) {
                        System.out.println(((NamingEvent) event).getServiceName());
                        System.out.println(((NamingEvent) event).getInstances());
                    }
                });
            }
        } catch (NacosException e) {
            e.printStackTrace();
        }

    }
}
