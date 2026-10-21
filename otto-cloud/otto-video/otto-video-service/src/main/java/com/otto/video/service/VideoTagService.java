package com.otto.video.service;

import com.otto.video.entity.po.VideoTag;

import java.util.List;

/**
 * 视频标签
 */
public interface VideoTagService {

    /**
     * 规范化标签：逗号拆分、去空格、去重、限制数量与长度
     */
    List<String> normalizeTagNames(List<String> tagNames);

    /**
     * 保存稿件标签：写关联、维护标签引用次数
     */
    void saveVideoTags(String videoId, List<String> tagNames);

    /**
     * 移除稿件全部标签并回退引用次数
     */
    void removeVideoTags(String videoId);

    /**
     * 查询稿件标签名
     */
    List<String> getTagNamesByVideoId(String videoId);

    /**
     * 热门标签
     */
    List<VideoTag> getHotTags(Integer limit);
}
