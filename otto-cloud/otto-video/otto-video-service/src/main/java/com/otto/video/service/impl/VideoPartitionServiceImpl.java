package com.otto.video.service.impl;

import com.otto.common.constants.Constants;
import com.otto.common.redis.RedisUtils;
import com.otto.video.entity.po.VideoPartition;
import com.otto.video.entity.query.VideoPartitionQuery;
import com.otto.video.entity.vo.PartitionTreeVO;
import com.otto.video.mappers.VideoPartitionMapper;
import com.otto.video.service.VideoPartitionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class VideoPartitionServiceImpl implements VideoPartitionService {

    private static final int STATUS_ENABLED = 1;
    private static final int ROOT_PARENT_ID = 0;

    @Resource
    private VideoPartitionMapper videoPartitionMapper;

    @Resource
    private RedisUtils redisUtils;

    @Override
    public List<PartitionTreeVO> getPartitionTree() {
        List<PartitionTreeVO> cached = redisUtils.getList(Constants.REDIS_KEY_PARTITION_TREE, PartitionTreeVO.class);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        VideoPartitionQuery query = new VideoPartitionQuery();
        query.setStatus(STATUS_ENABLED);
        query.setOrderBy("sort asc, partition_id asc");
        List<VideoPartition> partitions = videoPartitionMapper.selectList(query);
        List<PartitionTreeVO> tree = buildTree(partitions);
        redisUtils.setex(Constants.REDIS_KEY_PARTITION_TREE, tree, Constants.REDIS_KEY_EXPIRES_ONE_HOUR);
        return tree;
    }

    @Override
    public List<PartitionTreeVO> getChildren(Integer parentId) {
        int pid = parentId == null ? ROOT_PARENT_ID : parentId;
        List<PartitionTreeVO> tree = getPartitionTree();
        if (pid == ROOT_PARENT_ID) {
            return tree;
        }
        PartitionTreeVO node = findNode(tree, pid);
        if (node == null || node.getChildren() == null) {
            return Collections.emptyList();
        }
        return node.getChildren();
    }

    @Override
    public VideoPartition getById(Integer partitionId) {
        if (partitionId == null) {
            return null;
        }
        VideoPartitionQuery query = new VideoPartitionQuery();
        query.setPartitionId(partitionId);
        List<VideoPartition> list = videoPartitionMapper.selectList(query);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public void clearCache() {
        redisUtils.delete(Constants.REDIS_KEY_PARTITION_TREE);
    }

    private List<PartitionTreeVO> buildTree(List<VideoPartition> partitions) {
        Map<Integer, PartitionTreeVO> nodeMap = new LinkedHashMap<>();
        for (VideoPartition partition : partitions) {
            PartitionTreeVO node = new PartitionTreeVO();
            node.setPartitionId(partition.getPartitionId());
            node.setParentId(partition.getParentId());
            node.setName(partition.getName());
            node.setCode(partition.getCode());
            node.setIcon(partition.getIcon());
            node.setSort(partition.getSort());
            node.setChildren(new ArrayList<>());
            nodeMap.put(partition.getPartitionId(), node);
        }
        List<PartitionTreeVO> roots = new ArrayList<>();
        for (PartitionTreeVO node : nodeMap.values()) {
            Integer parentId = node.getParentId();
            if (parentId == null || parentId == ROOT_PARENT_ID || !nodeMap.containsKey(parentId)) {
                roots.add(node);
            } else {
                nodeMap.get(parentId).getChildren().add(node);
            }
        }
        return roots;
    }

    private PartitionTreeVO findNode(List<PartitionTreeVO> nodes, Integer partitionId) {
        for (PartitionTreeVO node : nodes) {
            if (partitionId.equals(node.getPartitionId())) {
                return node;
            }
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                PartitionTreeVO found = findNode(node.getChildren(), partitionId);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
