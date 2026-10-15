-- ============================================================
-- OttoVideo 数据统计库（Redis 聚合后定时落库）
-- ============================================================
CREATE DATABASE IF NOT EXISTS otto_stat DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE otto_stat;

-- ---------------------------- 视频计数总表 ----------------------------
DROP TABLE IF EXISTS `video_stat`;
CREATE TABLE `video_stat` (
  `video_id`       varchar(32) NOT NULL,
  `play_count`     bigint      NOT NULL DEFAULT 0 COMMENT '播放数',
  `like_count`     bigint      NOT NULL DEFAULT 0 COMMENT '点赞数',
  `coin_count`     bigint      NOT NULL DEFAULT 0 COMMENT '投币数',
  `favorite_count` bigint      NOT NULL DEFAULT 0 COMMENT '收藏数',
  `comment_count`  bigint      NOT NULL DEFAULT 0 COMMENT '评论数',
  `danmaku_count`  bigint      NOT NULL DEFAULT 0 COMMENT '弹幕数',
  `share_count`    bigint      NOT NULL DEFAULT 0 COMMENT '分享数',
  `update_time`    datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`video_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频计数总表';

-- ---------------------------- 视频计数日表 ----------------------------
DROP TABLE IF EXISTS `video_stat_daily`;
CREATE TABLE `video_stat_daily` (
  `id`             bigint      NOT NULL AUTO_INCREMENT,
  `video_id`       varchar(32) NOT NULL,
  `stat_date`      date        NOT NULL,
  `play_count`     bigint      NOT NULL DEFAULT 0,
  `like_count`     bigint      NOT NULL DEFAULT 0,
  `coin_count`     bigint      NOT NULL DEFAULT 0,
  `favorite_count` bigint      NOT NULL DEFAULT 0,
  `comment_count`  bigint      NOT NULL DEFAULT 0,
  `danmaku_count`  bigint      NOT NULL DEFAULT 0,
  `create_time`    datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_video_date` (`video_id`, `stat_date`),
  KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频计数日增量表';
