-- ============================================================
-- OttoVideo 消息通知库
-- ============================================================
CREATE DATABASE IF NOT EXISTS otto_message DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE otto_message;

-- ---------------------------- 用户通知表 ----------------------------
DROP TABLE IF EXISTS `user_notification`;
CREATE TABLE `user_notification` (
  `notification_id` varchar(32)  NOT NULL,
  `user_id`         varchar(32)  NOT NULL COMMENT '接收者ID',
  `send_user_id`    varchar(32)  DEFAULT NULL COMMENT '触发者ID（系统消息为空）',
  `type`            tinyint      NOT NULL COMMENT '0回复 1点赞 2投币 3收藏 4关注 5系统 6审核结果',
  `video_id`        varchar(32)  DEFAULT NULL COMMENT '关联视频ID',
  `comment_id`      varchar(32)  DEFAULT NULL COMMENT '关联评论ID',
  `content`         varchar(1000) NOT NULL COMMENT '通知内容',
  `read_status`     tinyint      NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
  `create_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`notification_id`),
  KEY `idx_user_read` (`user_id`, `read_status`),
  KEY `idx_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户站内通知表';
