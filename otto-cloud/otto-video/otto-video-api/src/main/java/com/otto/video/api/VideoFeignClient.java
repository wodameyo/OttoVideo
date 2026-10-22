package com.otto.video.api;

import com.otto.common.entity.vo.PaginationResultVO;
import com.otto.common.entity.vo.ResponseVO;
import com.otto.video.api.dto.VideoAuditItemDTO;
import com.otto.video.api.dto.VideoAuditParamDTO;
import com.otto.video.api.dto.VideoBaseInfoDTO;
import com.otto.video.api.dto.VideoFileDTO;
import com.otto.video.api.fallback.VideoClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 视频中心内部 Feign 接口（path=/inner/video，不经过网关）
 */
@FeignClient(name = "otto-video-service", contextId = "videoClient", path = "/inner/video",
        fallbackFactory = VideoClientFallbackFactory.class)
public interface VideoFeignClient {

    /**
     * 单个稿件基础信息
     */
    @GetMapping("/{videoId}")
    ResponseVO<VideoBaseInfoDTO> getById(@PathVariable("videoId") String videoId);

    /**
     * 批量稿件基础信息（避免 N+1）
     */
    @GetMapping("/list")
    ResponseVO<List<VideoBaseInfoDTO>> listByIds(@RequestParam("videoIds") List<String> videoIds);

    /**
     * 按 fileId 查询分P信息（media 上传前校验归属）
     */
    @GetMapping("/file")
    ResponseVO<VideoFileDTO> getFile(@RequestParam("fileId") String fileId);

    /**
     * 审核分页（管理后台）
     */
    @GetMapping("/auditPage")
    ResponseVO<PaginationResultVO<VideoAuditItemDTO>> auditPage(@RequestParam("pageNo") Integer pageNo,
                                                                @RequestParam("pageSize") Integer pageSize,
                                                                @RequestParam(value = "status", required = false) Integer status,
                                                                @RequestParam(value = "keyword", required = false) String keyword);

    /**
     * 审核通过 / 驳回
     */
    @PostMapping("/audit")
    ResponseVO<Object> audit(@RequestBody VideoAuditParamDTO param);

    /**
     * 下架已发布稿件（管理后台）
     */
    @PostMapping("/offline")
    ResponseVO<Object> offline(@RequestParam("videoId") String videoId);
}
