package com.otto.video.controller;

import com.otto.common.controller.ABaseController;
import com.otto.common.entity.vo.ResponseVO;
import com.otto.video.entity.vo.PartitionTreeVO;
import com.otto.video.service.VideoPartitionService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/partition")
public class PartitionController extends ABaseController {

    @Resource
    private VideoPartitionService videoPartitionService;

    /**
     * 两级分区树
     */
    @GetMapping("/tree")
    public ResponseVO<List<PartitionTreeVO>> tree() {
        return getSuccessResponseVO(videoPartitionService.getPartitionTree());
    }

    /**
     * 子分区列表，parentId 不传时返回一级分区
     */
    @GetMapping("/list")
    public ResponseVO<List<PartitionTreeVO>> list(@RequestParam(value = "parentId", required = false) Integer parentId) {
        return getSuccessResponseVO(videoPartitionService.getChildren(parentId));
    }
}
