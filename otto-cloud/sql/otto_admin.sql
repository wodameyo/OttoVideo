-- ============================================================
-- OttoVideo 后台/系统库（字典等；管理员账号复用 otto_user.user_info role=1）
-- ============================================================
CREATE DATABASE IF NOT EXISTS otto_admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE otto_admin;

-- ---------------------------- 字典类型表 ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict` (
  `dict_code`   varchar(64)  NOT NULL COMMENT '字典编码',
  `dict_name`   varchar(128) NOT NULL COMMENT '字典名称',
  `status`      tinyint      NOT NULL DEFAULT 1 COMMENT '0停用 1启用',
  `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`dict_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统字典类型表';

-- ---------------------------- 字典数据表 ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
  `data_id`     bigint       NOT NULL AUTO_INCREMENT,
  `dict_code`   varchar(64)  NOT NULL,
  `data_label`  varchar(128) NOT NULL COMMENT '字典标签',
  `data_value`  varchar(128) NOT NULL COMMENT '字典值',
  `sort`        int          NOT NULL DEFAULT 0,
  `status`      tinyint      NOT NULL DEFAULT 1,
  `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`data_id`),
  KEY `idx_dict_code` (`dict_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统字典数据表';

-- ---------------------------- 初始化字典 ----------------------------
INSERT INTO `sys_dict` (`dict_code`, `dict_name`) VALUES
('report_reason', '举报原因'),
('video_quality', '视频清晰度');

INSERT INTO `sys_dict_data` (`dict_code`, `data_label`, `data_value`, `sort`) VALUES
('report_reason', '垃圾广告', '1', 1),
('report_reason', '引战弹幕/评论', '2', 2),
('report_reason', '涉政敏感', '3', 3),
('report_reason', '色情低俗', '4', 4),
('report_reason', '侵犯隐私', '5', 5),
('report_reason', '其他', '99', 99),
('video_quality', '360P', '360', 1),
('video_quality', '480P', '480', 2),
('video_quality', '720P', '720', 3),
('video_quality', '1080P', '1080', 4);
