# OttoVideo 仿 B 站个人视频网站（微服务版）实施计划

## 一、项目调研结论（参考现有工程风格）

通过分析 `D:\WorkSpace\javaPrj` 下已有工程，确定沿用以下约定：

**后端（参考 betterMusic-java）**
- Spring Boot 3.x + JDK 21 + Maven 多模块聚合工程
- MyBatis（XML Mapper）+ 自研泛型 `BaseMapper<T, P>`（selectList / selectCount / insert / insertOrUpdate / insertBatch / updateByParam / deleteByParam）
- 分层包结构：`controller` / `service` + `service.impl` / `mappers` / `entity.po` / `entity.query` / `entity.vo` / `entity.dto` / `entity.enums` / `config` / `interceptor` / `annotation` / `redis` / `utils` / `exception` / `constants`
- 统一返回 `ResponseVO<T>{status, code, info, data}`，`ResponseCodeEnum`：200/404/600/601/500/901
- 分页三件套：`BaseParam(pageNo,pageSize,orderBy)` + `SimplePage` + `PaginationResultVO<T>`
- `BusinessException` + `AGlobalExceptionHandlerController` 全局异常；`ABaseController` 提供成功/异常响应与 token 用户解析
- 登录态：请求头 `token` → Redis 存取 `TokenUserInfoDTO`；`@GlobalInterceptor(checkLogin)` 注解 + 拦截器
- 主键使用 varchar(32) 随机串；日期 `Date` + `@JsonFormat(GMT+8)`；fastjson2、commons-lang3/codec/io、OkHttp、easy-captcha、spring-mail
- 自研代码生成器 `javaCode/easyjava`：读 MySQL 表结构生成 PO/Query/Mapper/XML/Service/Controller（本次需参数化后按服务复用）

**前端（参考 bettermusic-front / bettermusic-front-admin）**
- Vue 3 + Vite + Element Plus + @element-plus/icons-vue + axios（+ crypto-js / vue-cookies）
- `utils/Request.js`：baseURL `/api`、默认 FormData、请求头携带 `token`、901 跳登录、统一 Loading/Message
- 用户站与管理后台两个独立前端工程

**OttoVideo 与旧工程的差异**：由单体多模块升级为 Spring Cloud Alibaba 微服务，每服务独立 schema，服务间 OpenFeign 调用，MQ 做最终一致，MinIO 做对象存储，FFmpeg 做视频转码。

## 二、总体架构

### 技术选型（已确认）

| 类别 | 选型 |
|---|---|
| 基础框架 | JDK 21、Spring Boot 3.4.x、Spring Cloud 2024.0.x、Spring Cloud Alibaba 2023.0.3.x（阶段 0 先验证兼容矩阵，不行则回退 Boot 3.3.x 组合） |
| 注册/配置中心 | Nacos 2.x |
| 网关 | Spring Cloud Gateway（路由、CORS、统一鉴权、限流） |
| 服务间调用 | OpenFeign + Spring Cloud LoadBalancer |
| 持久层 | MyBatis（沿用 BaseMapper 风格）、MySQL 8（一服务一 schema）、HikariCP |
| 缓存/会话 | Redis（登录态、验证码、计数、排行榜、详情缓存） |
| 消息队列 | RabbitMQ（转码任务、计数聚合、通知、ES 同步） |
| 对象存储 | MinIO（视频/封面/头像；S3 协议，可平滑换 OSS） |
| 视频处理 | FFmpeg（多码率 HLS：m3u8+ts、切片、截图封面、时长探测） |
| 搜索（进阶） | Elasticsearch 8 + 中文分词 |
| 限流降级（进阶） | Sentinel |
| 前端 | Vue 3 + Vite + Element Plus + Pinia + Vue Router + axios；播放器用西瓜播放器 xgplayer（HLS + 弹幕插件） |
| 编排（进阶） | Docker Compose（Nacos/MySQL/Redis/RabbitMQ/MinIO/ES/Sentinel Dashboard） |

