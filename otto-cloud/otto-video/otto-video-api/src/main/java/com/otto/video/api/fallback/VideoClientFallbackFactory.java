package com.otto.video.api.fallback;

import com.otto.common.entity.enums.ResponseCodeEnum;
import com.otto.common.entity.vo.PaginationResultVO;
import com.otto.common.entity.vo.ResponseVO;
import com.otto.video.api.VideoFeignClient;
import com.otto.video.api.dto.VideoAuditItemDTO;
import com.otto.video.api.dto.VideoAuditParamDTO;
import com.otto.video.api.dto.VideoBaseInfoDTO;
import com.otto.video.api.dto.VideoFileDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 视频服务降级：不可用时返回空数据，避免拖垮调用方主链路
 */
@Component
public class VideoClientFallbackFactory implements FallbackFactory<VideoFeignClient> {

    private static final Logger logger = LoggerFactory.getLogger(VideoClientFallbackFactory.class);

    @Override
    public VideoFeignClient create(Throwable cause) {
        logger.error("调用 otto-video-service 失败，触发降级", cause);
        return new VideoFeignClient() {
            @Override
            public ResponseVO<VideoBaseInfoDTO> getById(String videoId) {
                return error(null);
            }

            @Override
            public ResponseVO<List<VideoBaseInfoDTO>> listByIds(List<String> videoIds) {
                return error(List.of());
            }

            @Override
            public ResponseVO<VideoFileDTO> getFile(String fileId) {
                return error(null);
            }

            @Override
            public ResponseVO<PaginationResultVO<VideoAuditItemDTO>> auditPage(Integer pageNo, Integer pageSize,
                                                                               Integer status, String keyword) {
                return error(null);
            }

            @Override
            public ResponseVO<Object> audit(VideoAuditParamDTO param) {
                return error(null);
            }
        };
    }

    private <T> ResponseVO<T> error(T data) {
        ResponseVO<T> vo = new ResponseVO<>();
        vo.setCode(ResponseCodeEnum.CODE_500.getCode());
        vo.setInfo("视频服务暂时不可用");
        vo.setData(data);
        return vo;
    }
}
