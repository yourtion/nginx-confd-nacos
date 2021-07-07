package com.yourtion.ncn;


import com.alibaba.nacos.api.naming.NamingFactory;
import com.sun.net.httpserver.HttpServer;
import com.yourtion.ncn.utils.ConfUtil;
import com.yourtion.ncn.utils.NginxUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

class MainTest {
    private static final List<HttpServer> serverList = new LinkedList<>();
    private static final int S1 = 5001;
    private static final int S2 = 5002;
    private static final int S3 = 5003;
    private static final int S4 = 5004;
    private static final int S5 = 5005;
    private static final int S6 = 5006;

    private static HttpServer startService(int port) throws Exception {
        HttpServer s = HTTPServer.newHttpServer(port);
        s.start();
        return s;
    }
    @BeforeAll
    static void setUp() throws Exception {
        System.setProperty("ncn.delay", "1");
        System.setProperty("ncn.period", "1");
        ConfUtil.loadConfig();
        serverList.add(startService(S1));
        serverList.add(startService(S2));
        serverList.add(startService(S3));
        serverList.add(startService(S4));
        serverList.add(startService(S5));
        serverList.add(startService(S6));
        Files.write(Paths.get(ConfUtil.get("ncn.conf.upstream")), "".getBytes(StandardCharsets.UTF_8));
        Files.write(Paths.get(ConfUtil.get("ncn.conf.location")), "".getBytes(StandardCharsets.UTF_8));
    }

    @AfterAll
    static void tearDown() {
        for (HttpServer server : serverList) {
            server.stop(0);
        }
    }

    @Test
    void startScheduled() throws Exception {
        NginxUtil nginx = new NginxUtil(System.getenv("NginxUtilTestCMD"));
        ScheduledExecutorService service = Main.startScheduled(nginx, NamingFactory.createNamingService(ConfUtil.get("serverAddr")));
        NamingFactory.createNamingService(ConfUtil.get("serverAddr")).registerInstance("test", "127.0.0.1", S1);
        NamingFactory.createNamingService(ConfUtil.get("serverAddr")).registerInstance("test", "127.0.0.1", S2);
        NamingFactory.createNamingService(ConfUtil.get("serverAddr")).registerInstance("test", "127.0.0.1", S3);
        Thread.sleep(1500);
        HashSet<String> set = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            String res = HTTPServer.sendRequest("http://127.0.0.1:3000/test");
            set.add(res);
        }
        System.out.println("--------- " + set);
        Assertions.assertEquals(set.size(), 3);
        service.shutdown();
    }
}