package com.yourtion.ncn.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * 配置工具
 * <p> env 环境变量 > 系统变量 > jar 包外 application.properties > jar 包内的 application.properties </p>
 *
 * @author Yourtion
 */
public class ConfUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfUtil.class);

    private static final Properties CONF = new Properties();

    public static Properties loadConfig() {
        CONF.clear();
        LOGGER.debug("开始加载配置文件");
        long start = System.currentTimeMillis();
        loadConf("application.properties");
        checkSystemProperties();
        checkSystemEnv();
        long cost = System.currentTimeMillis() - start;
        LOGGER.debug("配置文件加载完毕，耗时" + cost + " 毫秒，配置项数目：" + CONF.size());
        return CONF;
    }

    public static void set(String key, String value) {
        CONF.put(key, value);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = CONF.get(key) == null ? Boolean.valueOf(defaultValue).toString() : CONF.getProperty(key);
        LOGGER.debug("获取配置项：" + key + "=" + value);
        return value.contains("true") || value.contains("1") || value.contains("yes");
    }

    public static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public static int getInt(String key, int defaultValue) {
        int value = CONF.get(key) == null ? defaultValue : Integer.parseInt(CONF.getProperty(key).trim());
        LOGGER.debug("获取配置项：" + key + "=" + value);
        return value;
    }

    public static int getInt(String key) {
        return getInt(key, -1);
    }

    public static long getLong(String key, long defaultValue) {
        long value = CONF.get(key) == null ? defaultValue : Long.parseLong(CONF.getProperty(key).trim());
        LOGGER.debug("获取配置项：" + key + "=" + value);
        return value;
    }

    public static long getLong(String key) {
        return getLong(key, -1L);
    }

    public static String get(String key, String defaultValue) {
        return CONF.getProperty(key, defaultValue);
    }

    public static String get(String key) {
        String value = CONF.getProperty(key);
        LOGGER.debug("获取配置项：" + key + "=" + value);
        return value;
    }

    /**
     * 强制覆盖默认配置
     *
     * @param confFile 配置文件
     */
    public static void forceOverride(File confFile) {
        try (InputStream in = new FileInputStream(confFile)) {
            LOGGER.debug("使用配置文件 " + confFile.getAbsolutePath() + " 强制覆盖默认配置");
            loadConf(in);
        } catch (Exception ex) {
            LOGGER.warn("强制覆盖默认配置失败：" + ex);
        }
        for (Object key : CONF.keySet()) {
            LOGGER.debug(key + "=" + CONF.get(key));
        }
    }

    /**
     * 强制覆盖默认配置
     *
     * @param confFile 配置文件路径
     */
    public static void forceOverride(String confFile) {
        File file = new File(confFile);
        forceOverride(file);
    }

    /**
     * 强制覆盖默认配置
     *
     * @param content 配置文件内容
     */
    public static void addConfig(String content) {
        loadConf(new ByteArrayInputStream(content.getBytes()));
    }

    /**
     * 加载配置文件
     *
     * @param confFile 类路径下的配置文件
     */
    private static void loadConf(String confFile) {
        try {
            InputStream in;
            File file = new File(confFile);
            if (file.exists()) {
                // 读取jar包同级的application.properties文件
                in = new FileInputStream(file);
            } else {
                // 读取jar内的application.properties文件
                in = ConfUtil.class.getClassLoader().getResourceAsStream(confFile);
            }
            loadConf(in);
            LOGGER.debug("加载配置文件：" + confFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 加载配置文件
     *
     * @param in 文件输入流
     */
    private static void loadConf(InputStream in) {
        try {
            Properties prop = new Properties();
            prop.load(in);
            for (Object key : prop.keySet()) {
                Object value = prop.get(key);
                if (value != null) {
                    CONF.put(key, value);
                    LOGGER.debug("环境变量覆盖默认配置：" + key + "=" + value);
                }
            }
        } catch (IOException ex) {
            System.err.println("配置文件加载失败:" + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    /**
     * 使用环境变量覆盖配置
     */
    private static void checkSystemEnv() {
        LOGGER.debug("检查系环境变量配置");
        for (Object key : CONF.keySet()) {
            String value = System.getenv(key.toString());
            if (value != null) {
                CONF.put(key, value);
                LOGGER.debug("环境变量覆盖默认配置：" + key + "=" + value);
            }
        }
    }

    /**
     * 使用系统属性覆盖配置文件
     */
    private static void checkSystemProperties() {
        LOGGER.debug("检查系统属性配置");
        for (Object key : CONF.keySet()) {
            String value = System.getProperty(key.toString());
            if (value != null) {
                CONF.put(key, value);
                LOGGER.debug("系统属性覆盖默认配置：" + key + "=" + value);
            }
        }
    }
}

