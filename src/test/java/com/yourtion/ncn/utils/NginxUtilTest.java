package com.yourtion.ncn.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NginxUtilTest {

    private static final NginxUtil nginx = new NginxUtil(System.getenv("NginxUtilTestCMD"));

    @Test
    void checkNginxOk() {
        boolean ok = nginx.checkNginxOk();
        Assertions.assertTrue(ok);
    }

    @Test
    void testConfig() {
        boolean ok = nginx.testConfig();
        Assertions.assertTrue(ok);
    }

    @Test
    void reload() {
        boolean ok = nginx.reload();
        Assertions.assertTrue(ok);
    }
}