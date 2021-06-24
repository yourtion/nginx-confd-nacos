package com.yourtion.ncn;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;
import java.util.Map;

/**
 * @author Yourtion
 */
public class NginxConfigGen {
    static class GenRet {
        private final String upstream;
        private final String location;

        @Override
        public String toString() {
            return upstream + location;
        }

        public GenRet(String upstream, String location) {
            this.upstream = upstream;
            this.location = location;
        }

        public String getUpstream() {
            return upstream;
        }

        public String getLocation() {
            return location;
        }
    }

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

    static public GenRet genServer(Map<String, List<Instance>> info) {
        StringBuilder upstream = new StringBuilder();
        StringBuilder proxy = new StringBuilder();
        for (String server : info.keySet()) {
            upstream.append(genUpstream(server, info.get(server)));
            upstream.append("\n");
            proxy.append(genProxy(server));
            proxy.append("\n");
        }
        return new GenRet(upstream.toString(), proxy.toString());
    }
}
