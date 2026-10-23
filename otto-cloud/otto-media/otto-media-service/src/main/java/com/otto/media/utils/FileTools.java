package com.otto.media.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * 本地文件工具
 */
public class FileTools {

    /**
     * 递归删除目录（不存在则忽略）
     */
    public static boolean deleteDirectory(Path dir) {
        if (dir == null || !Files.exists(dir)) {
            return true;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // 单个文件删除失败不阻断整体清理
                }
            });
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
