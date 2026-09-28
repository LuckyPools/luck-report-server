-- =============================================
-- Luck Report 数据库初始化脚本
-- 数据库类型：MySQL
-- =============================================

SET NAMES utf8mb4;

-- -------------------------------------------
-- 会话表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `luck_chat_session` (
    `id` varchar(36) NOT NULL COMMENT '会话ID',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` tinyint DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    `title` varchar(255) DEFAULT '新对话' COMMENT '会话标题',
    `status` varchar(50) DEFAULT 'active' COMMENT '状态：1-active，2-archived',
    `is_pinned` tinyint DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
    `user_id` varchar(64) DEFAULT NULL COMMENT '用户ID',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_is_pinned` (`is_pinned`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='聊天会话表';

-- -------------------------------------------
-- 消息表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `luck_chat_message` (
    `id` varchar(32) NOT NULL COMMENT '消息ID',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` tinyint DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    `session_id` varchar(36) NOT NULL COMMENT '会话ID',
    `role` varchar(20) NOT NULL COMMENT '角色：1-user，2-assistant，3-system，4-tool_result',
    `content` text COMMENT '消息内容',
    `message_type` varchar(50) DEFAULT 'text' COMMENT '消息类型：1-text，2-tool_call，3-tool_result，4-error',
    `metadata` json DEFAULT NULL COMMENT '元数据',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_role` (`role`),
    KEY `idx_message_type` (`message_type`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `luck_chat_message_ibfk_1` FOREIGN KEY (`session_id`) REFERENCES `luck_chat_session` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='聊天消息表';

-- -------------------------------------------
-- 模型配置表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `luck_model_config` (
    `id` varchar(32) NOT NULL COMMENT '配置ID',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `del_flag` int(11) DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
    `provider` varchar(255) NOT NULL COMMENT '厂商标识',
    `base_url` varchar(255) NOT NULL COMMENT 'API基础地址',
    `api_key` varchar(255) NOT NULL COMMENT 'API密钥',
    `model_name` varchar(255) NOT NULL COMMENT '模型名称',
    `config_name` varchar(50) DEFAULT NULL COMMENT '自定义名称',
    `sort` int(11) DEFAULT '0' COMMENT '排序字段',
    `temperature` decimal(10,2) unsigned DEFAULT '0.00' COMMENT '温度参数',
    `is_active` tinyint(1) DEFAULT '0' COMMENT '是否激活：0-未使用，1-当前使用',
    `context_window_tokens` int(11) DEFAULT '128000' COMMENT '上下文窗口大小',
    `model_type` varchar(20) NOT NULL DEFAULT 'CHAT' COMMENT '模型类型：1-CHAT，2-EMBEDDING，3-RERANK',
    `api_path` varchar(255) DEFAULT NULL COMMENT 'API路径',
    `proxy_enabled` tinyint(1) DEFAULT '0' COMMENT '是否启用代理：0-禁用，1-启用',
    `proxy_host` varchar(255) DEFAULT NULL COMMENT '代理主机地址',
    `proxy_port` int(11) DEFAULT NULL COMMENT '代理端口',
    `proxy_username` varchar(255) DEFAULT NULL COMMENT '代理用户名',
    `proxy_password` varchar(255) DEFAULT NULL COMMENT '代理密码',
    PRIMARY KEY (`id`),
    INDEX `idx_model_type` (`model_type`),
    INDEX `idx_is_active` (`is_active`),
    INDEX `idx_provider` (`provider`),
    INDEX `idx_sort` (`sort`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='大模型配置表';

-- -------------------------------------------
-- 数据源配置表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `luck_report_datasource` (
    `id`              varchar(32) NOT NULL COMMENT '主键ID',
    `create_by`       varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time`     datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time`     datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`        tinyint DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    `name`            varchar(100) NOT NULL COMMENT '数据源名称',
    `type`            varchar(50) NOT NULL COMMENT '数据源类型：1-mysql，2-postgresql，3-oracle，4-dameng，5-sqlserver，6-hive',
    `host`            varchar(255) NOT NULL COMMENT '主机地址',
    `port`            int NOT NULL COMMENT '端口号',
    `database_name`   varchar(100) COMMENT '数据库名',
    `username`        varchar(100) COMMENT '用户名',
    `password`        varchar(512) COMMENT '密码',
    `connection_url`  varchar(500) COMMENT '完整连接URL',
    `status`          varchar(20) DEFAULT 'active' COMMENT '状态：1-active，2-inactive',
    `test_status`     varchar(20) DEFAULT 'unknown' COMMENT '连接测试状态：1-success，2-failed，3-unknown',
    `description`     text COMMENT '描述',
    `model_id`        varchar(32) DEFAULT NULL COMMENT '嵌入模型配置ID',
    `initialized_tables` text COMMENT '已初始化的表名列表',
    PRIMARY KEY (`id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据源配置表';

-- -------------------------------------------
-- 逻辑外键配置表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `luck_logical_relation` (
    `id`                 varchar(32) NOT NULL COMMENT '主键ID',
    `create_by`          varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time`        datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`          varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time`        datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`           tinyint DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    `datasource_id`      varchar(32) NOT NULL COMMENT '数据源ID',
    `source_table_name`  varchar(100) NOT NULL COMMENT '主表名',
    `source_column_name` varchar(100) NOT NULL COMMENT '主表字段名',
    `target_table_name`  varchar(100) NOT NULL COMMENT '关联表名',
    `target_column_name` varchar(100) NOT NULL COMMENT '关联表字段名',
    `relation_type`      varchar(20) COMMENT '关系类型：1-1:1，2-1:N，3-N:1',
    `description`        text COMMENT '业务描述',
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
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` tinyint DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    `title` varchar(255) NOT NULL COMMENT '知识标题',
    `type` varchar(20) NOT NULL COMMENT '知识类型：1-DOCUMENT，2-QA，3-FAQ',
    `question` text DEFAULT NULL COMMENT '问题',
    `content` mediumtext DEFAULT NULL COMMENT '知识正文',
    `enabled` tinyint DEFAULT 1 COMMENT '是否生效：0-不生效，1-生效',
    `embedding_status` varchar(20) DEFAULT 'PENDING' COMMENT '向量化状态：1-PENDING，2-PROCESSING，3-COMPLETED，4-FAILED',
    `error_msg` varchar(500) DEFAULT NULL COMMENT '错误信息',
    `source_filename` varchar(255) DEFAULT NULL COMMENT '原始文件名',
    `file_path` varchar(500) DEFAULT NULL COMMENT '文件存储路径',
    `file_size` bigint DEFAULT NULL COMMENT '文件大小',
    `file_type` varchar(100) DEFAULT NULL COMMENT '文件类型',
    `splitter_type` varchar(20) DEFAULT 'token' COMMENT '分块策略类型：1-token，2-recursive，3-sentence，4-paragraph，5-semantic',
    `model_id` varchar(32) DEFAULT NULL COMMENT '嵌入模型配置ID',
    `is_resource_cleaned` tinyint DEFAULT 0 COMMENT '物理资源是否已清理：0-未清理，1-已清理',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_enabled` (`enabled`),
    KEY `idx_embedding_status` (`embedding_status`),
    KEY `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务知识表';

-- -------------------------------------------
-- 智能体知识表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `luck_agent_knowledge` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` tinyint DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    `title` varchar(255) NOT NULL COMMENT '知识标题',
    `type` varchar(20) NOT NULL COMMENT '知识类型：1-DOCUMENT，2-QA，3-FAQ',
    `question` text DEFAULT NULL COMMENT '问题',
    `content` mediumtext DEFAULT NULL COMMENT '知识正文',
    `enabled` tinyint DEFAULT 1 COMMENT '是否生效：0-不生效，1-生效',
    `embedding_status` varchar(20) DEFAULT 'PENDING' COMMENT '向量化状态：1-PENDING，2-PROCESSING，3-COMPLETED，4-FAILED',
    `error_msg` varchar(500) DEFAULT NULL COMMENT '错误信息',
    `source_filename` varchar(255) DEFAULT NULL COMMENT '原始文件名',
    `file_path` varchar(500) DEFAULT NULL COMMENT '文件存储路径',
    `file_size` bigint DEFAULT NULL COMMENT '文件大小',
    `file_type` varchar(100) DEFAULT NULL COMMENT '文件类型',
    `splitter_type` varchar(20) DEFAULT 'token' COMMENT '分块策略类型：1-token，2-recursive，3-sentence，4-paragraph，5-semantic',
    `model_id` varchar(32) DEFAULT NULL COMMENT '嵌入模型配置ID',
    `is_resource_cleaned` tinyint DEFAULT 0 COMMENT '物理资源是否已清理：0-未清理，1-已清理',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_enabled` (`enabled`),
    KEY `idx_embedding_status` (`embedding_status`),
    KEY `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能体知识表';

-- -------------------------------------------
-- 报表文件表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `luck_report_template` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` tinyint DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    `title` varchar(255) NOT NULL COMMENT '报表标题',
    `template` mediumtext DEFAULT NULL COMMENT '报表模板内容',
    PRIMARY KEY (`id`),
    KEY `idx_del_flag` (`del_flag`),
    KEY `idx_title` (`title`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表文件表';

-- -------------------------------------------
-- 角色与报表绑定关系表
-- file_path 示例：file:test.ureport.xml / db:1 / classpath:foo.ureport.xml / *
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `luck_report_role` (
    `role_code` varchar(128) NOT NULL COMMENT '角色编码',
    `file_path` varchar(512) NOT NULL COMMENT '报表完整路径',
    PRIMARY KEY (`role_code`, `file_path`),
    KEY `idx_file_path` (`file_path`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色与报表绑定关系表';

-- -------------------------------------------
-- 公共数据集表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `luck_report_dataset` (
    `id`            varchar(32) NOT NULL COMMENT '主键ID',
    `create_by`     varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time`   datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time`   datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`      tinyint DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    `name`          varchar(100) NOT NULL COMMENT '数据集名称',
    `type`          varchar(20) NOT NULL COMMENT '类型：1-sql，2-json',
    `datasource_id` varchar(32) DEFAULT NULL COMMENT '绑定的公共数据源ID',
    `sql_content`   longtext COMMENT 'SQL语句',
    `json_content`  longtext COMMENT 'JSON数组内容',
    `parameters`    text COMMENT 'SQL参数定义',
    `fields`        text COMMENT '字段列表',
    `description`   varchar(500) DEFAULT NULL COMMENT '描述',
    `status`        varchar(20) NOT NULL DEFAULT 'active' COMMENT '状态：1-active，2-inactive',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pd_name` (`name`),
    INDEX `idx_pd_datasource_id` (`datasource_id`),
    INDEX `idx_pd_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公共数据集表';
