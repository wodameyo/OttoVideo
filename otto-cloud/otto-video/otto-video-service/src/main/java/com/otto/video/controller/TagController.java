package com.otto.video.controller;

import com.otto.common.controller.ABaseController;
import com.otto.common.entity.vo.ResponseVO;
import com.otto.video.entity.po.VideoTag;
import com.otto.video.service.VideoTagService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tag")
public class TagController extends ABaseController {

    @Resource
    private VideoTagService videoTagService;

    /**
     * 热门标签
     */
    @GetMapping("/hot")
    public ResponseVO<List<VideoTag>> hot(@RequestParam(value = "limit", required = false) Integer limit) {
        return getSuccessResponseVO(videoTagService.getHotTags(limit));
    }
}