### 微服务拆分与端口

| 服务 | 端口 | 库 | 职责 |
|---|---|---|---|
| otto-gateway | 8080 | — | 统一入口、路由、CORS、token 鉴权、用户信息透传、限流 |
| otto-user-service | 8081 | otto_user | 注册/登录/邮箱验证码、资料、头像、关注粉丝、角色 |
| otto-video-service | 8082 | otto_video | 投稿、分 P、分区、标签、合集、稿件状态机、审核、视频查询 |
| otto-media-service | 8083 | 无（Redis+MinIO+MQ） | 分片上传、秒传、FFmpeg 转码 HLS、截图，结果经 MQ 回写 |
| otto-interact-service | 8084 | otto_interact | 弹幕、楼中楼评论、点赞/投币/收藏三连、历史、稍后再看、举报 |
| otto-stat-service | 8085 | otto_stat | Redis 计数聚合、定时落库、日表、排行榜、播放上报 |
| otto-message-service | 8086 | otto_message | 站内通知（回复/赞/币/藏/关注/审核结果/系统）、未读数 |
| otto-search-service | 8087 | ES（无 MySQL） | 视频搜索、联想词（进阶阶段） |
| otto-admin-service | 8088 | otto_admin | 后台 BFF：审核、用户/分区/标签/举报/字典管理、看板 |

跨服务铁律：**禁止跨库 JOIN，禁止直连别人的库**；用户昵称头像、计数等一律 Feign 批量接口 + Redis 缓存拼装。

### 后端工程结构（D:\WorkSpace\javaPrj\OttoVideo\otto-cloud）

```text
otto-cloud/
  pom.xml                     # 父 POM：Boot/Cloud/SCA/MyBatis/MinIO/AMQP 版本管理
  sql/                        # 全部建库建表脚本 + 初始化数据
    otto_user.sql / otto_video.sql / otto_interact.sql /
    otto_stat.sql / otto_message.sql / otto_admin.sql
  docker-compose.yml          # 中间件一键启动（进阶阶段补全）
  otto-common/                # ResponseVO、异常、BaseParam/SimplePage、BaseMapper、
                              # StringTools/DateUtil/JsonUtils、RedisUtils、Feign 通用 DTO、
                              # MQ 常量、MinIO 配置、全局异常、用户上下文
  otto-gateway/
  otto-user/
    otto-user-api/            # Feign 接口 + fallback + 对外 DTO（供其他服务依赖）
    otto-user-service/
  otto-video/  (otto-video-api + otto-video-service)
  otto-media/  (otto-media-api + otto-media-service)
  otto-interact/ (otto-interact-api + otto-interact-service)
  otto-stat/   (otto-stat-api + otto-stat-service)
  otto-message/(otto-message-api + otto-message-service)
  otto-search/ (otto-search-service)
  otto-admin/  (otto-admin-service)
```

每个 service 内部沿用旧工程包结构；`xxx-api` 只放 FeignClient、fallback、跨服务 DTO、枚举。

### 前端工程结构（与旧工程布局一致）

```text
OttoVideo/
  otto-web/      用户站（首页/分区/详情播放器/投稿/个人空间/消息/搜索）
  otto-admin/    管理后台（审核/用户/分区标签/举报/字典/看板）
  otto-cloud/    后端（见上）
```

### 关键链路设计

