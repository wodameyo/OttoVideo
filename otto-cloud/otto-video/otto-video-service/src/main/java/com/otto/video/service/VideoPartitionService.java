package com.otto.video.service;

import com.otto.video.entity.po.VideoPartition;
import com.otto.video.entity.vo.PartitionTreeVO;

import java.util.List;

/**
 * 视频分区
 */
public interface VideoPartitionService {

    /**
     * 两级分区树（Redis 缓存 1 小时）
     */
    List<PartitionTreeVO> getPartitionTree();

    /**
     * 指定父分区下的子分区；parentId=0 返回一级分区
     */
    List<PartitionTreeVO> getChildren(Integer parentId);

    /**
     * 按 ID 查询分区（投稿校验用）
     */
    VideoPartition getById(Integer partitionId);

    /**
     * 清除分区树缓存
     */
    void clearCache();
}
