package com.yourtion.ncn;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.alibaba.nacos.common.utils.StringUtils;
import com.yourtion.ncn.utils.ConfUtil;
import com.yourtion.ncn.utils.FileUtil;
import com.yourtion.ncn.utils.NacosUtil;
import com.yourtion.ncn.utils.NginxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Yourtion
 */
public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final ConcurrentHashMap<String, List<Instance>> CURRENT_CONFIG = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, List<Instance>> QUEUE = new ConcurrentHashMap<>();
    private static NginxConfigGen.GenRet lastGen= null;

    private static void processEvent(Event event) {
        if (event instanceof NamingEvent) {
            NamingEvent e = (NamingEvent) event;
            LOGGER.debug("ProcessEvent : {} : {}", e.getServiceName(), e.getInstances());
            QUEUE.put(e.getServiceName(), e.getInstances());
        }
    }

    public static void main(String[] args) {
        Properties conf = ConfUtil.loadConfig();
        LOGGER.info("Load config: {}", conf);
        try {
            // 加载 Nacos 配置（如果有）
            String addr = ConfUtil.get("serverAddr");
            String namespace = ConfUtil.get("namespace");
            String dataId = ConfUtil.get("dataId");
            String group = ConfUtil.get("group");
            if (StringUtils.isNotBlank(dataId) && StringUtils.isNotBlank(group)) {
                String content = NacosUtil.getNacosConfig(addr, namespace, dataId, group);
                ConfUtil.addConfig(content);
                LOGGER.info("Update config: {}", conf);
            }
            // 根据配置检查环境信息（Nginx路径、运行状态）
            NginxUtil nginx = new NginxUtil(ConfUtil.get("ncn.nginx"));
            if (!nginx.checkNginxOk()) {
                LOGGER.error("Nginx Error");
            }
            NamingService naming = NamingFactory.createNamingService(addr);
            startScheduled(nginx, naming);
        } catch (NacosException e) {
            e.printStackTrace();
        }
    }

    static ScheduledExecutorService startScheduled(NginxUtil nginx, NamingService naming) {
        //noinspection AlibabaThreadPoolCreation
        ScheduledExecutorService scheduledThreadPool = Executors.newSingleThreadScheduledExecutor();
        scheduledThreadPool.scheduleAtFixedRate(() -> {
            LOGGER.debug("scheduleAtFixedRate");
            try {
                ListView<String> serviceList = naming.getServicesOfServer(0, ConfUtil.getInt("ncn.pageSize", 100));
                for (String service : serviceList.getData()) {
                    if (!CURRENT_CONFIG.containsKey(service)) {
                        naming.subscribe(service, Main::processEvent);
                    }
                }
                if (lastGen != null && QUEUE.isEmpty()) {
                    return;
                }
                LOGGER.debug("start processing");
                HashMap<String, List<Instance>> config = new HashMap<>(CURRENT_CONFIG);
                config.putAll(QUEUE);
                QUEUE.clear();
                // TODO: 处理已经被移除的服务
                for (String service : config.keySet()) {
                    if (config.get(service).isEmpty()) {
                        naming.unsubscribe(service, Main::processEvent);
                        config.remove(service);
                    }
                }
                NginxConfigGen.GenRet ret = NginxConfigGen.genServer(config);
                if (!ret.equals(lastGen)) {
                    // TODO: 错误处理
                    boolean uOk = FileUtil.backupAndWrite(ConfUtil.get("ncn.conf.upstream"), ret.getUpstream());
                    boolean lOk = FileUtil.backupAndWrite(ConfUtil.get("ncn.conf.location"), ret.getLocation());
                    if (!uOk) {
                        FileUtil.rollback(ConfUtil.get("ncn.conf.upstream"));
                    }
                    if (!lOk) {
                        FileUtil.rollback(ConfUtil.get("ncn.conf.location"));
                    }
                    boolean nOk = nginx.checkNginxOk();
                    boolean rOk = nginx.reload();
                    lastGen = ret;
                }
                // 成功则替换配置
                CURRENT_CONFIG.clear();
                CURRENT_CONFIG.putAll(config);
            } catch (Exception e) {
                LOGGER.error("ScheduleAtFixedRateError: ", e);
            }
        }, ConfUtil.getInt("ncn.delay", 10), ConfUtil.getInt("ncn.period", 5), TimeUnit.SECONDS);
        return scheduledThreadPool;
    }
}
