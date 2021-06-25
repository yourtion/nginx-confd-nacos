package com.yourtion.ncn;


import com.alibaba.nacos.api.naming.NamingFactory;
import com.sun.net.httpserver.HttpServer;
import com.yourtion.ncn.utils.ConfUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.LinkedList;
import java.util.List;

class MainTest {
    private static final List<HttpServer> serverList = new LinkedList<>();

    @BeforeAll
    static void setUp() {

    }

    @AfterAll
    static void tearDown() {
        for (HttpServer server : serverList) {
            server.stop(0);
        }
    }

    private static HttpServer startAndRegister(int port) throws Exception {
        HttpServer s = HTTPServer.newHttpServer(port);
        s.start();
        NamingFactory.createNamingService(ConfUtil.get("serverAddr"))
                .registerInstance("test", s.getAddress().getHostName(), s.getAddress().getPort());
        return s;
    }

    public static void main(String[] args) throws Exception {
        ConfUtil.loadConfig();
        HttpServer s1 = startAndRegister(8888);
        serverList.add(s1);
        Thread.sleep(10000);
        HttpServer s2 = startAndRegister(8889);
        serverList.add(s2);
        Thread.sleep(10000);
    }
}