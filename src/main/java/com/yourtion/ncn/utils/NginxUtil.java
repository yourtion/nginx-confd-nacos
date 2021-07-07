package com.yourtion.ncn.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Yourtion
 */
public class NginxUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(NginxUtil.class);

    static String NGINX_CMD = "nginx_cmd";
    static int TIMEOUT = 10;
    private final String cmd;

    public NginxUtil(String cmd) {
        this.cmd = cmd;
    }

    /**
     * 执行 Nginx 命令
     *
     * @param args 命令文本
     * @return 执行是否成功
     */
    public boolean runCmd(String args) {
        LOGGER.debug("runCmd: {}", args);
        try {
            Process process = Runtime.getRuntime().exec(cmd + " " + args);
            if (!process.waitFor(TIMEOUT, TimeUnit.SECONDS)) {
                LOGGER.error("nginx timeout , execute [{}] to get detail ", (cmd + " -t"));
                return false;
            }
            if (process.exitValue() != 0) {
                LOGGER.error("nginx syntax incorrect , execute [{}] to get detail ", (cmd + " " + args));
                return false;
            }
            return true;
        } catch (IOException | InterruptedException e) {
            throw new IllegalArgumentException(NGINX_CMD + " is incorrect");
        }
    }

    /**
     * 检查 Nginx 命令与位置
     */
    public boolean checkNginxOk() {
        LOGGER.debug("checkNginxOk");
        try {
            Process process = Runtime.getRuntime().exec(cmd + " -V");
            if (process.waitFor() != 0) {
                throw new IllegalArgumentException(NGINX_CMD + " is incorrect");
            }
            return this.testConfig();
        } catch (IOException | InterruptedException e) {
            throw new IllegalArgumentException(NGINX_CMD + " is incorrect");
        }
    }

    /**
     * 检查 Nginx 配置信息
     *
     * @return 检查结果
     */
    public boolean testConfig() {
        return this.runCmd("-t");
    }

    /**
     * 重载 Nginx 配置
     *
     * @return 重载结果
     */
    public boolean reload() {
        return this.runCmd("-s reload");
    }

}
