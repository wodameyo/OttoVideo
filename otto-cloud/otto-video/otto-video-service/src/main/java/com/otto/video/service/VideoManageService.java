package com.otto.video.service;

import com.otto.common.entity.vo.PaginationResultVO;
import com.otto.video.entity.dto.VideoCreateDTO;
import com.otto.video.entity.dto.VideoEditDTO;
import com.otto.video.entity.query.VideoInfoQuery;
import com.otto.video.entity.vo.MyVideoVO;
import com.otto.video.entity.vo.VideoCreateResultVO;
import com.otto.video.entity.vo.VideoDetailVO;

/**
 * 投稿管理（创建/编辑/删除/我的稿件）
 */
public interface VideoManageService {

    /**
     * 创建投稿草稿，返回 videoId 与各分P的 fileId
     */
    VideoCreateResultVO create(VideoCreateDTO dto, String userId);

    /**
     * 编辑稿件（仅转码中/转码失败/驳回状态）
     */
    void edit(VideoEditDTO dto, String userId);

    /**
     * 删除稿件（仅转码中/转码失败/驳回状态）
     */
    void delete(String videoId, String userId);

    /**
     * 我的稿件分页
     */
    PaginationResultVO<MyVideoVO> myList(VideoInfoQuery query, String userId);

    /**
     * 我的稿件详情（含分P、标签、合集）
     */
    VideoDetailVO myDetail(String videoId, String userId);
}
