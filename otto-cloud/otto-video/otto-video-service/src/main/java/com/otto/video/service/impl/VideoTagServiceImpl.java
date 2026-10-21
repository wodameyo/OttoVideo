package com.otto.video.service.impl;

import com.otto.common.exception.BusinessException;
import com.otto.video.entity.po.VideoTag;
import com.otto.video.entity.po.VideoTagRef;
import com.otto.video.mappers.VideoTagMapper;
import com.otto.video.mappers.VideoTagRefMapper;
import com.otto.video.service.VideoTagService;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class VideoTagServiceImpl implements VideoTagService {

    private static final int MAX_TAG_COUNT = 10;
    private static final int MAX_TAG_LENGTH = 20;
    private static final int DEFAULT_HOT_LIMIT = 20;
    private static final int MAX_HOT_LIMIT = 50;

    @Resource
    private VideoTagMapper videoTagMapper;

    @Resource
    private VideoTagRefMapper videoTagRefMapper;

    @Override
    public List<String> normalizeTagNames(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new ArrayList<>();
        }
        Set<String> result = new LinkedHashSet<>();
        for (String raw : tagNames) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            // 兼容前端传单个逗号分隔字符串
            for (String part : raw.split("[,，]")) {
                String name = part.trim();
                if (name.isEmpty()) {
                    continue;
                }
                if (name.length() > MAX_TAG_LENGTH) {
                    throw new BusinessException("单个标签长度不能超过" + MAX_TAG_LENGTH + "个字符");
                }
                result.add(name);
            }
        }
        if (result.size() > MAX_TAG_COUNT) {
            throw new BusinessException("最多添加" + MAX_TAG_COUNT + "个标签");
        }
        return new ArrayList<>(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveVideoTags(String videoId, List<String> tagNames) {
        List<String> names = normalizeTagNames(tagNames);
        if (names.isEmpty()) {
            return;
        }
        Set<Long> boundTagIds = new HashSet<>(videoTagRefMapper.selectTagIdListByVideoId(videoId));
        Map<String, VideoTag> tagMap = new HashMap<>();
        for (VideoTag tag : videoTagMapper.selectListByNames(names)) {
            tagMap.put(tag.getTagName(), tag);
        }
        for (String name : names) {
            VideoTag tag = resolveTag(name, tagMap);
            if (!boundTagIds.add(tag.getTagId())) {
                // 该稿件已关联此标签
                continue;
            }
            VideoTagRef ref = new VideoTagRef();
            ref.setVideoId(videoId);
            ref.setTagId(tag.getTagId());
            videoTagRefMapper.insert(ref);
            videoTagMapper.updateUseCount(tag.getTagId(), 1);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeVideoTags(String videoId) {
        List<Long> tagIds = videoTagRefMapper.selectTagIdListByVideoId(videoId);
        if (tagIds.isEmpty()) {
            return;
        }
        videoTagRefMapper.deleteByVideoId(videoId);
        for (Long tagId : tagIds) {
            videoTagMapper.updateUseCount(tagId, -1);
        }
    }

    @Override
    public List<String> getTagNamesByVideoId(String videoId) {
        List<Long> tagIds = videoTagRefMapper.selectTagIdListByVideoId(videoId);
        if (tagIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> names = new ArrayList<>();
        for (VideoTag tag : videoTagMapper.selectListByTagIds(tagIds)) {
            names.add(tag.getTagName());
        }
        return names;
    }

    @Override
    public List<VideoTag> getHotTags(Integer limit) {
        int size = (limit == null || limit <= 0) ? DEFAULT_HOT_LIMIT : Math.min(limit, MAX_HOT_LIMIT);
        return videoTagMapper.selectHotTags(size);
    }

    /**
     * 获取或创建标签；并发创建冲突时回读已存在记录
     */
    private VideoTag resolveTag(String name, Map<String, VideoTag> tagMap) {
        VideoTag tag = tagMap.get(name);
        if (tag != null) {
            return tag;
        }
        VideoTag newTag = new VideoTag();
        newTag.setTagName(name);
        newTag.setUseCount(0);
        try {
            videoTagMapper.insert(newTag);
            tag = newTag;
        } catch (DuplicateKeyException e) {
            List<VideoTag> exist = videoTagMapper.selectListByNames(List.of(name));
            if (exist.isEmpty()) {
                throw e;
            }
            tag = exist.get(0);
        }
        tagMap.put(name, tag);
        return tag;
    }
}
