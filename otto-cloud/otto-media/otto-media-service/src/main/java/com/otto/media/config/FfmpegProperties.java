package com.otto.media.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * FFmpeg 配置（otto.ffmpeg.*）
 */
@Data
@Component
@ConfigurationProperties(prefix = "otto.ffmpeg")
public class FfmpegProperties {

    /**
     * ffmpeg 可执行文件，默认从 PATH 查找
     */
    private String path = "ffmpeg";

    /**
     * ffprobe 可执行文件，留空则按 ffmpeg 所在目录推导
     */
    private String ffprobePath;

    /**
     * 单次命令超时（秒）
     */
    private Long timeoutSeconds = 3600L;
}