1. **登录态**：登录后 token 写 Redis（`otto:token:web:{token}`，1 天过期；后台 `otto:token:admin:{token}`）。网关全局过滤器按路径规则校验白名单，命中受保护资源则查 Redis，并向下游透传请求头 `X-User-Id / X-Nick-Name / X-Avatar / X-Role`；下游 common WebFilter 解析为 UserContext。后台路由要求 role=1。
2. **投稿转码链路**：前端调 video 创建稿件(status=0转码中, 多 P 记录) → 分片上传至 media/MinIO（支持 hash 秒传、断点续传）→ media 发 `video.transcode.queue` → media 消费者 FFmpeg 转多码率 HLS + 截图 → 发 `video.transcode.result.queue` → video-service 更新 video_file/hls_path 与稿件状态为待审核。
3. **互动计数链路（最终一致）**：点赞/投币/收藏/评论/弹幕在本服务本地事务落库 → 发 `video.count.queue`（含动作类型与增量）→ stat-service 写 Redis Hash `otto:video:stat:{videoId}` → 定时聚合落 video_stat / video_stat_daily；video 列表通过 stat-api 批量取数。粉丝/获赞数经 MQ 由 user-service 回写。
4. **通知链路**：评论/点赞/投币/收藏/关注/审核动作 → `notification.queue` → message-service 落 user_notification，前端查未读数。
5. **播放上报**：播放器定时/结束时调 stat 播放上报，Redis 计数 + 历史记录由 interact 维护（去重、UV 可 HyperLogLog）。
6. **弹幕**：发送入库（video_time 毫秒）；播放按分 P 分段拉取（按时间区间索引），xgplayer 弹幕插件渲染。
7. **搜索演进**：首期 video 服务 MySQL LIKE 兜底；进阶 search-service 建 ES 索引，稿件发布/状态变更经 `video.search.queue` 同步。

## 三、数据库设计（一服务一 schema）

约定：主键 varchar(32) 随机串（高频流水表用 bigint 自增）；snake_case 列名（MyBatis 开下划线转驼峰）；不建物理外键，靠索引+应用保证；统一 `create_time` / `update_time`；状态类字段 tinyint + 枚举类（沿用 enums 包风格）。

### 1. otto_user（用户中心）

**user_info（用户）**
`user_id` varchar(32) PK｜`email` varchar 唯一｜`nick_name`｜`avatar`｜`password`（MD5+盐）｜`salt`｜`sex` tinyint(0未知1男2女)｜`personal_sign`｜`birthday` date｜`role` tinyint(0普通1管理员)｜`status` tinyint(0禁用1正常)｜`integral` int 硬币｜`total_follows`/`total_fans`/`total_likes` int 冗余计数｜`create_time`/`last_login_time`/`update_time`

**user_follow（关注关系）**
`id` bigint PK｜`user_id`（粉丝）｜`follow_user_id`（UP）｜`create_time`；唯一索引 `(user_id, follow_user_id)`，双方普通索引

### 2. otto_video（视频中心）

**video_partition（分区，两级）**
`partition_id` int PK 自增｜`parent_id` int｜`name`｜`code`｜`icon`｜`sort` int｜`status` tinyint

**video_info（稿件）**
`video_id` varchar(32) PK（BV 风格生成）｜`user_id`｜`partition_id` int｜`title`｜`cover`｜`introduction` text｜`tags` varchar（标签名冗余展示）｜`status` tinyint(0转码中1转码失败2待审核3已发布4审核驳回5下架)｜`reject_reason`｜`duration` int 秒（取第一 P）｜`create_time`/`publish_time`/`update_time`；索引 user_id、partition_id、status、publish_time

**video_file（分 P / 转码产物）**
`file_id` varchar(32) PK｜`video_id`｜`file_index` int P 数｜`title`｜`duration` int｜`file_size` bigint｜`resolution` varchar｜`origin_path`（MinIO 原片 key）｜`hls_path`（m3u8 key）｜`cover_path`｜`status` tinyint(0转码中1成功2失败)｜`create_time`/`update_time`；索引 video_id

**video_tag（标签）**：`tag_id` bigint PK｜`tag_name` 唯一｜`use_count` int｜`create_time`
**video_tag_ref（稿件-标签）**：`id` bigint PK｜`video_id`｜`tag_id`；唯一 `(video_id,tag_id)`
**video_series（合集）**：`series_id` varchar(32) PK｜`user_id`｜`name`｜`description`｜`cover`｜`video_count` int｜`create_time`/`update_time`
**video_series_item（合辑条目）**：`id` bigint PK｜`series_id`｜`video_id`｜`sort` int｜`create_time`
**video_audit_log（审核记录）**：`id` bigint PK｜`video_id`｜`auditor_id`｜`status` tinyint｜`opinion`｜`create_time`

