-- ============================================================
-- OttoVideo 用户中心库
-- ============================================================
CREATE DATABASE IF NOT EXISTS otto_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE otto_user;

-- ---------------------------- 用户信息表 ----------------------------
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info` (
  `user_id`        varchar(32)  NOT NULL COMMENT '用户ID（随机数字串）',
  `email`          varchar(128) NOT NULL COMMENT '邮箱',
  `nick_name`      varchar(64)  NOT NULL COMMENT '昵称',
  `avatar`         varchar(256) DEFAULT NULL COMMENT '头像地址',
  `password`       varchar(64)  NOT NULL COMMENT '密码（MD5加盐）',
  `salt`           varchar(16)  NOT NULL COMMENT '密码盐',
  `sex`            tinyint      NOT NULL DEFAULT 0 COMMENT '性别 0未知 1男 2女',
  `personal_sign`  varchar(256) DEFAULT NULL COMMENT '个性签名',
  `birthday`       date         DEFAULT NULL COMMENT '生日',
  `role`           tinyint      NOT NULL DEFAULT 0 COMMENT '角色 0普通用户 1管理员',
  `status`         tinyint      NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1正常',
  `integral`       int          NOT NULL DEFAULT 0 COMMENT '硬币数',
  `total_follows`  int          NOT NULL DEFAULT 0 COMMENT '关注数',
  `total_fans`     int          NOT NULL DEFAULT 0 COMMENT '粉丝数',
  `total_likes`    int          NOT NULL DEFAULT 0 COMMENT '获赞数',
  `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_login_time` datetime    DEFAULT NULL COMMENT '最后登录时间',
  `update_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_nick_name` (`nick_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- ---------------------------- 关注关系表 ----------------------------
DROP TABLE IF EXISTS `user_follow`;
CREATE TABLE `user_follow` (
  `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `user_id`        varchar(32)  NOT NULL COMMENT '粉丝用户ID',
  `follow_user_id` varchar(32)  NOT NULL COMMENT '被关注用户ID（UP主）',
  `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_follow` (`user_id`, `follow_user_id`),
  KEY `idx_follow_user` (`follow_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注关系表';
