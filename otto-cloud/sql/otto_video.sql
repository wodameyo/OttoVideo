-- ============================================================
-- OttoVideo 视频中心库
-- ============================================================
CREATE DATABASE IF NOT EXISTS otto_video DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE otto_video;

-- ---------------------------- 视频分区表（两级） ----------------------------
DROP TABLE IF EXISTS `video_partition`;
CREATE TABLE `video_partition` (
  `partition_id` int          NOT NULL AUTO_INCREMENT COMMENT '分区ID',
  `parent_id`    int          NOT NULL DEFAULT 0 COMMENT '父分区ID，0为一级分区',
  `name`         varchar(64)  NOT NULL COMMENT '分区名称',
  `code`         varchar(32)  DEFAULT NULL COMMENT '分区编码',
  `icon`         varchar(256) DEFAULT NULL COMMENT '分区图标',
  `sort`         int          NOT NULL DEFAULT 0 COMMENT '排序',
  `status`       tinyint      NOT NULL DEFAULT 1 COMMENT '状态 0停用 1启用',
  `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`partition_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频分区表';

-- ---------------------------- 视频稿件表 ----------------------------
DROP TABLE IF EXISTS `video_info`;
CREATE TABLE `video_info` (
  `video_id`      varchar(32)  NOT NULL COMMENT '视频ID（BV风格）',
  `user_id`       varchar(32)  NOT NULL COMMENT '投稿用户ID',
  `partition_id`  int          NOT NULL COMMENT '分区ID',
  `title`         varchar(128) NOT NULL COMMENT '标题',
  `cover`         varchar(256) DEFAULT NULL COMMENT '封面地址',
  `introduction`  text         COMMENT '简介',
  `tags`          varchar(512) DEFAULT NULL COMMENT '标签名称（逗号分隔，冗余展示）',
  `status`        tinyint      NOT NULL DEFAULT 0 COMMENT '0转码中 1转码失败 2待审核 3已发布 4审核驳回 5已下架',
  `reject_reason` varchar(512) DEFAULT NULL COMMENT '驳回原因',
  `duration`      int          NOT NULL DEFAULT 0 COMMENT '总时长（秒，取第一P）',
  `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `publish_time`  datetime     DEFAULT NULL COMMENT '发布时间',
  `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`video_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_partition_status` (`partition_id`, `status`),
  KEY `idx_status_publish` (`status`, `publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频稿件表';

-- ---------------------------- 分P/转码文件表 ----------------------------
DROP TABLE IF EXISTS `video_file`;
CREATE TABLE `video_file` (
  `file_id`     varchar(32)  NOT NULL COMMENT '文件ID',
  `video_id`    varchar(32)  NOT NULL COMMENT '视频ID',
  `file_index`  int          NOT NULL DEFAULT 1 COMMENT '第几P',
  `title`       varchar(128) DEFAULT NULL COMMENT '分P标题',
  `duration`    int          NOT NULL DEFAULT 0 COMMENT '时长（秒）',
  `file_size`   bigint       NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
  `resolution`  varchar(16)  DEFAULT NULL COMMENT '分辨率，如1080P',
  `origin_path` varchar(512) DEFAULT NULL COMMENT '原片存储路径（MinIO key）',
  `hls_path`    varchar(512) DEFAULT NULL COMMENT 'HLS m3u8 存储路径（MinIO key）',
  `cover_path`  varchar(512) DEFAULT NULL COMMENT '截图封面路径',
  `status`      tinyint      NOT NULL DEFAULT 0 COMMENT '0转码中 1成功 2失败',
  `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`file_id`),
  KEY `idx_video_id` (`video_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频分P文件表';

-- ---------------------------- 视频标签表 ----------------------------
DROP TABLE IF EXISTS `video_tag`;
CREATE TABLE `video_tag` (
  `tag_id`      bigint       NOT NULL AUTO_INCREMENT,
  `tag_name`    varchar(32)  NOT NULL COMMENT '标签名',
  `use_count`   int          NOT NULL DEFAULT 0 COMMENT '被引用次数',
  `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`tag_id`),
  UNIQUE KEY `uk_tag_name` (`tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频标签表';

-- ---------------------------- 稿件-标签关联表 ----------------------------
DROP TABLE IF EXISTS `video_tag_ref`;
CREATE TABLE `video_tag_ref` (
  `id`       bigint      NOT NULL AUTO_INCREMENT,
  `video_id` varchar(32) NOT NULL,
  `tag_id`   bigint      NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_video_tag` (`video_id`, `tag_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='稿件标签关联表';

-- ---------------------------- 视频合集表 ----------------------------
DROP TABLE IF EXISTS `video_series`;
CREATE TABLE `video_series` (
  `series_id`   varchar(32)  NOT NULL,
  `user_id`     varchar(32)  NOT NULL COMMENT 'UP主ID',
  `name`        varchar(128) NOT NULL COMMENT '合集名称',
  `description` varchar(512) DEFAULT NULL,
  `cover`       varchar(256) DEFAULT NULL,
  `video_count` int          NOT NULL DEFAULT 0,
  `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`series_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频合集表';

-- ---------------------------- 合集条目表 ----------------------------
DROP TABLE IF EXISTS `video_series_item`;
CREATE TABLE `video_series_item` (
  `id`          bigint      NOT NULL AUTO_INCREMENT,
  `series_id`   varchar(32) NOT NULL,
  `video_id`    varchar(32) NOT NULL,
  `sort`        int         NOT NULL DEFAULT 0,
  `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_series_video` (`series_id`, `video_id`),
  KEY `idx_video_id` (`video_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合集条目表';

-- ---------------------------- 视频审核记录表 ----------------------------
DROP TABLE IF EXISTS `video_audit_log`;
CREATE TABLE `video_audit_log` (
  `id`          bigint       NOT NULL AUTO_INCREMENT,
  `video_id`    varchar(32)  NOT NULL,
  `auditor_id`  varchar(32)  NOT NULL COMMENT '审核人ID',
  `status`      tinyint      NOT NULL COMMENT '审核结果 3通过 4驳回',
  `opinion`     varchar(512) DEFAULT NULL COMMENT '审核意见',
  `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_video_id` (`video_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频审核记录表';

-- ---------------------------- 初始化分区数据 ----------------------------
INSERT INTO `video_partition` (`partition_id`, `parent_id`, `name`, `code`, `sort`) VALUES
(1, 0, '动画', 'anime', 1),
(2, 0, '游戏', 'game', 2),
(3, 0, '音乐', 'music', 3),
(4, 0, '科技', 'tech', 4),
(5, 0, '生活', 'life', 5),
(101, 1, 'MAD·AMV', 'mad', 1),
(102, 1, 'MM3D', 'mmd', 2),
(201, 2, '单机游戏', 'pcgame', 1),
(202, 2, '电子竞技', 'esports', 2),
(301, 3, '音乐现场', 'live', 1),
(302, 3, '翻唱', 'cover', 2),
(401, 4, '编程', 'coding', 1),
(402, 4, '数码', 'digital', 2),
(501, 5, '日常', 'daily', 1),
(502, 5, '美食', 'food', 2);
