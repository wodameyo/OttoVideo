package com.otto.video.controller;

import com.otto.common.controller.ABaseController;
import com.otto.common.entity.vo.PaginationResultVO;
import com.otto.common.entity.vo.ResponseVO;
import com.otto.video.api.dto.VideoAuditItemDTO;
import com.otto.video.api.dto.VideoAuditParamDTO;
import com.otto.video.api.dto.VideoBaseInfoDTO;
import com.otto.video.api.dto.VideoFileDTO;
import com.otto.video.service.VideoInnerService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 服务内部接口（网关不暴露，WebMvcConfig 排除登录拦截）
 */
@RestController
@RequestMapping("/inner/video")
public class InnerVideoController extends ABaseController {

    @Resource
    private VideoInnerService videoInnerService;

    @GetMapping("/{videoId}")
    public ResponseVO<VideoBaseInfoDTO> getById(@PathVariable("videoId") String videoId) {
        return getSuccessResponseVO(videoInnerService.getBaseInfo(videoId));
    }

    @GetMapping("/list")
    public ResponseVO<List<VideoBaseInfoDTO>> listByIds(@RequestParam("videoIds") List<String> videoIds) {
        return getSuccessResponseVO(videoInnerService.listBaseInfo(videoIds));
    }

    @GetMapping("/file")
    public ResponseVO<VideoFileDTO> getFile(@RequestParam("fileId") String fileId) {
        return getSuccessResponseVO(videoInnerService.getFile(fileId));
    }

    @GetMapping("/auditPage")
    public ResponseVO<PaginationResultVO<VideoAuditItemDTO>> auditPage(
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "keyword", required = false) String keyword) {
        return getSuccessResponseVO(videoInnerService.auditPage(pageNo, pageSize, status, keyword));
    }

    @PostMapping("/audit")
    public ResponseVO<Object> audit(@RequestBody VideoAuditParamDTO param) {
        videoInnerService.audit(param);
        return getSuccessResponseVO(null);
    }

    @PostMapping("/offline")
    public ResponseVO<Object> offline(@RequestParam("videoId") String videoId) {
        videoInnerService.offline(videoId);
        return getSuccessResponseVO(null);
    }
}
