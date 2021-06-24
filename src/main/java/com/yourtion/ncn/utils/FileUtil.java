package com.yourtion.ncn.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * @author Yourtion
 */
public class FileUtil {
    private static final String BACKUP_SUFFIX = ".bak";

    static public boolean backupAndWrite(String filename, String data) {
        Path org = Paths.get(filename);
        Path ret = Paths.get(filename + BACKUP_SUFFIX);
        try {
            Files.move(org, ret, REPLACE_EXISTING);
            Files.write(org, data.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException ignored) {
        }
        return false;
    }

    static public boolean rollback(String filename) {
        Path org = Paths.get(filename);
        Path ret = Paths.get(filename + BACKUP_SUFFIX);
        try {
            Files.move(ret, org, REPLACE_EXISTING);
            return true;
        } catch (IOException ignored) {
        }
        return false;
    }
}
