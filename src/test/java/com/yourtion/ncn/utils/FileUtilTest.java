package com.yourtion.ncn.utils;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FileUtilTest {

    private static final String TEST_FILE = "/tmp/ncn.conf";
    private static final String TEST_INFO_ORG = "TEST_ORG";
    private static final String TEST_INFO_RET = "TEST_RET";

    @BeforeAll
    static void setUp() throws IOException {
        Files.write(Paths.get(TEST_FILE), TEST_INFO_ORG.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @Order(1)
    void backupAndWrite() throws IOException {
        boolean ret = FileUtil.backupAndWrite(TEST_FILE, TEST_INFO_RET);
        Assertions.assertTrue(ret);
        String ret2 = new String(Files.readAllBytes(Paths.get(TEST_FILE)));
        Assertions.assertEquals(TEST_INFO_RET, ret2);
    }

    @Test
    @Order(2)
    void rollbackOk() throws IOException {
        boolean ret = FileUtil.rollback(TEST_FILE);
        Assertions.assertTrue(ret);
        String ret2 = new String(Files.readAllBytes(Paths.get(TEST_FILE)));
        Assertions.assertEquals(TEST_INFO_ORG, ret2);
    }

    @Test
    @Order(3)
    void rollbackNoBackupFile() {
        boolean ret = FileUtil.rollback(TEST_FILE);
        Assertions.assertFalse(ret);
    }
}