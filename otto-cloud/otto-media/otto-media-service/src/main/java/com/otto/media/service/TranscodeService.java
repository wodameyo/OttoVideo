package com.otto.media.service;

import com.otto.media.mq.VideoTranscodeMessage;

/**
 * 视频转码编排：下载原片 → HLS → 封面 → 上传 → 回写结果
 */
public interface TranscodeService {

    /**
     * 处理一条转码消息（内部消化异常并回写失败结果）
     */
    void transcode(VideoTranscodeMessage message);
}
