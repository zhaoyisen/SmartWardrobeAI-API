--  app用户表
DROP TABLE IF EXISTS `users`;

CREATE TABLE `users`
(
    `id`            BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username`      VARCHAR(50)  NOT NULL COMMENT '昵称',
    `email`         VARCHAR(100) UNIQUE COMMENT '邮箱 (登录凭证1)',
    `phone`         VARCHAR(20) UNIQUE COMMENT '手机号 (登录凭证2)',
    `password_hash` VARCHAR(255) COMMENT 'BCrypt加密密码',
    `avatar_url`    VARCHAR(512) COMMENT '头像URL',
    `height`        INT COMMENT '身高(cm)',
    `weight`        INT COMMENT '体重(kg)',
    `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- 确保至少有一个联系方式不为空的约束通常由代码控制，或者使用复杂的 Check Constraint
    INDEX           `idx_email` (`email`),
    INDEX           `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='app用户核心表';

ALTER TABLE users ADD COLUMN status TINYINT(1) DEFAULT 1 COMMENT '状态:1启用 0禁用';

-- admin用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT,
    `username`    varchar(64)  NOT NULL COMMENT '用户名',
    `password`    varchar(128) NOT NULL COMMENT '密码(BCrypt加密)',
    `nickname`    varchar(64)  DEFAULT NULL COMMENT '昵称',
    `avatar`      varchar(255) DEFAULT NULL COMMENT '头像',
    `status`      tinyint(1) DEFAULT 1 COMMENT '状态:1启用 0禁用',
    `create_time` datetime     DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台管理员表';

-- 插入默认管理员: admin / 123456
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `status`)
VALUES (1, 'admin', '$2a$10$uLuIUy4O3hymWIK20NlrWO7ksp6YtuWiGULCOAMAtsRyBHLhNZJUW', '超级管理员', 1);


-- #附件表
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file`
(
    `id`          bigint NOT NULL AUTO_INCREMENT,
    `file_name`   varchar(255) DEFAULT NULL COMMENT '原始文件名',
    `file_path`   varchar(500) DEFAULT NULL COMMENT '文件存储路径(OSS Key)',
    `file_url`    varchar(500) DEFAULT NULL COMMENT '完整访问URL(快照)',
    `file_size`   bigint       DEFAULT NULL COMMENT '文件大小(字节)',
    `file_type`   varchar(10)  DEFAULT NULL COMMENT '扩展名(jpg/png)',
    `platform`    varchar(20)  DEFAULT 'qiniu' COMMENT '存储平台(qiniu/aliyun/local)',
    `create_by`   varchar(64)  DEFAULT NULL COMMENT '上传人',
    `create_time` datetime     DEFAULT NULL,
    `update_time` datetime     DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件记录表';

-- 添加 file_hash 字段
ALTER TABLE sys_file
    ADD COLUMN file_hash VARCHAR(64) COMMENT '文件内容哈希(MD5)';

-- 加个索引，以后查得快
CREATE INDEX idx_file_hash ON sys_file (file_hash);


-- #AI模型表
DROP TABLE IF EXISTS `sys_ai_model`;
CREATE TABLE `sys_ai_model`
(
    `id`                      bigint(20) NOT NULL AUTO_INCREMENT,
    `model_key`               varchar(64)  NOT NULL COMMENT '前端传参的标识 (如: qwen-plus)',
    `label`                   varchar(64)  NOT NULL COMMENT '前端展示名称 (如: 通义千问VL Plus)',
    `model_name`              varchar(64)  NOT NULL COMMENT '实际调用模型名 (如: qwen-vl-plus)',
    `base_url`                varchar(255) NOT NULL COMMENT '接口地址',
    `api_key`                 varchar(128) NOT NULL COMMENT 'API Key',

    `support_thinking`        tinyint(1) DEFAULT 0 COMMENT '能力开关: 是否支持思考模式',
    `max_thinking_budget`     bigint(20) DEFAULT 4096 COMMENT '风控限制: 最大允许的思考Token数',
    `default_enable_thinking` tinyint(1) DEFAULT 0 COMMENT '默认配置: 若前端未传，是否默认开启',
    `default_thinking_budget` bigint(20) DEFAULT 1024 COMMENT '默认配置: 若前端未传，默认Token数',

    `sort`                    int(11) DEFAULT 0 COMMENT '排序',
    `status`                  tinyint(1) DEFAULT 1 COMMENT '状态: 1启用 0禁用',
    `create_time`             datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time`             datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_model_key` (`model_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型配置表';


-- #衣物表
DROP TABLE IF EXISTS `clothing`;

CREATE TABLE `clothing`
(
    `id`            bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`       bigint(20) NOT NULL COMMENT '所属用户ID',

    -- ================= 核心图片区 =================
    `image_id`      bigint(20) NOT NULL COMMENT '原始图片ID (关联sys_file id)',
    `mask_image_id` bigint(20) DEFAULT NULL COMMENT 'AI抠图后的透明底图ID (关联sys_file id)',

    -- ================= 核心分类 (业务逻辑关键) =================
    `name`          varchar(64) NOT NULL COMMENT '衣物名称 (例如: 我的白T恤)',

    `region`        varchar(32) NOT NULL COMMENT '部位 (Region): TOP(上装)/BOTTOM(下装)/DRESS(全身)/SHOES(鞋)/ACCESSORY(配饰)',
    `category`      varchar(32) NOT NULL COMMENT '具体品类 (Category): T-shirt/Jeans/Hoodie/Coat...',

    `default_layer` int(11) DEFAULT 2 COMMENT '建议层级: 1-Inner(贴身), 2-Middle(常规), 3-Outer(外套), 4-Accessory(配饰)',

    -- ================= AI 识别属性 (AI分析结果) =================
    `color`         varchar(32)    DEFAULT NULL COMMENT '主色调',
    `season`        varchar(32)    DEFAULT NULL COMMENT '适用季节 (Spring/Summer/Autumn/Winter)',
    `fit_type`      varchar(32)    DEFAULT 'Regular' COMMENT '版型: Slim(修身)/Regular(标准)/Loose(宽松)/Oversize',
    `view_type`     varchar(32)    DEFAULT 'Flat' COMMENT '视角: Flat(平铺)/Hanger(挂拍)/Model(模特)/Folded(折叠)',

    -- ================= 用户补充信息 (管理属性) =================
    `shelf_no`      varchar(32)    DEFAULT NULL COMMENT '货架号/收纳位置 (例如: A-1-05)',
    `brand`         varchar(64)    DEFAULT NULL COMMENT '品牌',
    `size`          varchar(32)    DEFAULT NULL COMMENT '尺码 (S/M/L/XL/40/42)',
    `price`         decimal(10, 2) DEFAULT NULL COMMENT '购买价格',
    `purchase_date` date           DEFAULT NULL COMMENT '购买日期',

    -- ================= 状态与统计 =================
    `status`        tinyint(4) DEFAULT 1 COMMENT '状态: 1-在柜, 2-洗衣中, 3-借出, 0-丢弃/回收',
    `wear_count`    int(11) DEFAULT 0 COMMENT '穿着次数统计',

    -- ================= 系统字段 =================
    `create_time`   datetime       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`      tinyint(1) DEFAULT 0 COMMENT '逻辑删除 (0:正常, 1:删除)',

    PRIMARY KEY (`id`),
    KEY             `idx_user_id` (`user_id`),
    KEY             `idx_region` (`region`),
    KEY             `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能衣物表';


-- 数据字典模块建表脚本

-- 字典类型表
DROP TABLE IF EXISTS `sys_dict_type`;

CREATE TABLE `sys_dict_type`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `dict_type`   varchar(64) NOT NULL COMMENT '字典类型编码（唯一标识，如：gender, status, color）',
    `dict_name`   varchar(64) NOT NULL COMMENT '字典类型名称（如：性别、状态、颜色）',
    `remark`      varchar(255) DEFAULT NULL COMMENT '备注说明',
    `sort`        int(11) DEFAULT 0 COMMENT '排序值（数字越小越靠前）',
    `status`      tinyint(1) DEFAULT 1 COMMENT '状态：1启用 0禁用',
    `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dict_type` (`dict_type`),
    KEY           `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

-- 初始化示例数据
INSERT INTO `sys_dict_type` (`dict_type`, `dict_name`, `remark`, `sort`, `status`)
VALUES ('gender', '性别', '用户性别字典', 1, 1),
       ('user_status', '用户状态', '用户账号状态', 2, 1),
       ('clothing_color', '服装颜色', '服装颜色字典，用于AI识别', 3, 1),
       ('clothing_season', '适用季节', '服装适用季节', 4, 1);

-- 字典数据表
DROP TABLE IF EXISTS `sys_dict_data`;

CREATE TABLE `sys_dict_data`
(
    `id`           bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `dict_type_id` bigint(20) NOT NULL COMMENT '字典类型ID（关联sys_dict_type.id）',
    `dict_type`    varchar(64) NOT NULL COMMENT '字典类型编码（冗余字段，方便查询）',
    `dict_label`   varchar(64) NOT NULL COMMENT '字典标签（显示文本，如：男、女、红色）',
    `dict_value`   varchar(64) NOT NULL COMMENT '字典值（存储值，如：male, female, red）',
    `prompt_text`  varchar(255) DEFAULT NULL COMMENT 'AI提示词补充（关键字段，用于AI识别，如：dark red, burgundy）',
    `remark`       varchar(255) DEFAULT NULL COMMENT '备注说明',
    `sort`         int(11) DEFAULT 0 COMMENT '排序值（数字越小越靠前）',
    `status`       tinyint(1) DEFAULT 1 COMMENT '状态：1启用 0禁用',
    `create_time`  datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY            `idx_dict_type_id` (`dict_type_id`),
    KEY            `idx_dict_type` (`dict_type`),
    KEY            `idx_status` (`status`),
    KEY            `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

-- 初始化示例数据
INSERT INTO `sys_dict_data` (`dict_type_id`, `dict_type`, `dict_label`, `dict_value`, `prompt_text`, `sort`, `status`)
VALUES (1, 'gender', '男', 'male', 'male, man', 1, 1),
       (1, 'gender', '女', 'female', 'female, woman', 2, 1),
       (3, 'clothing_color', '红色', 'red', 'red, bright red', 1, 1),
       (3, 'clothing_color', '酒红', 'burgundy', 'dark red, burgundy, wine red', 2, 1),
       (3, 'clothing_color', '蓝色', 'blue', 'blue, azure, sky blue', 3, 1);


-- 数据字典模块建表脚本

-- 字典类型表
DROP TABLE IF EXISTS `sys_dict_type`;

CREATE TABLE `sys_dict_type`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `dict_type`   varchar(64) NOT NULL COMMENT '字典类型编码（唯一标识，如：gender, status, color）',
    `dict_name`   varchar(64) NOT NULL COMMENT '字典类型名称（如：性别、状态、颜色）',
    `remark`      varchar(255) DEFAULT NULL COMMENT '备注说明',
    `sort`        int(11) DEFAULT 0 COMMENT '排序值（数字越小越靠前）',
    `status`      tinyint(1) DEFAULT 1 COMMENT '状态：1启用 0禁用',
    `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dict_type` (`dict_type`),
    KEY           `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

-- 初始化示例数据
INSERT INTO `sys_dict_type` (`dict_type`, `dict_name`, `remark`, `sort`, `status`)
VALUES ('gender', '性别', '用户性别字典', 1, 1),
       ('user_status', '用户状态', '用户账号状态', 2, 1),
       ('clothing_color', '服装颜色', '服装颜色字典，用于AI识别', 3, 1),
       ('clothing_season', '适用季节', '服装适用季节', 4, 1);

-- 字典数据表
DROP TABLE IF EXISTS `sys_dict_data`;

CREATE TABLE `sys_dict_data`
(
    `id`           bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `dict_type_id` bigint(20) NOT NULL COMMENT '字典类型ID（关联sys_dict_type.id）',
    `dict_type`    varchar(64) NOT NULL COMMENT '字典类型编码（冗余字段，方便查询）',
    `dict_label`   varchar(64) NOT NULL COMMENT '字典标签（显示文本，如：男、女、红色）',
    `dict_value`   varchar(64) NOT NULL COMMENT '字典值（存储值，如：male, female, red）',
    `prompt_text`  varchar(255) DEFAULT NULL COMMENT 'AI提示词补充（关键字段，用于AI识别，如：dark red, burgundy）',
    `remark`       varchar(255) DEFAULT NULL COMMENT '备注说明',
    `sort`         int(11) DEFAULT 0 COMMENT '排序值（数字越小越靠前）',
    `status`       tinyint(1) DEFAULT 1 COMMENT '状态：1启用 0禁用',
    `create_time`  datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY            `idx_dict_type_id` (`dict_type_id`),
    KEY            `idx_dict_type` (`dict_type`),
    KEY            `idx_status` (`status`),
    KEY            `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

-- 初始化示例数据
INSERT INTO `sys_dict_data` (`dict_type_id`, `dict_type`, `dict_label`, `dict_value`, `prompt_text`, `sort`, `status`)
VALUES (1, 'gender', '男', 'male', 'male, man', 1, 1),
       (1, 'gender', '女', 'female', 'female, woman', 2, 1),
       (3, 'clothing_color', '红色', 'red', 'red, bright red', 1, 1),
       (3, 'clothing_color', '酒红', 'burgundy', 'dark red, burgundy, wine red', 2, 1),
       (3, 'clothing_color', '蓝色', 'blue', 'blue, azure, sky blue', 3, 1);