### 3. otto_interact（互动）

**video_comment（评论，楼中楼）**
`comment_id` varchar(32) PK｜`video_id`｜`file_id`（分 P，可空）｜`user_id`｜`root_id`（根评论，根为自身/0）｜`parent_id`（直接父评论）｜`reply_user_id`（被回复人）｜`content`｜`like_count`/`reply_count` int 冗余｜`status` tinyint(0正常1删除)｜`create_time`；索引 (video_id, root_id)、user_id

**video_comment_like（评论点赞）**：`id` bigint PK｜`comment_id`｜`user_id`｜`create_time`；唯一 `(comment_id,user_id)`
**video_like（视频点赞）**：`id` bigint PK｜`video_id`｜`user_id`｜`create_time`；唯一 `(video_id,user_id)`
**video_coin（投币）**：`id` bigint PK｜`video_id`｜`user_id`｜`coin_count` tinyint(1/2)｜`create_time`；唯一 `(video_id,user_id)`
**video_favorite_folder（收藏夹）**：`folder_id` varchar(32) PK｜`user_id`｜`name`｜`cover`｜`video_count` int｜`privacy` tinyint(0公开1私密)｜`create_time`/`update_time`
**video_favorite（收藏关系）**：`id` bigint PK｜`folder_id`｜`video_id`｜`user_id`｜`create_time`；唯一 `(folder_id,video_id)`
**video_danmaku（弹幕）**：`id` bigint PK｜`video_id`｜`file_id`｜`user_id`｜`video_time` int 毫秒｜`content` varchar(500)｜`color` varchar(16)｜`mode` tinyint(1滚动4底部5顶部)｜`font_size` tinyint｜`create_time`；联合索引 `(file_id, video_time)`
**video_history（历史）**：`id` bigint PK｜`user_id`｜`video_id`｜`file_id`｜`play_duration` int（看到秒）｜`video_duration` int｜`create_time`/`update_time`；唯一 `(user_id,video_id)`
**video_watch_later（稍后再看）**：`id` bigint PK｜`user_id`｜`video_id`｜`create_time`；唯一 `(user_id,video_id)`
**video_report（举报）**：`report_id` varchar(32) PK｜`target_type` tinyint(0视频1评论2用户)｜`target_id`｜`user_id`｜`reason_type` int｜`content`｜`handle_status` tinyint(0待处理1已处理)｜`handle_user_id`｜`create_time`/`handle_time`

### 4. otto_stat（统计）

**video_stat（计数总表）**：`video_id` varchar(32) PK｜`play_count`/`like_count`/`coin_count`/`favorite_count`/`comment_count`/`danmaku_count`/`share_count` bigint｜`update_time`（定时从 Redis 聚合覆盖/累加落库）
**video_stat_daily（日增量）**：`id` bigint PK｜`video_id`｜`stat_date` date｜各计数字段 bigint；唯一 `(video_id,stat_date)`
排行榜不落表：Redis ZSet `otto:rank:play:{partitionId}:{date}`、全站榜。

### 5. otto_message（消息）

**user_notification（通知）**：`notification_id` varchar(32) PK｜`user_id`（接收者）｜`send_user_id`｜`type` tinyint(0回复1点赞2投币3收藏4关注5系统6审核结果)｜`video_id`/`comment_id`（关联资源，可空）｜`content`｜`read_status` tinyint(0未读1已读)｜`create_time`；索引 (user_id, read_status)
（私信会话/消息表预留，首期不建）

### 6. otto_admin（系统/后台）

**sys_dict（字典类型）**：`dict_code` PK｜`dict_name`｜`status`｜`create_time`
**sys_dict_data（字典项）**：`data_id` bigint PK｜`dict_code`｜`data_label`｜`data_value`｜`sort`｜`status`
管理员登录复用 user_info（role=1），不另建账号表。

