package com.otto.video.service.impl;

import com.otto.common.entity.enums.PageSize;
import com.otto.common.entity.query.SimplePage;
import com.otto.common.entity.vo.PaginationResultVO;
import com.otto.common.exception.BusinessException;
import com.otto.common.utils.StringTools;
import com.otto.video.entity.dto.VideoCreateDTO;
import com.otto.video.entity.dto.VideoEditDTO;
import com.otto.video.entity.dto.VideoFileCreateDTO;
import com.otto.video.entity.dto.VideoFileUpdateDTO;
import com.otto.video.entity.enums.VideoStatusEnum;
import com.otto.video.entity.po.VideoAuditLog;
import com.otto.video.entity.po.VideoFile;
import com.otto.video.entity.po.VideoInfo;
import com.otto.video.entity.po.VideoPartition;
import com.otto.video.entity.po.VideoSeries;
import com.otto.video.entity.po.VideoSeriesItem;
import com.otto.video.entity.query.VideoAuditLogQuery;
import com.otto.video.entity.query.VideoFileQuery;
import com.otto.video.entity.query.VideoInfoQuery;
import com.otto.video.entity.query.VideoSeriesItemQuery;
import com.otto.video.entity.vo.MyVideoVO;
import com.otto.video.entity.vo.VideoCreateFileVO;
import com.otto.video.entity.vo.VideoCreateResultVO;
import com.otto.video.entity.vo.VideoDetailVO;
import com.otto.video.entity.vo.VideoFileVO;
import com.otto.video.mappers.VideoAuditLogMapper;
import com.otto.video.mappers.VideoFileMapper;
import com.otto.video.mappers.VideoInfoMapper;
import com.otto.video.mappers.VideoSeriesItemMapper;
import com.otto.video.mappers.VideoSeriesMapper;
import com.otto.video.service.VideoManageService;
import com.otto.video.service.VideoPartitionService;
import com.otto.video.service.VideoTagService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class VideoManageServiceImpl implements VideoManageService {

    private static final int MAX_TITLE_LENGTH = 128;
    private static final int MAX_INTRODUCTION_LENGTH = 2000;
    private static final int MAX_FILE_COUNT = 100;

    @Resource
    private VideoInfoMapper videoInfoMapper;

    @Resource
    private VideoFileMapper videoFileMapper;

    @Resource
    private VideoSeriesMapper videoSeriesMapper;

    @Resource
    private VideoSeriesItemMapper videoSeriesItemMapper;

    @Resource
    private VideoAuditLogMapper videoAuditLogMapper;

    @Resource
    private VideoPartitionService videoPartitionService;

    @Resource
    private VideoTagService videoTagService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VideoCreateResultVO create(VideoCreateDTO dto, String userId) {
        String title = validateTitle(dto.getTitle());
        validatePartition(dto.getPartitionId());
        validateIntroduction(dto.getIntroduction());
        List<VideoFileCreateDTO> fileDTOList = dto.getFiles();
        if (fileDTOList == null || fileDTOList.isEmpty()) {
            throw new BusinessException("请至少上传一个分P");
        }
        if (fileDTOList.size() > MAX_FILE_COUNT) {
            throw new BusinessException("分P数量不能超过" + MAX_FILE_COUNT + "个");
        }
        List<String> tagNames = videoTagService.normalizeTagNames(dto.getTags());

        String videoId = generateVideoId();
        VideoInfo videoInfo = new VideoInfo();
        videoInfo.setVideoId(videoId);
        videoInfo.setUserId(userId);
        videoInfo.setPartitionId(dto.getPartitionId());
        videoInfo.setTitle(title);
        videoInfo.setCover(dto.getCover());
        videoInfo.setIntroduction(dto.getIntroduction());
        videoInfo.setTags(String.join(",", tagNames));
        videoInfo.setStatus(VideoStatusEnum.TRANSCODING.getStatus());
        videoInfo.setDuration(0);
        videoInfo.setCreateTime(new Date());
        videoInfoMapper.insert(videoInfo);

        List<VideoFile> files = new ArrayList<>();
        List<VideoCreateFileVO> fileVOs = new ArrayList<>();
        for (int i = 0; i < fileDTOList.size(); i++) {
            VideoFileCreateDTO fileDTO = fileDTOList.get(i);
            int fileIndex = i + 1;
            String fileTitle = normalizeFileTitle(fileDTO.getTitle(), fileIndex);
            VideoFile file = new VideoFile();
            file.setFileId(StringTools.getUUID());
            file.setVideoId(videoId);
            file.setFileIndex(fileIndex);
            file.setTitle(fileTitle);
            file.setDuration(0);
            file.setFileSize(fileDTO.getFileSize() == null ? 0L : fileDTO.getFileSize());
            file.setResolution(fileDTO.getResolution());
            file.setStatus(0);
            file.setCreateTime(new Date());
            files.add(file);

            VideoCreateFileVO fileVO = new VideoCreateFileVO();
            fileVO.setFileId(file.getFileId());
            fileVO.setFileIndex(fileIndex);
            fileVO.setTitle(fileTitle);
            fileVOs.add(fileVO);
        }
        videoFileMapper.insertBatch(files);

        videoTagService.saveVideoTags(videoId, tagNames);
        if (StringTools.isNotEmpty(dto.getSeriesId())) {
            bindSeries(videoId, dto.getSeriesId(), userId);
        }

        VideoCreateResultVO result = new VideoCreateResultVO();
        result.setVideoId(videoId);
        result.setFiles(fileVOs);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void edit(VideoEditDTO dto, String userId) {
        if (StringTools.isEmpty(dto.getVideoId())) {
            throw new BusinessException("稿件ID不能为空");
        }
        VideoInfo video = getOwnedVideo(dto.getVideoId(), userId);
        Integer status = video.getStatus();
        if (!VideoStatusEnum.TRANSCODING.getStatus().equals(status)
                && !VideoStatusEnum.TRANSCODE_FAILED.getStatus().equals(status)
                && !VideoStatusEnum.REJECTED.getStatus().equals(status)) {
            throw new BusinessException("当前状态不可编辑");
        }

        VideoInfo update = new VideoInfo();
        if (dto.getTitle() != null) {
            update.setTitle(validateTitle(dto.getTitle()));
        }
        if (dto.getPartitionId() != null) {
            validatePartition(dto.getPartitionId());
            update.setPartitionId(dto.getPartitionId());
        }
        if (dto.getCover() != null) {
            update.setCover(dto.getCover());
        }
        if (dto.getIntroduction() != null) {
            validateIntroduction(dto.getIntroduction());
            update.setIntroduction(dto.getIntroduction());
        }
        if (dto.getTags() != null) {
            List<String> tagNames = videoTagService.normalizeTagNames(dto.getTags());
            update.setTags(String.join(",", tagNames));
            videoTagService.removeVideoTags(dto.getVideoId());
            videoTagService.saveVideoTags(dto.getVideoId(), tagNames);
        }
        if (dto.getSeriesId() != null) {
            changeSeries(dto.getVideoId(), dto.getSeriesId(), userId);
        }
        if (video.getStatus().equals(VideoStatusEnum.REJECTED.getStatus())) {
            // 修改已驳回稿件后重新进入待审核
            update.setStatus(VideoStatusEnum.PENDING_AUDIT.getStatus());
            update.setRejectReason("");
        }
        updateFileTitles(dto.getVideoId(), dto.getFiles());
        if (update.getTitle() != null || update.getPartitionId() != null || update.getCover() != null
                || update.getIntroduction() != null || update.getTags() != null || update.getStatus() != null) {
            VideoInfoQuery query = new VideoInfoQuery();
            query.setVideoId(dto.getVideoId());
            videoInfoMapper.updateByParam(update, query);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String videoId, String userId) {
        VideoInfo video = getOwnedVideo(videoId, userId);
        Integer status = video.getStatus();
        if (!VideoStatusEnum.TRANSCODING.getStatus().equals(status)
                && !VideoStatusEnum.TRANSCODE_FAILED.getStatus().equals(status)
                && !VideoStatusEnum.REJECTED.getStatus().equals(status)) {
            throw new BusinessException("仅草稿、转码失败或已驳回的稿件可删除");
        }
        videoTagService.removeVideoTags(videoId);
        unbindSeries(videoId);

        VideoFileQuery fileQuery = new VideoFileQuery();
        fileQuery.setVideoId(videoId);
        videoFileMapper.deleteByParam(fileQuery);

        VideoAuditLogQuery auditQuery = new VideoAuditLogQuery();
        auditQuery.setVideoId(videoId);
        videoAuditLogMapper.deleteByParam(auditQuery);

        VideoInfoQuery videoQuery = new VideoInfoQuery();
        videoQuery.setVideoId(videoId);
        videoInfoMapper.deleteByParam(videoQuery);
    }

    @Override
    public PaginationResultVO<MyVideoVO> myList(VideoInfoQuery query, String userId) {
        query.setUserId(userId);
        if (StringTools.isEmpty(query.getOrderBy())) {
            query.setOrderBy("create_time desc");
        }
        int pageNo = query.getPageNo() == null || query.getPageNo() < 1 ? 1 : query.getPageNo();
        int pageSize = query.getPageSize() == null || query.getPageSize() <= 0
                ? PageSize.SIZE20.getSize() : query.getPageSize();
        int totalCount = videoInfoMapper.selectCount(query);
        query.setSimplePage(new SimplePage(pageNo, totalCount, pageSize));
        List<VideoInfo> list = videoInfoMapper.selectList(query);

        Map<String, Integer> fileCountMap = new HashMap<>();
        if (!list.isEmpty()) {
            List<String> videoIds = new ArrayList<>();
            for (VideoInfo video : list) {
                videoIds.add(video.getVideoId());
            }
            for (VideoFile file : videoFileMapper.selectListByVideoIds(videoIds)) {
                fileCountMap.merge(file.getVideoId(), 1, Integer::sum);
            }
        }
        List<MyVideoVO> resultList = new ArrayList<>();
        for (VideoInfo video : list) {
            MyVideoVO vo = new MyVideoVO();
            vo.setVideoId(video.getVideoId());
            vo.setTitle(video.getTitle());
            vo.setCover(video.getCover());
            vo.setPartitionId(video.getPartitionId());
            vo.setStatus(video.getStatus());
            vo.setRejectReason(video.getRejectReason());
            vo.setDuration(video.getDuration());
            vo.setFileCount(fileCountMap.getOrDefault(video.getVideoId(), 0));
            vo.setCreateTime(video.getCreateTime());
            vo.setPublishTime(video.getPublishTime());
            resultList.add(vo);
        }
        return new PaginationResultVO<>(totalCount, pageSize, pageNo, resultList);
    }

    @Override
    public VideoDetailVO myDetail(String videoId, String userId) {
        VideoInfo video = getOwnedVideo(videoId, userId);
        VideoDetailVO vo = new VideoDetailVO();
        vo.setVideoId(video.getVideoId());
        vo.setUserId(video.getUserId());
        vo.setPartitionId(video.getPartitionId());
        VideoPartition partition = videoPartitionService.getById(video.getPartitionId());
        vo.setPartitionName(partition == null ? null : partition.getName());
        vo.setTitle(video.getTitle());
        vo.setCover(video.getCover());
        vo.setIntroduction(video.getIntroduction());
        vo.setTags(videoTagService.getTagNamesByVideoId(videoId));
        vo.setDuration(video.getDuration());
        vo.setStatus(video.getStatus());
        vo.setRejectReason(video.getRejectReason());
        vo.setCreateTime(video.getCreateTime());
        vo.setPublishTime(video.getPublishTime());

        List<VideoFileVO> fileVOs = new ArrayList<>();
        for (VideoFile file : videoFileMapper.selectListByVideoId(videoId)) {
            VideoFileVO fileVO = new VideoFileVO();
            fileVO.setFileId(file.getFileId());
            fileVO.setVideoId(file.getVideoId());
            fileVO.setFileIndex(file.getFileIndex());
            fileVO.setTitle(file.getTitle());
            fileVO.setDuration(file.getDuration());
            fileVO.setResolution(file.getResolution());
            fileVO.setHlsPath(file.getHlsPath());
            fileVO.setCoverPath(file.getCoverPath());
            fileVO.setStatus(file.getStatus());
            fileVOs.add(fileVO);
        }
        vo.setFiles(fileVOs);

        VideoSeriesItem seriesItem = findSeriesItem(videoId);
        if (seriesItem != null) {
            VideoSeries series = selectSeries(seriesItem.getSeriesId());
            if (series != null) {
                vo.setSeriesId(series.getSeriesId());
                vo.setSeriesName(series.getName());
            }
        }

        VideoAuditLog latestAudit = videoAuditLogMapper.selectLatestByVideoId(videoId);
        if (latestAudit != null) {
            vo.setAuditOpinion(latestAudit.getOpinion());
            vo.setAuditTime(latestAudit.getCreateTime());
        }
        return vo;
    }

    private VideoInfo getOwnedVideo(String videoId, String userId) {
        VideoInfo video = selectVideo(videoId);
        if (video == null) {
            throw new BusinessException("稿件不存在");
        }
        if (!video.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该稿件");
        }
        return video;
    }

    private VideoInfo selectVideo(String videoId) {
        VideoInfoQuery query = new VideoInfoQuery();
        query.setVideoId(videoId);
        List<VideoInfo> list = videoInfoMapper.selectList(query);
        return list.isEmpty() ? null : list.get(0);
    }

    private void validatePartition(Integer partitionId) {
        if (partitionId == null) {
            throw new BusinessException("请选择分区");
        }
        VideoPartition partition = videoPartitionService.getById(partitionId);
        if (partition == null || partition.getStatus() == null || partition.getStatus() != 1) {
            throw new BusinessException("分区不存在或已停用");
        }
    }

    private String validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BusinessException("标题不能为空");
        }
        String value = title.trim();
        if (value.length() > MAX_TITLE_LENGTH) {
            throw new BusinessException("标题长度不能超过" + MAX_TITLE_LENGTH + "个字符");
        }
        return value;
    }

    private String normalizeFileTitle(String title, int fileIndex) {
        if (title == null || title.isBlank()) {
            return "P" + fileIndex;
        }
        String value = title.trim();
        if (value.length() > MAX_TITLE_LENGTH) {
            throw new BusinessException("分P标题长度不能超过" + MAX_TITLE_LENGTH + "个字符");
        }
        return value;
    }

    private void validateIntroduction(String introduction) {
        if (introduction != null && introduction.length() > MAX_INTRODUCTION_LENGTH) {
            throw new BusinessException("简介长度不能超过" + MAX_INTRODUCTION_LENGTH + "个字符");
        }
    }

    private void updateFileTitles(String videoId, List<VideoFileUpdateDTO> fileUpdates) {
        if (fileUpdates == null || fileUpdates.isEmpty()) {
            return;
        }
        Set<String> ownedFileIds = new HashSet<>();
        for (VideoFile file : videoFileMapper.selectListByVideoId(videoId)) {
            ownedFileIds.add(file.getFileId());
        }
        for (VideoFileUpdateDTO fileUpdate : fileUpdates) {
            if (StringTools.isEmpty(fileUpdate.getFileId()) || !ownedFileIds.contains(fileUpdate.getFileId())) {
                throw new BusinessException("分P不存在或不属于该稿件");
            }
            if (fileUpdate.getTitle() == null || fileUpdate.getTitle().isBlank()) {
                continue;
            }
            String title = fileUpdate.getTitle().trim();
            if (title.length() > MAX_TITLE_LENGTH) {
                throw new BusinessException("分P标题长度不能超过" + MAX_TITLE_LENGTH + "个字符");
            }
            VideoFile update = new VideoFile();
            update.setTitle(title);
            VideoFileQuery query = new VideoFileQuery();
            query.setFileId(fileUpdate.getFileId());
            query.setVideoId(videoId);
            videoFileMapper.updateByParam(update, query);
        }
    }

    private void changeSeries(String videoId, String seriesId, String userId) {
        VideoSeriesItem current = findSeriesItem(videoId);
        if (StringTools.isEmpty(seriesId)) {
            if (current != null) {
                unbindSeries(videoId);
            }
            return;
        }
        if (current != null) {
            if (seriesId.equals(current.getSeriesId())) {
                return;
            }
            unbindSeries(videoId);
        }
        bindSeries(videoId, seriesId, userId);
    }

    private void bindSeries(String videoId, String seriesId, String userId) {
        VideoSeries series = selectSeries(seriesId);
        if (series == null) {
            throw new BusinessException("合集不存在");
        }
        if (!series.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该合集");
        }
        Integer maxSort = videoSeriesItemMapper.selectMaxSort(seriesId);
        VideoSeriesItem item = new VideoSeriesItem();
        item.setSeriesId(seriesId);
        item.setVideoId(videoId);
        item.setSort(maxSort == null ? 1 : maxSort + 1);
        item.setCreateTime(new Date());
        videoSeriesItemMapper.insert(item);
        videoSeriesMapper.updateVideoCount(seriesId, 1);
    }

    private void unbindSeries(String videoId) {
        VideoSeriesItemQuery query = new VideoSeriesItemQuery();
        query.setVideoId(videoId);
        List<VideoSeriesItem> items = videoSeriesItemMapper.selectList(query);
        for (VideoSeriesItem item : items) {
            VideoSeriesItemQuery deleteQuery = new VideoSeriesItemQuery();
            deleteQuery.setId(item.getId());
            videoSeriesItemMapper.deleteByParam(deleteQuery);
            videoSeriesMapper.updateVideoCount(item.getSeriesId(), -1);
        }
    }

    private VideoSeriesItem findSeriesItem(String videoId) {
        VideoSeriesItemQuery query = new VideoSeriesItemQuery();
        query.setVideoId(videoId);
        List<VideoSeriesItem> items = videoSeriesItemMapper.selectList(query);
        return items.isEmpty() ? null : items.get(0);
    }

    private VideoSeries selectSeries(String seriesId) {
        com.otto.video.entity.query.VideoSeriesQuery query = new com.otto.video.entity.query.VideoSeriesQuery();
        query.setSeriesId(seriesId);
        List<VideoSeries> list = videoSeriesMapper.selectList(query);
        return list.isEmpty() ? null : list.get(0);
    }

    private String generateVideoId() {
        for (int i = 0; i < 5; i++) {
            String videoId = "BV" + StringTools.getRandomString(10);
            if (selectVideo(videoId) == null) {
                return videoId;
            }
        }
        throw new BusinessException("生成稿件ID失败，请重试");
    }
}
