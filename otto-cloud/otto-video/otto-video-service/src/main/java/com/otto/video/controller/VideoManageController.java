package com.otto.video.controller;

import com.otto.common.controller.ABaseController;
import com.otto.common.entity.dto.TokenUserInfoDTO;
import com.otto.common.entity.vo.PaginationResultVO;
import com.otto.common.entity.vo.ResponseVO;
import com.otto.video.annotation.GlobalInterceptor;
import com.otto.video.entity.dto.VideoCreateDTO;
import com.otto.video.entity.dto.VideoEditDTO;
import com.otto.video.entity.query.VideoInfoQuery;
import com.otto.video.entity.vo.MyVideoVO;
import com.otto.video.entity.vo.VideoCreateResultVO;
import com.otto.video.entity.vo.VideoDetailVO;
import com.otto.video.service.VideoManageService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 投稿管理：创建、编辑、删除、我的稿件
 */
@RestController
@RequestMapping("/video")
public class VideoManageController extends ABaseController {

    @Resource
    private VideoManageService videoManageService;

    @PostMapping("/create")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO<VideoCreateResultVO> create(@RequestBody VideoCreateDTO dto) {
        TokenUserInfoDTO user = getTokenUserInfo();
        return getSuccessResponseVO(videoManageService.create(dto, user.getUserId()));
    }

    @PostMapping("/edit")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO<Object> edit(@RequestBody VideoEditDTO dto) {
        TokenUserInfoDTO user = getTokenUserInfo();
        videoManageService.edit(dto, user.getUserId());
        return getSuccessResponseVO(null);
    }

    @PostMapping("/delete")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO<Object> delete(@RequestParam("videoId") String videoId) {
        TokenUserInfoDTO user = getTokenUserInfo();
        videoManageService.delete(videoId, user.getUserId());
        return getSuccessResponseVO(null);
    }

    @GetMapping("/myList")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO<PaginationResultVO<MyVideoVO>> myList(VideoInfoQuery query) {
        TokenUserInfoDTO user = getTokenUserInfo();
        return getSuccessResponseVO(videoManageService.myList(query, user.getUserId()));
    }

    @GetMapping("/myDetail")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO<VideoDetailVO> myDetail(@RequestParam("videoId") String videoId) {
        TokenUserInfoDTO user = getTokenUserInfo();
        return getSuccessResponseVO(videoManageService.myDetail(videoId, user.getUserId()));
    }
}
