package com.otto.media.service;

import com.otto.media.entity.dto.MediaInfoDTO;

import java.nio.file.Path;

/**
 * FFmpeg 命令封装
 */
public interface FfmpegService {

    /**
     * 探测分辨率与时长
     */
    MediaInfoDTO probe(Path input);

    /**
     * 转码为 HLS（单清晰度）
     *
     * @param targetHeight  目标高度，null 表示保持原分辨率
     * @param videoBitrate  视频码率，如 2200k
     * @param audioBitrate  音频码率，如 128k
     * @param logFile       命令输出日志
     */
    void transcodeHls(Path input, Path outputDir, Integer targetHeight,
                      String videoBitrate, String audioBitrate, Path logFile);

    /**
     * 截取封面
     *
     * @param seekSecond 取帧位置（秒）
     */
    void snapshot(Path input, Path target, int seekSecond);
}
