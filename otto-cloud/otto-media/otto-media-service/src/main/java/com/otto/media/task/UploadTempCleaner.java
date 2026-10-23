package com.otto.media.task;

import com.otto.media.config.UploadProperties;
import com.otto.media.utils.FileTools;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * 启动时清理过期的上传临时目录
 */
@Component
public class UploadTempCleaner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(UploadTempCleaner.class);

    @Resource
    private UploadProperties uploadProperties;

    @Override
    public void run(ApplicationArguments args) {
        Path root = Paths.get(uploadProperties.getTempDir()).toAbsolutePath().normalize();
        if (!Files.isDirectory(root)) {
            return;
        }
        long expireMillis = uploadProperties.getTempExpireHours() * 3600_000L;
        long deadline = System.currentTimeMillis() - expireMillis;
        int cleaned = 0;
        try (Stream<Path> stream = Files.list(root)) {
            for (Path path : stream.filter(Files::isDirectory).toList()) {
                long lastModified = Files.getLastModifiedTime(path).toMillis();
                if (lastModified < deadline && FileTools.deleteDirectory(path)) {
                    cleaned++;
                }
            }
            if (cleaned > 0) {
                logger.info("清理过期上传临时目录 {} 个，根目录：{}", cleaned, root);
            }
        } catch (IOException e) {
            logger.warn("扫描上传临时目录失败：{}", root, e);
        }
    }
}
