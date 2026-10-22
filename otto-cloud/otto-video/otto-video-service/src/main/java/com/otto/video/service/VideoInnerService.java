package com.otto.video.service;

import com.otto.common.entity.vo.PaginationResultVO;
import com.otto.video.api.dto.VideoAuditItemDTO;
import com.otto.video.api.dto.VideoAuditParamDTO;
import com.otto.video.api.dto.VideoBaseInfoDTO;
import com.otto.video.api.dto.VideoFileDTO;

import java.util.List;

/**
 * 服务内部接口：跨服务查询、审核、下架（仅供 /inner/** 使用）
 */
public interface VideoInnerService {

    VideoBaseInfoDTO getBaseInfo(String videoId);

    List<VideoBaseInfoDTO> listBaseInfo(List<String> videoIds);

    /**
     * 上传前校验 fileId 归属
     */
    VideoFileDTO getFile(String fileId);

    PaginationResultVO<VideoAuditItemDTO> auditPage(Integer pageNo, Integer pageSize, Integer status, String keyword);

    void audit(VideoAuditParamDTO param);

    void offline(String videoId);
}
