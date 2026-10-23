package com.otto.media.controller;

import com.otto.common.controller.ABaseController;
import com.otto.common.entity.dto.TokenUserInfoDTO;
import com.otto.common.entity.vo.ResponseVO;
import com.otto.media.annotation.GlobalInterceptor;
import com.otto.media.entity.dto.UploadInitDTO;
import com.otto.media.entity.vo.UploadCompleteVO;
import com.otto.media.entity.vo.UploadInitVO;
import com.otto.media.entity.vo.UploadProgressVO;
import com.otto.media.service.UploadService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 分片上传（网关前缀 /api/media）
 */
@RestController
@RequestMapping("/upload")
public class UploadController extends ABaseController {

    @Resource
    private UploadService uploadService;

    /**
     * 初始化上传：返回 uploadId 与已上传分片，命中秒传则 instant=true
     */
    @PostMapping("/init")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO<UploadInitVO> init(@RequestBody UploadInitDTO dto) {
        TokenUserInfoDTO user = getTokenUserInfo();
        return getSuccessResponseVO(uploadService.init(dto, user.getUserId()));
    }

    /**
     * 上传单个分片
     */
    @PostMapping("/chunk")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO<UploadProgressVO> chunk(@RequestParam("uploadId") String uploadId,
                                              @RequestParam("index") Integer index,
                                              @RequestPart("file") MultipartFile file) {
        TokenUserInfoDTO user = getTokenUserInfo();
        return getSuccessResponseVO(uploadService.uploadChunk(uploadId, index, file, user.getUserId()));
    }

    /**
     * 合并分片并投递转码
     */
    @PostMapping("/complete")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO<UploadCompleteVO> complete(@RequestParam("uploadId") String uploadId) {
        TokenUserInfoDTO user = getTokenUserInfo();
        return getSuccessResponseVO(uploadService.complete(uploadId, user.getUserId()));
    }

    /**
     * 查询上传进度（断点续传）
     */
    @GetMapping("/status")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO<UploadProgressVO> status(@RequestParam("uploadId") String uploadId) {
        TokenUserInfoDTO user = getTokenUserInfo();
        return getSuccessResponseVO(uploadService.status(uploadId, user.getUserId()));
    }

    /**
     * 取消上传
     */
    @PostMapping("/cancel")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO<Object> cancel(@RequestParam("uploadId") String uploadId) {
        TokenUserInfoDTO user = getTokenUserInfo();
        uploadService.cancel(uploadId, user.getUserId());
        return getSuccessResponseVO(null);
    }
}
