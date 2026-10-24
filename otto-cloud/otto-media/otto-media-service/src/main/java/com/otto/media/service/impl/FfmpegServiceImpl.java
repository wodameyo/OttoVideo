package com.otto.media.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.otto.common.exception.BusinessException;
import com.otto.media.config.FfmpegProperties;
import com.otto.media.entity.dto.MediaInfoDTO;
import com.otto.media.service.FfmpegService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * FFmpeg 命令封装：ProcessBuilder 调用、超时控制、输出落日志
 */
@Service
public class FfmpegServiceImpl implements FfmpegService {

    private static final Logger logger = LoggerFactory.getLogger(FfmpegServiceImpl.class);

    @Resource
    private FfmpegProperties ffmpegProperties;

    @Override
    public MediaInfoDTO probe(Path input) {
        Path logFile = input.getParent().resolve("ffprobe.log");
        List<String> command = List.of(ffprobePath(), "-v", "error", "-select_streams", "v:0",
                "-show_entries", "stream=width,height:format=duration", "-of", "json", input.toString());
        String output = exec(command, logFile);
        JSONObject json;
        try {
            json = JSON.parseObject(output);
        } catch (Exception e) {
            throw new BusinessException("解析视频信息失败");
        }
        if (json == null || json.getJSONArray("streams") == null || json.getJSONArray("streams").isEmpty()) {
            throw new BusinessException("未检测到视频流");
        }
        JSONObject stream = json.getJSONArray("streams").getJSONObject(0);
        MediaInfoDTO info = new MediaInfoDTO();
        info.setWidth(stream.getInteger("width"));
        info.setHeight(stream.getInteger("height"));
        info.setDuration(parseDuration(json, stream));
        if (info.getWidth() == null || info.getHeight() == null
                || info.getWidth() <= 0 || info.getHeight() <= 0) {
            throw new BusinessException("视频分辨率解析失败");
        }
        logger.info("探测完成：{}x{}，时长 {} 秒", info.getWidth(), info.getHeight(), info.getDuration());
        return info;
    }

    @Override
    public void transcodeHls(Path input, Path outputDir, Integer targetHeight,
                             String videoBitrate, String audioBitrate, Path logFile) {
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            throw new BusinessException("创建 HLS 输出目录失败", e);
        }
        List<String> command = new ArrayList<>();
        command.add(ffmpegPath());
        command.addAll(List.of("-y", "-hide_banner", "-loglevel", "warning", "-i", input.toString(),
                "-c:v", "libx264", "-preset", "veryfast", "-crf", "23",
                "-maxrate", videoBitrate, "-bufsize", doubleBitrate(videoBitrate),
                "-c:a", "aac", "-b:a", audioBitrate, "-ac", "2"));
        if (targetHeight != null) {
            command.add("-vf");
            command.add("scale=-2:" + targetHeight);
        }
        command.addAll(List.of("-hls_time", "10", "-hls_playlist_type", "vod", "-hls_list_size", "0",
                "-hls_segment_filename", outputDir.resolve("segment_%05d.ts").toString(),
                outputDir.resolve("index.m3u8").toString()));
        exec(command, logFile);
        if (!Files.exists(outputDir.resolve("index.m3u8"))) {
            throw new BusinessException("转码未生成播放列表");
        }
    }

    @Override
    public void snapshot(Path input, Path target, int seekSecond) {
        List<String> command = List.of(ffmpegPath(), "-y", "-hide_banner", "-loglevel", "error",
                "-ss", String.valueOf(seekSecond), "-i", input.toString(),
                "-frames:v", "1", "-q:v", "2", target.toString());
        exec(command, target.getParent().resolve("snapshot.log"));
        if (!Files.exists(target)) {
            throw new BusinessException("封面截图生成失败");
        }
    }

    // ============ 内部方法 ============

    /**
     * 执行命令，输出重定向到日志文件，返回日志内容
     */
    private String exec(List<String> command, Path logFile) {
        try {
            Files.createDirectories(logFile.getParent());
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.to(logFile.toFile()));
            Process process = processBuilder.start();
            boolean finished = process.waitFor(ffmpegProperties.getTimeoutSeconds(), TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new BusinessException("FFmpeg 执行超时（" + ffmpegProperties.getTimeoutSeconds() + " 秒）");
            }
            String output = readLog(logFile);
            if (process.exitValue() != 0) {
                throw new BusinessException("FFmpeg 执行失败：" + tail(output));
            }
            return output;
        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("FFmpeg 执行被中断", e);
        } catch (IOException e) {
            throw new BusinessException("FFmpeg 启动失败，请检查 otto.ffmpeg.path 配置", e);
        }
    }

    private String readLog(Path logFile) {
        try {
            return Files.exists(logFile) ? Files.readString(logFile) : "";
        } catch (IOException e) {
            return "";
        }
    }

    private String tail(String output) {
        if (output == null) {
            return "";
        }
        String text = output.strip();
        int max = 500;
        return text.length() <= max ? text : text.substring(text.length() - max);
    }

    private Integer parseDuration(JSONObject json, JSONObject stream) {
        String duration = null;
        JSONObject format = json.getJSONObject("format");
        if (format != null) {
            duration = format.getString("duration");
        }
        if (duration == null || duration.isBlank()) {
            duration = stream.getString("duration");
        }
        if (duration == null || duration.isBlank()) {
            return 0;
        }
        try {
            return (int) Math.round(Double.parseDouble(duration));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String doubleBitrate(String bitrate) {
        if (bitrate == null || !bitrate.endsWith("k")) {
            return bitrate;
        }
        try {
            int value = Integer.parseInt(bitrate.substring(0, bitrate.length() - 1));
            return (value * 2) + "k";
        } catch (NumberFormatException e) {
            return bitrate;
        }
    }

    private String ffmpegPath() {
        return ffmpegProperties.getPath();
    }

    /**
     * 未显式配置 ffprobe 时，按 ffmpeg 所在目录推导
     */
    private String ffprobePath() {
        String configured = ffmpegProperties.getFfprobePath();
        if (configured != null && !configured.isBlank()) {
            return configured;
        }
        String ffmpeg = ffmpegProperties.getPath();
        int index = Math.max(ffmpeg.lastIndexOf('/'), ffmpeg.lastIndexOf('\\'));
        if (index < 0) {
            return "ffprobe";
        }
        String dir = ffmpeg.substring(0, index + 1);
        return dir + (ffmpeg.toLowerCase().endsWith(".exe") ? "ffprobe.exe" : "ffprobe");
    }
}
