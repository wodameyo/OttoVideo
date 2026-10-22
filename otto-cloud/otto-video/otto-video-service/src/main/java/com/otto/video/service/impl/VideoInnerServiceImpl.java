package com.otto.video.service.impl;

import com.otto.common.constants.Constants;
import com.otto.common.entity.enums.PageSize;
import com.otto.common.entity.query.SimplePage;
import com.otto.common.entity.vo.PaginationResultVO;
import com.otto.common.exception.BusinessException;
import com.otto.common.redis.RedisUtils;
import com.otto.common.utils.StringTools;
import com.otto.video.api.dto.VideoAuditItemDTO;
import com.otto.video.api.dto.VideoAuditParamDTO;
import com.otto.video.api.dto.VideoBaseInfoDTO;
import com.otto.video.api.dto.VideoFileDTO;
import com.otto.video.entity.enums.VideoStatusEnum;
import com.otto.video.entity.po.VideoAuditLog;
import com.otto.video.entity.po.VideoFile;
import com.otto.video.entity.po.VideoInfo;
import com.otto.video.entity.query.VideoFileQuery;
import com.otto.video.entity.query.VideoInfoQuery;
import com.otto.video.mappers.VideoAuditLogMapper;
import com.otto.video.mappers.VideoFileMapper;
import com.otto.video.mappers.VideoInfoMapper;
import com.otto.video.service.VideoInnerService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class VideoInnerServiceImpl implements VideoInnerService {

    private static final int MAX_KEYWORD_LENGTH = 64;

    @Resource
    private VideoInfoMapper videoInfoMapper;

    @Resource
    private VideoFileMapper videoFileMapper;

    @Resource
    private VideoAuditLogMapper videoAuditLogMapper;

    @Resource
    private RedisUtils redisUtils;

    @Override
    public VideoBaseInfoDTO getBaseInfo(String videoId) {
        if (StringTools.isEmpty(videoId)) {
            return null;
        }
        VideoInfo video = selectVideo(videoId);
        return video == null ? null : toBaseInfo(video);
    }

    @Override
    public List<VideoBaseInfoDTO> listBaseInfo(List<String> videoIds) {
        List<VideoBaseInfoDTO> result = new ArrayList<>();
        if (videoIds == null || videoIds.isEmpty()) {
            return result;
        }
        for (VideoInfo video : videoInfoMapper.selectListByVideoIds(videoIds)) {
            result.add(toBaseInfo(video));
        }
        return result;
    }

    @Override
    public VideoFileDTO getFile(String fileId) {
        if (StringTools.isEmpty(fileId)) {
            return null;
        }
        VideoFileQuery query = new VideoFileQuery();
        query.setFileId(fileId);
        List<VideoFile> list = videoFileMapper.selectList(query);
        return list.isEmpty() ? null : toFileDTO(list.get(0));
    }

    @Override
    public PaginationResultVO<VideoAuditItemDTO> auditPage(Integer pageNo, Integer pageSize,
                                                           Integer status, String keyword) {
        VideoInfoQuery query = new VideoInfoQuery();
        if (status == null) {
            query.setStatusList(List.of(VideoStatusEnum.PENDING_AUDIT.getStatus(),
                    VideoStatusEnum.PUBLISHED.getStatus(),
                    VideoStatusEnum.REJECTED.getStatus()));
        } else {
            query.setStatus(status);
        }
        if (StringTools.isNotEmpty(keyword)) {
            String value = keyword.trim();
            if (value.length() > MAX_KEYWORD_LENGTH) {
                throw new BusinessException("搜索关键字过长");
            }
            query.setKeyword(value);
        }
        int currentPage = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int currentSize = pageSize == null || pageSize <= 0 ? PageSize.SIZE20.getSize() : pageSize;
        int totalCount = videoInfoMapper.selectCount(query);
        query.setSimplePage(new SimplePage(currentPage, totalCount, currentSize));
        query.setOrderBy(VideoStatusEnum.PENDING_AUDIT.getStatus().equals(status)
                ? "create_time asc" : "create_time desc");
        List<VideoInfo> list = videoInfoMapper.selectList(query);
        List<VideoAuditItemDTO> resultList = new ArrayList<>();
        for (VideoInfo video : list) {
            resultList.add(toAuditItem(video));
        }
        return new PaginationResultVO<>(totalCount, currentSize, currentPage, resultList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(VideoAuditParamDTO param) {
        if (param == null || StringTools.isEmpty(param.getVideoId())) {
            throw new BusinessException("稿件ID不能为空");
        }
        Integer targetStatus = param.getStatus();
        if (!VideoStatusEnum.PUBLISHED.getStatus().equals(targetStatus)
                && !VideoStatusEnum.REJECTED.getStatus().equals(targetStatus)) {
            throw new BusinessException("审核结果不合法");
        }
        if (StringTools.isEmpty(param.getAuditorId())) {
            throw new BusinessException("审核人不能为空");
        }
        if (VideoStatusEnum.REJECTED.getStatus().equals(targetStatus) && StringTools.isEmpty(param.getOpinion())) {
            throw new BusinessException("驳回时必须填写审核意见");
        }
        VideoInfo video = selectVideo(param.getVideoId());
        if (video == null) {
            throw new BusinessException("稿件不存在");
        }
        if (!VideoStatusEnum.PENDING_AUDIT.getStatus().equals(video.getStatus())) {
            throw new BusinessException("当前状态不可审核");
        }

        VideoInfo update = new VideoInfo();
        update.setStatus(targetStatus);
        if (VideoStatusEnum.PUBLISHED.getStatus().equals(targetStatus)) {
            update.setPublishTime(new Date());
            update.setRejectReason("");
        } else {
            update.setRejectReason(param.getOpinion().trim());
        }
        VideoInfoQuery query = new VideoInfoQuery();
        query.setVideoId(param.getVideoId());
        videoInfoMapper.updateByParam(update, query);

        VideoAuditLog auditLog = new VideoAuditLog();
        auditLog.setVideoId(param.getVideoId());
        auditLog.setAuditorId(param.getAuditorId());
        auditLog.setStatus(targetStatus);
        auditLog.setOpinion(param.getOpinion());
        auditLog.setCreateTime(new Date());
        videoAuditLogMapper.insert(auditLog);

        redisUtils.delete(Constants.REDIS_KEY_VIDEO_DETAIL + param.getVideoId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offline(String videoId) {
        if (StringTools.isEmpty(videoId)) {
            throw new BusinessException("稿件ID不能为空");
        }
        VideoInfo video = selectVideo(videoId);
        if (video == null) {
            throw new BusinessException("稿件不存在");
        }
        if (VideoStatusEnum.OFFLINE.getStatus().equals(video.getStatus())) {
            return;
        }
        if (!VideoStatusEnum.PUBLISHED.getStatus().equals(video.getStatus())) {
            throw new BusinessException("仅已发布稿件可下架");
        }
        VideoInfo update = new VideoInfo();
        update.setStatus(VideoStatusEnum.OFFLINE.getStatus());
        VideoInfoQuery query = new VideoInfoQuery();
        query.setVideoId(videoId);
        videoInfoMapper.updateByParam(update, query);
        redisUtils.delete(Constants.REDIS_KEY_VIDEO_DETAIL + videoId);
    }

    private VideoInfo selectVideo(String videoId) {
        VideoInfoQuery query = new VideoInfoQuery();
        query.setVideoId(videoId);
        List<VideoInfo> list = videoInfoMapper.selectList(query);
        return list.isEmpty() ? null : list.get(0);
    }

    private VideoBaseInfoDTO toBaseInfo(VideoInfo video) {
        VideoBaseInfoDTO dto = new VideoBaseInfoDTO();
        dto.setVideoId(video.getVideoId());
        dto.setUserId(video.getUserId());
        dto.setPartitionId(video.getPartitionId());
        dto.setTitle(video.getTitle());
        dto.setCover(video.getCover());
        dto.setDuration(video.getDuration());
        dto.setStatus(video.getStatus());
        dto.setTags(video.getTags());
        dto.setPublishTime(video.getPublishTime());
        dto.setCreateTime(video.getCreateTime());
        return dto;
    }

    private VideoAuditItemDTO toAuditItem(VideoInfo video) {
        VideoAuditItemDTO dto = new VideoAuditItemDTO();
        dto.setVideoId(video.getVideoId());
        dto.setUserId(video.getUserId());
        dto.setPartitionId(video.getPartitionId());
        dto.setTitle(video.getTitle());
        dto.setCover(video.getCover());
        dto.setIntroduction(video.getIntroduction());
        dto.setDuration(video.getDuration());
        dto.setStatus(video.getStatus());
        dto.setRejectReason(video.getRejectReason());
        dto.setCreateTime(video.getCreateTime());
        dto.setPublishTime(video.getPublishTime());
        return dto;
    }

    private VideoFileDTO toFileDTO(VideoFile file) {
        VideoFileDTO dto = new VideoFileDTO();
        dto.setFileId(file.getFileId());
        dto.setVideoId(file.getVideoId());
        dto.setFileIndex(file.getFileIndex());
        dto.setTitle(file.getTitle());
        dto.setDuration(file.getDuration());
        dto.setFileSize(file.getFileSize());
        dto.setResolution(file.getResolution());
        dto.setOriginPath(file.getOriginPath());
        dto.setHlsPath(file.getHlsPath());
        dto.setCoverPath(file.getCoverPath());
        dto.setStatus(file.getStatus());
        return dto;
    }
}
