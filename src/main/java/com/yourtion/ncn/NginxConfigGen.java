package com.yourtion.ncn;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;
import java.util.Map;

/**
 * @author Yourtion
 */
public class NginxConfigGen {
    static public String genUpstream(String name, List<Instance> ins) {
        StringBuilder sb = new StringBuilder();
        sb.append("upstream ").append(name).append(" {\n");
        for (Instance in : ins) {
            sb.append("  " + "server ").append(in.getIp()).append(":").append(in.getPort()).append(";\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    static public String genProxy(String name) {
        return "  location /" + name + "/ {\n" +
                "    proxy_pass http://" + name + ";\n" +
                "  }";
    }

    static public String genServer(Map<String, List<Instance>> info) {
        StringBuilder upstream = new StringBuilder();
        StringBuilder proxy = new StringBuilder();
        for (String server : info.keySet()) {
            upstream.append(genUpstream(server, info.get(server)));
            upstream.append("\n");
            proxy.append(genProxy(server));
            proxy.append("\n");
        }
        StringBuilder sb = new  StringBuilder(upstream);
        sb.append("\n");
        sb.append("server {\n");
        sb.append(proxy);
        sb.append("}\n");
        return sb.toString();

    }
}