### 7. ES 索引（进阶，无 MySQL）

`video_index`：videoId、userId、nickName、title、introduction、tags、partitionId、duration、cover、playCount、likeCount、createTime；IK 分词，发布/上下线经 MQ 同步。

## 四、实施步骤（分阶段清单，严格依赖排序）

**阶段 0：基础设施与骨架**
1. 准备环境：JDK21/Maven/Node20；用 Docker Compose 启动 Nacos、MySQL8、Redis、RabbitMQ、MinIO；记录各连接信息到 Nacos 配置
2. 创建 `otto-cloud` 聚合父工程，锁定 Boot/Cloud/SCA 版本并跑通最小启动验证兼容矩阵；搭建 `otto-common`（从旧工程迁移 ResponseVO、枚举、异常、分页、BaseMapper、工具类、RedisUtils、全局异常、UserContext）
3. 搭建 otto-gateway：注册 Nacos、CORS、路由、全局鉴权过滤器（白名单 + Redis token 校验 + 用户头透传 + 管理员角色）
4. otto-user-service 空壳注册 Nacos，并验证 Nacos 配置中心下发（datasource/redis 配置外置）

**阶段 1：数据库与用户中心**
5. 编写 6 个 schema 的建库建表 SQL 与分区/字典初始化数据，本地执行验证
6. 参数化 easyjava 生成器（path.base/package.base 可按服务传入），为各服务生成 Po/Query/Mapper/XML/Service 骨架
7. user-service：邮箱验证码（沿用 easy-captcha + Redis）、注册、登录（token 写 Redis）、资料修改、头像上传（经 MinIO）、关注/粉丝列表、user-api（批量查用户信息 fallback）
8. 网关鉴权端到端联调（含未登录 901、越权）

**阶段 2：视频中心 + 媒资**
9. video-service：分区树、标签、投稿创建/编辑（多 P + 合集）、我的稿件、状态机、审核接口（供 admin Feign 调用）、video-api
10. media-service：MinIO 分片上传/断点续传/hash 秒传；RabbitMQ 转码消费者；FFmpeg 探测时长、多码率 HLS 切片、截图封面；结果 MQ 回写，video-service 消费更新状态
11. 首页/分区/详情查询：Feign 批量补 UP 主信息、Redis 缓存详情与分区、状态仅展示已发布

**阶段 3：互动**
12. interact-service：弹幕发送与按时间分段拉取；评论楼中楼（根评论/回复分页）+ 评论点赞；点赞/投币/收藏夹收藏（"三连"一次请求本地事务 + 发 MQ）；历史、稍后再看、举报
13. 动作统一发 `video.count.queue`；硬币不足校验（查 user 积分，投币成功经 MQ 扣减）

**阶段 4：统计与消息**
14. stat-service：消费计数 MQ 写 Redis；定时落 video_stat / video_stat_daily；播放上报（UV 去重）；分区/全站排行榜 ZSet；stat-api 批量计数
15. message-service：消费互动/关注/审核事件生成通知；通知列表、未读数、全部已读；粉丝数/获赞数变更回写 user-service

**阶段 5：用户站前端（otto-web）**
16. 脚手架：沿用 Request.js（baseURL `/api` 代理网关 8080）、Message/Verify、Pinia、路由守卫；登录/注册页
17. 首页：分区导航、推荐 feed 位、排行榜；分区列表页（分页+排序）
18. 视频详情：xgplayer 播放 HLS + 弹幕插件（发送/屏蔽/透明度）、三连按钮、收藏夹选择、评论楼中楼、分 P 切换、相关推荐
19. 投稿页：封面裁剪（沿用 ImageCoverCut 风格）、多 P 分片上传 + 进度条、标签选择、稿件管理（状态/驳回原因）
20. 个人空间（投稿/合集/收藏/关注粉丝/历史/稍后再看/资料设置）、消息中心、搜索页（首期 LIKE 兜底）

