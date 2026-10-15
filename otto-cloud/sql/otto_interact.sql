-- ============================================================
-- OttoVideo 互动服务库（弹幕/评论/赞币藏/历史/举报）
-- ============================================================
CREATE DATABASE IF NOT EXISTS otto_interact DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE otto_interact;

-- ---------------------------- 视频评论表（楼中楼） ----------------------------
DROP TABLE IF EXISTS `video_comment`;
CREATE TABLE `video_comment` (
  `comment_id`   varchar(32)  NOT NULL,
  `video_id`     varchar(32)  NOT NULL,
  `file_id`      varchar(32)  DEFAULT NULL COMMENT '分P文件ID，可空',
  `user_id`      varchar(32)  NOT NULL COMMENT '评论人',
  `root_id`      varchar(32)  NOT NULL DEFAULT '0' COMMENT '根评论ID，根评论为自身ID',
  `parent_id`    varchar(32)  NOT NULL DEFAULT '0' COMMENT '直接父评论ID',
  `reply_user_id` varchar(32) DEFAULT NULL COMMENT '被回复人ID',
  `content`      varchar(1000) NOT NULL COMMENT '评论内容',
  `like_count`   int          NOT NULL DEFAULT 0,
  `reply_count`  int          NOT NULL DEFAULT 0,
  `status`       tinyint      NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
  `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`comment_id`),
  KEY `idx_video_root` (`video_id`, `root_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频评论表';

-- ---------------------------- 评论点赞表 ----------------------------
DROP TABLE IF EXISTS `video_comment_like`;
CREATE TABLE `video_comment_like` (
  `id`          bigint      NOT NULL AUTO_INCREMENT,
  `comment_id`  varchar(32) NOT NULL,
  `user_id`     varchar(32) NOT NULL,
  `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞表';

-- ---------------------------- 视频点赞表 ----------------------------
DROP TABLE IF EXISTS `video_like`;
CREATE TABLE `video_like` (
  `id`          bigint      NOT NULL AUTO_INCREMENT,
  `video_id`    varchar(32) NOT NULL,
  `user_id`     varchar(32) NOT NULL,
  `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_video_user` (`video_id`, `user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频点赞表';

-- ---------------------------- 视频投币表 ----------------------------
DROP TABLE IF EXISTS `video_coin`;
CREATE TABLE `video_coin` (
  `id`          bigint      NOT NULL AUTO_INCREMENT,
  `video_id`    varchar(32) NOT NULL,
  `user_id`     varchar(32) NOT NULL,
  `coin_count`  tinyint     NOT NULL DEFAULT 1 COMMENT '投币数量 1或2',
  `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_video_user` (`video_id`, `user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频投币表';

-- ---------------------------- 收藏夹表 ----------------------------
DROP TABLE IF EXISTS `video_favorite_folder`;
CREATE TABLE `video_favorite_folder` (
  `folder_id`   varchar(32)  NOT NULL,
  `user_id`     varchar(32)  NOT NULL,
  `name`        varchar(64)  NOT NULL,
  `cover`       varchar(256) DEFAULT NULL,
  `video_count` int          NOT NULL DEFAULT 0,
  `privacy`     tinyint      NOT NULL DEFAULT 0 COMMENT '0公开 1私密',
  `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`folder_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频收藏夹表';

-- ---------------------------- 视频收藏关系表 ----------------------------
DROP TABLE IF EXISTS `video_favorite`;
CREATE TABLE `video_favorite` (
  `id`          bigint      NOT NULL AUTO_INCREMENT,
  `folder_id`   varchar(32) NOT NULL,
  `video_id`    varchar(32) NOT NULL,
  `user_id`     varchar(32) NOT NULL,
  `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_folder_video` (`folder_id`, `video_id`),
  KEY `idx_video_id` (`video_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频收藏关系表';

-- ---------------------------- 弹幕表 ----------------------------
DROP TABLE IF EXISTS `video_danmaku`;
CREATE TABLE `video_danmaku` (
  `id`          bigint       NOT NULL AUTO_INCREMENT,
  `video_id`    varchar(32)  NOT NULL,
  `file_id`     varchar(32)  NOT NULL COMMENT '分P文件ID',
  `user_id`     varchar(32)  NOT NULL,
  `video_time`  int          NOT NULL COMMENT '出现时间（毫秒）',
  `content`     varchar(500) NOT NULL,
  `color`       varchar(16)  NOT NULL DEFAULT '#FFFFFF',
  `mode`        tinyint      NOT NULL DEFAULT 1 COMMENT '1滚动 4底部 5顶部',
  `font_size`   tinyint      NOT NULL DEFAULT 25,
  `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_file_time` (`file_id`, `video_time`),
  KEY `idx_video_id` (`video_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频弹幕表';

-- ---------------------------- 观看历史表 ----------------------------
DROP TABLE IF EXISTS `video_history`;
CREATE TABLE `video_history` (
  `id`            bigint      NOT NULL AUTO_INCREMENT,
  `user_id`       varchar(32) NOT NULL,
  `video_id`      varchar(32) NOT NULL,
  `file_id`       varchar(32) DEFAULT NULL,
  `play_duration` int         NOT NULL DEFAULT 0 COMMENT '观看到的位置（秒）',
  `video_duration` int        NOT NULL DEFAULT 0 COMMENT '视频总时长（秒）',
  `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次观看时间',
  `update_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最近观看时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_video` (`user_id`, `video_id`),
  KEY `idx_user_update` (`user_id`, `update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='观看历史表';

-- ---------------------------- 稍后再看表 ----------------------------
DROP TABLE IF EXISTS `video_watch_later`;
CREATE TABLE `video_watch_later` (
  `id`          bigint      NOT NULL AUTO_INCREMENT,
  `user_id`     varchar(32) NOT NULL,
  `video_id`    varchar(32) NOT NULL,
  `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_video` (`user_id`, `video_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='稍后再看表';

-- ---------------------------- 举报表 ----------------------------
DROP TABLE IF EXISTS `video_report`;
CREATE TABLE `video_report` (
  `report_id`     varchar(32)  NOT NULL,
  `target_type`   tinyint      NOT NULL COMMENT '举报对象 0视频 1评论 2用户',
  `target_id`     varchar(32)  NOT NULL,
  `user_id`       varchar(32)  NOT NULL COMMENT '举报人',
  `reason_type`   int          NOT NULL COMMENT '举报原因类型（字典）',
  `content`       varchar(512) DEFAULT NULL COMMENT '补充说明',
  `handle_status` tinyint      NOT NULL DEFAULT 0 COMMENT '0待处理 1已处理',
  `handle_user_id` varchar(32) DEFAULT NULL,
  `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `handle_time`   datetime     DEFAULT NULL,
  PRIMARY KEY (`report_id`),
  KEY `idx_handle_status` (`handle_status`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';
