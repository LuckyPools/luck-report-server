-- =============================================
-- Luck Report 数据库初始化脚本
-- 数据库类型：MySQL
-- 约定：列不设 DEFAULT；审计时间 / 开关 / 枚举等一律由应用写入
-- 日期类型统一：datetime
-- =============================================

SET NAMES utf8mb4;

-- -------------------------------------------
-- 会话表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `luck_chat_session` (
    `id` varchar(36) NOT NULL COMMENT '会话ID',
    `create_by` varchar(128) COMMENT '创建人',
    `create_time` datetime COMMENT '创建时间',
    `update_by` varchar(128) COMMENT '更新人',
    `update_time` datetime COMMENT '更新时间',
    `del_flag` tinyint NOT NULL COMMENT '删除标志：0-未删除，1-已删除',
    `title` varchar(255) NOT NULL COMMENT '会话标题',
    `is_pinned` tinyint NOT NULL COMMENT '是否置顶：0-否，1-是',
    `user_id` varchar(128) COMMENT '用户ID',
    PRIMARY KEY (`id`),
    KEY `idx_user_id_del_pinned_update` (`user_id`, `del_flag`, `is_pinned`, `update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='聊天会话表';

-- -------------------------------------------
-- 消息表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `luck_chat_message` (
    `id` varchar(32) NOT NULL COMMENT '消息ID',
    `create_by` varchar(128) COMMENT '创建人',
    `create_time` datetime COMMENT '创建时间',
    `update_by` varchar(128) COMMENT '更新人',
    `update_time` datetime COMMENT '更新时间',
    `del_flag` tinyint NOT NULL COMMENT '删除标志：0-未删除，1-已删除',
    `session_id` varchar(36) NOT NULL COMMENT '会话ID',
    `role` varchar(20) NOT NULL COMMENT '角色：user / assistant / system / tool_result',
    `content` mediumtext COMMENT '消息内容',
    `message_type` varchar(50) NOT NULL COMMENT '消息类型：text / tool_call / tool_result / error',
    `metadata` json COMMENT '元数据',
    PRIMARY KEY (`id`),
    KEY `idx_session_del_create` (`session_id`, `del_flag`, `create_time`),
    CONSTRAINT `luck_chat_message_ibfk_1` FOREIGN KEY (`session_id`) REFERENCES `luck_chat_session` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='聊天消息表';

-- -------------------------------------------
-- 模型配置表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `luck_model_config` (
    `id` varchar(32) NOT NULL COMMENT '配置ID',
    `create_by` varchar(128) COMMENT '创建人',
    `create_time` datetime COMMENT '创建时间',
    `update_by` varchar(128) COMMENT '更新人',
    `update_time` datetime COMMENT '更新时间',
    `del_flag` tinyint NOT NULL COMMENT '删除标志：0-未删除，1-已删除',
    `provider` varchar(255) NOT NULL COMMENT '厂商标识',
    `base_url` varchar(255) NOT NULL COMMENT 'API基础地址',
    `api_key` varchar(1024) NOT NULL COMMENT 'API密钥（应用层加密存储）',
    `model_name` varchar(255) NOT NULL COMMENT '模型名称',
    `config_name` varchar(50) COMMENT '自定义名称',
    `sort` int COMMENT '排序字段',
    `temperature` decimal(10,2) unsigned COMMENT '温度参数',
    `is_enabled` tinyint NOT NULL COMMENT '是否启用：0-禁用，1-启用',
    `context_window_tokens` int COMMENT '上下文窗口大小',
    `model_type` varchar(20) NOT NULL COMMENT '模型类型：CHAT / EMBEDDING / RERANK',
    `api_path` varchar(255) COMMENT 'API路径',
    `is_proxy_enabled` tinyint NOT NULL COMMENT '是否启用代理：0-禁用，1-启用',
    `proxy_host` varchar(255) COMMENT '代理主机地址',
    `proxy_port` int COMMENT '代理端口',
    `proxy_username` varchar(255) COMMENT '代理用户名',
    `proxy_password` varchar(1024) COMMENT '代理密码（应用层加密存储）',
    PRIMARY KEY (`id`),
    INDEX `idx_model_type_enabled_del_sort` (`model_type`, `is_enabled`, `del_flag`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='大模型配置表';

-- -------------------------------------------
-- 数据源配置表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `luck_report_datasource` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `create_by` varchar(128) COMMENT '创建人',
    `create_time` datetime COMMENT '创建时间',
    `update_by` varchar(128) COMMENT '更新人',
    `update_time` datetime COMMENT '更新时间',
    `del_flag` tinyint NOT NULL COMMENT '删除标志：0-未删除，1-已删除',
    `name` varchar(100) NOT NULL COMMENT '数据源名称',
    `type` varchar(50) NOT NULL COMMENT '数据源类型：mysql / postgresql / oracle / dameng / sqlserver / hive',
    `host` varchar(255) NOT NULL COMMENT '主机地址',
    `port` int NOT NULL COMMENT '端口号',
    `database_name` varchar(100) COMMENT '数据库名',
    `username` varchar(100) COMMENT '用户名',
    `password` varchar(1024) COMMENT '密码（应用层加密存储）',
    `connection_url` varchar(500) COMMENT '完整连接URL',
    `is_enabled` tinyint NOT NULL COMMENT '是否启用：0-禁用，1-启用',
    `test_status` varchar(20) NOT NULL COMMENT '连接测试状态：success / failed / unknown',
    `description` varchar(500) COMMENT '描述',
    `initialized_tables` text COMMENT '已初始化的表名列表（JSON）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ds_name` (`name`),
    INDEX `idx_enabled` (`is_enabled`),
    INDEX `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据源配置表';

-- -------------------------------------------
-- 逻辑外键配置表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `luck_logical_relation` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `create_by` varchar(128) COMMENT '创建人',
    `create_time` datetime COMMENT '创建时间',
    `update_by` varchar(128) COMMENT '更新人',
    `update_time` datetime COMMENT '更新时间',
    `del_flag` tinyint NOT NULL COMMENT '删除标志：0-未删除，1-已删除',
    `datasource_id` varchar(32) NOT NULL COMMENT '数据源ID',
    `source_table_name` varchar(100) NOT NULL COMMENT '主表名',
    `source_column_name` varchar(100) NOT NULL COMMENT '主表字段名',
    `target_table_name` varchar(100) NOT NULL COMMENT '关联表名',
    `target_column_name` varchar(100) NOT NULL COMMENT '关联表字段名',
    `relation_type` varchar(20) COMMENT '关系类型：1:1 / 1:N / N:1',
    `description` varchar(500) COMMENT '业务描述',
    PRIMARY KEY (`id`),
    INDEX `idx_datasource_id` (`datasource_id`),
    INDEX `idx_source_table` (`source_table_name`),
    INDEX `idx_target_table` (`target_table_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='逻辑外键配置表';

-- -------------------------------------------
-- 业务知识表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `luck_business_knowledge` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `create_by` varchar(128) COMMENT '创建人',
    `create_time` datetime COMMENT '创建时间',
    `update_by` varchar(128) COMMENT '更新人',
    `update_time` datetime COMMENT '更新时间',
    `del_flag` tinyint NOT NULL COMMENT '删除标志：0-未删除，1-已删除',
    `title` varchar(255) NOT NULL COMMENT '知识标题',
    `type` varchar(20) NOT NULL COMMENT '知识类型：DOCUMENT / QA / FAQ',
    `question` text COMMENT '问题',
    `content` mediumtext COMMENT '知识正文',
    `is_enabled` tinyint NOT NULL COMMENT '是否生效：0-不生效，1-生效',
    `embedding_status` varchar(20) NOT NULL COMMENT '向量化状态：PENDING / PROCESSING / COMPLETED / FAILED',
    `error_msg` varchar(500) COMMENT '错误信息',
    `source_filename` varchar(255) COMMENT '原始文件名',
    `file_size` bigint COMMENT '文件大小',
    `file_type` varchar(100) COMMENT '文件类型',
    `splitter_type` varchar(20) NOT NULL COMMENT '分块策略：token / recursive / sentence / paragraph / semantic',
    `model_id` varchar(32) COMMENT '嵌入模型配置ID',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_enabled` (`is_enabled`),
    KEY `idx_embedding_status` (`embedding_status`),
    KEY `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务知识表';

-- -------------------------------------------
-- 智能体知识表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `luck_agent_knowledge` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `create_by` varchar(128) COMMENT '创建人',
    `create_time` datetime COMMENT '创建时间',
    `update_by` varchar(128) COMMENT '更新人',
    `update_time` datetime COMMENT '更新时间',
    `del_flag` tinyint NOT NULL COMMENT '删除标志：0-未删除，1-已删除',
    `title` varchar(255) NOT NULL COMMENT '知识标题',
    `type` varchar(20) NOT NULL COMMENT '知识类型：DOCUMENT / QA / FAQ',
    `question` text COMMENT '问题',
    `content` mediumtext COMMENT '知识正文',
    `is_enabled` tinyint NOT NULL COMMENT '是否生效：0-不生效，1-生效',
    `embedding_status` varchar(20) NOT NULL COMMENT '向量化状态：PENDING / PROCESSING / COMPLETED / FAILED',
    `error_msg` varchar(500) COMMENT '错误信息',
    `source_filename` varchar(255) COMMENT '原始文件名',
    `file_size` bigint COMMENT '文件大小',
    `file_type` varchar(100) COMMENT '文件类型',
    `splitter_type` varchar(20) NOT NULL COMMENT '分块策略：token / recursive / sentence / paragraph / semantic',
    `model_id` varchar(32) COMMENT '嵌入模型配置ID',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_enabled` (`is_enabled`),
    KEY `idx_embedding_status` (`embedding_status`),
    KEY `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='智能体知识表';

-- -------------------------------------------
-- 报表文件表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `luck_report_template` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `create_by` varchar(128) COMMENT '创建人',
    `create_time` datetime COMMENT '创建时间',
    `update_by` varchar(128) COMMENT '更新人',
    `update_time` datetime COMMENT '更新时间',
    `del_flag` tinyint NOT NULL COMMENT '删除标志：0-未删除，1-已删除',
    `title` varchar(255) NOT NULL COMMENT '报表标题',
    `template` mediumtext COMMENT '报表模板内容',
    PRIMARY KEY (`id`),
    KEY `idx_del_flag` (`del_flag`),
    KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='报表文件表';

-- -------------------------------------------
-- 角色与报表绑定关系表（纯关联，无审计字段）
-- file_path 示例：file:test.ureport.xml / db:1 / classpath:foo.ureport.xml / *
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `luck_report_role` (
    `role_code` varchar(128) NOT NULL COMMENT '角色编码',
    `file_path` varchar(512) NOT NULL COMMENT '报表完整路径',
    PRIMARY KEY (`role_code`, `file_path`),
    KEY `idx_file_path` (`file_path`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色与报表绑定关系表';

-- -------------------------------------------
-- 公共数据集表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `luck_report_dataset` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `create_by` varchar(128) COMMENT '创建人',
    `create_time` datetime COMMENT '创建时间',
    `update_by` varchar(128) COMMENT '更新人',
    `update_time` datetime COMMENT '更新时间',
    `del_flag` tinyint NOT NULL COMMENT '删除标志：0-未删除，1-已删除',
    `name` varchar(100) NOT NULL COMMENT '数据集名称',
    `type` varchar(20) NOT NULL COMMENT '类型：sql / json',
    `datasource_id` varchar(32) COMMENT '绑定的公共数据源ID',
    `sql_content` mediumtext COMMENT 'SQL语句',
    `json_content` mediumtext COMMENT 'JSON数组内容',
    `parameters` text COMMENT 'SQL参数定义（JSON）',
    `fields` text COMMENT '字段列表（JSON）',
    `description` varchar(500) COMMENT '描述',
    `is_enabled` tinyint NOT NULL COMMENT '是否启用：0-禁用，1-启用',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pd_name` (`name`),
    INDEX `idx_pd_datasource_id` (`datasource_id`),
    INDEX `idx_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公共数据集表';
