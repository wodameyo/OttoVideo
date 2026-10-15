package com.otto.common.constants;

/**
 * 全局常量（Redis Key、请求头、MQ 队列等，沿用 Constans 风格集中管理）
 */
public interface Constants {

    // ============ Redis Key 前缀 ============
    String REDIS_KEY_TOKEN_WEB = "otto:token:web:";
    String REDIS_KEY_TOKEN_ADMIN = "otto:token:admin:";
    String REDIS_KEY_CHECK_CODE = "otto:checkcode:";
    String REDIS_KEY_EMAIL_CODE = "otto:emailcode:";
    String REDIS_KEY_VIDEO_STAT = "otto:video:stat:";
    String REDIS_KEY_RANK_PLAY = "otto:rank:play:";
    String REDIS_KEY_USER_INFO = "otto:user:info:";
    String REDIS_KEY_VIDEO_DETAIL = "otto:video:detail:";

    // ============ 过期时间（秒） ============
    long REDIS_KEY_EXPIRES_ONE_MIN = 60L;
    long REDIS_KEY_EXPIRES_ONE_HOUR = 3600L;
    long REDIS_KEY_EXPIRES_ONE_DAY = 86400L;
    long REDIS_KEY_EXPIRES_HALF_HOUR = 1800L;

    // ============ 网关透传请求头 ============
    String HEADER_TOKEN = "token";
    String HEADER_USER_ID = "X-User-Id";
    String HEADER_NICK_NAME = "X-Nick-Name";
    String HEADER_AVATAR = "X-Avatar";
    String HEADER_ROLE = "X-Role";

    String STATUS_SUCCESS = "success";
    String STATUS_ERROR = "error";

    Integer ROLE_USER = 0;
    Integer ROLE_ADMIN = 1;

    // ============ RabbitMQ 队列 / 交换机 / 路由键 ============
    String MQ_EXCHANGE_VIDEO = "otto.video.exchange";
    String MQ_QUEUE_TRANSCODE = "video.transcode.queue";
    String MQ_ROUTING_TRANSCODE = "video.transcode";
    String MQ_QUEUE_TRANSCODE_RESULT = "video.transcode.result.queue";
    String MQ_ROUTING_TRANSCODE_RESULT = "video.transcode.result";

    String MQ_EXCHANGE_INTERACT = "otto.interact.exchange";
    String MQ_QUEUE_VIDEO_COUNT = "video.count.queue";
    String MQ_ROUTING_VIDEO_COUNT = "video.count";
    String MQ_QUEUE_NOTIFICATION = "notification.queue";
    String MQ_ROUTING_NOTIFICATION = "notification";

    String MQ_EXCHANGE_SEARCH = "otto.search.exchange";
    String MQ_QUEUE_VIDEO_INDEX = "video.search.queue";
    String MQ_ROUTING_VIDEO_INDEX = "video.search";
}