**阶段 6：管理后台（otto-admin-service + otto-admin）**
21. 后台登录（role 校验）、Layout、看板（总数/趋势调 stat）
22. 视频审核列表 + 在线预览 + 通过/驳回（写 audit_log 发 MQ 通知）；用户管理（禁用/积分）；分区/标签管理；举报处理；字典管理（sys_dict）

**阶段 7：进阶能力**
23. search-service：ES 建索引 + MQ 同步 + 中文分词搜索/搜索联想，替换前端 LIKE
24. 推荐 Feed：基于播放/点赞/分区的简单加权推荐，结果写 Redis
25. Sentinel 网关限流与服务降级（Feign fallback 全链路补齐）；Docker Compose 一键编排全部中间件；traceId 日志透传（micrometer-tracing）

## 五、依赖与注意事项

- 版本兼容：Spring Cloud Alibaba 对 Boot 版本有滞后，阶段 0 必须先确认 `Boot 3.4.x + Cloud 2024.0.x + SCA 2023.0.3.x` 能正常注册/配置；备选 Boot 3.3.x 组合
- Feign 只依赖对方 `xxx-api` 模块，禁止 service 模块互相依赖；循环调用改为走 MQ
- 三连、投币扣积分等跨服务操作不引入 Seata，用"本地事务 + MQ 最终一致 + 幂等消费（消息表/唯一键去重）"
- MinIO bucket 规划：`otto-video`（原片/hls，按 yyyy-MM 分目录取代旧工程本地目录）、`otto-cover`、`otto-avatar`；通过预签名 URL 上传/播放
- 转码为 CPU 密集任务，消费者独立线程池配置，必要时 media-service 可多实例
- Redis 统一 key 前缀常量放 otto-common（沿用 Constans 风格）；各服务用不同 key 前缀避免冲突
- 安全：密码加盐 MD5（沿用旧工程，可升级 BCrypt）；弹幕/评论 XSS 过滤；管理端接口网关双重角色校验；MinIO 密钥仅存 Nacos
- 代码生成器当前 `path.base/package.base` 写死，需改为命令行参数/按服务配置文件输出

## 六、验证方式

- 每个服务：注册 Nacos 控制台可见；配置中心改配置能动态生效
- SQL：全部脚本在空库可重复执行；唯一索引/普通索引齐备
- 阶段 1：Postman/Apipifox 跑通注册→验证码→登录→网关鉴权；token 失效返回 901
- 阶段 2：真实 mp4 走通上传→转码→m3u8 可播放→待审核全链路；转码失败状态正确回写
- 阶段 3：弹幕时序正确、三连后计数与状态一致、重复请求幂等
- 阶段 4：Redis 计数定时落库与流水一致；通知触达且未读数正确
- 阶段 5/6：前端两大工程各核心页面浏览器实测（登录态、上传进度、播放器弹幕、审核流）
- 阶段 7：ES 搜索召回、限流触发降级、`docker compose up` 全新环境可启动

## 七、风险与对策

- **微服务粒度对个人项目偏重**：严格按阶段推进，阶段 0-1 完成后即具备可运行骨架；media/search 等可在不影响主链路下后置
- **FFmpeg/MinIO 在 Windows 的安装成本**：统一用 Docker Compose；FFmpeg 用 jave2 或容器内官方镜像，命令参数先本地小样验证
- **N+1 远程调用**：所有拼装走 Feign 批量接口（如按 userIds 批量取用户），并加 Redis 缓存与短 TTL
- **消息重复/丢失**：生产端 confirm、消费端幂等（唯一键 + 消息去重表），转码任务超时重试与死信队列
- **版本不兼容**：阶段 0 设为硬门槛，先跑通 Nacos+Gateway+Feign Hello World 再写业务
- **HLS 播放跨域/鉴权**：m3u8/ts 经网关或 MinIO 预签名 URL 放行，避免播放器无法带自定义头
