package com.yourtion.ncn.utils;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.utils.StringUtils;

import java.util.Properties;

/**
 * @author Yourtion
 */
public class NacosUtil {
    static final long TIMEOUT = 5000;

    static public String getNacosConfig(String server, String namespace, String dataId, String group) throws NacosException {
        Properties properties = new Properties();
        properties.put("serverAddr", server);
        if (StringUtils.isNotBlank(namespace)) {
            properties.put("namespace", namespace);
        }
        ConfigService configService = NacosFactory.createConfigService(properties);
        return configService.getConfig(dataId, group, TIMEOUT);
    }
}
