-- =============================================
-- Luck Report 数据库初始化脚本
-- 数据库类型：MySQL
-- 说明：合并最终表结构（含 model_config.api_path、知识库 content 大字段等）
-- =============================================

SET NAMES utf8mb4;

-- -------------------------------------------
-- 会话表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `luck_chat_session` (
    `id` varchar(36) NOT NULL COMMENT '会话ID（UUID）',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` tinyint DEFAULT 0 COMMENT '删除标志(0-未删除,1-已删除)',
    `title` varchar(255) DEFAULT '新对话' COMMENT '会话标题',
    `status` varchar(50) DEFAULT 'active' COMMENT '状态：active-活跃，archived-归档',
    `is_pinned` tinyint DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
    `user_id` varchar(64) DEFAULT NULL COMMENT '用户ID（字符串形式，兼容数字主键、UUID、工号等）',
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
    `id` varchar(32) NOT NULL COMMENT '消息ID（Snowflake）',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` tinyint DEFAULT 0 COMMENT '删除标志(0-未删除,1-已删除)',
    `session_id` varchar(36) NOT NULL COMMENT '会话ID',
    `role` varchar(20) NOT NULL COMMENT '角色：user-用户，assistant-助手，system-系统，tool_result-工具结果',
    `content` text COMMENT '消息内容',
    `message_type` varchar(50) DEFAULT 'text' COMMENT '消息类型：text-文本，tool_call-工具调用，tool_result-工具结果，error-错误',
    `metadata` json DEFAULT NULL COMMENT '元数据（JSON格式，存储tool_calls数组或tool_call_id等）',
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
    `id` varchar(32) NOT NULL COMMENT '配置ID（Snowflake）',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `del_flag` int(11) DEFAULT '0' COMMENT '删除标志(0-未删除,1-已删除)',
    `provider` varchar(255) NOT NULL COMMENT '厂商标识(如 alibaba、openai、deepseek),方便前端展示回显',
    `base_url` varchar(255) NOT NULL COMMENT 'API基础地址(如 https://dashscope.aliyuncs.com/compatible-mode/v1)',
    `api_key` varchar(255) NOT NULL COMMENT 'API密钥',
    `model_name` varchar(255) NOT NULL COMMENT '模型名称(如 qwen3.5-plus、text-embedding-v3)',
    `config_name` varchar(50) DEFAULT NULL COMMENT '自定义名称,最多50个字',
    `sort` int(11) DEFAULT '0' COMMENT '排序字段,数字越小越靠前',
    `temperature` decimal(10,2) unsigned DEFAULT '0.00' COMMENT '温度参数,控制生成随机性,0~1',
    `is_active` tinyint(1) DEFAULT '0' COMMENT '是否激活:true-当前使用,false-未使用',
    `context_window_tokens` int(11) DEFAULT '128000' COMMENT '上下文窗口大小（token），用于对话压缩判断',
    `model_type` varchar(20) NOT NULL DEFAULT 'CHAT' COMMENT '模型类型(CHAT/EMBEDDING/RERANK)',
    `api_path` varchar(255) DEFAULT NULL COMMENT 'API路径，拼在base_url后；空则按类型使用默认路径',
    `proxy_enabled` tinyint(1) DEFAULT '0' COMMENT '是否启用代理:0-禁用,1-启用',
    `proxy_host` varchar(255) DEFAULT NULL COMMENT '代理主机地址',
    `proxy_port` int(11) DEFAULT NULL COMMENT '代理端口',
    `proxy_username` varchar(255) DEFAULT NULL COMMENT '代理用户名(可选)',
    `proxy_password` varchar(255) DEFAULT NULL COMMENT '代理密码(可选)',
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
    `id`              varchar(32) NOT NULL COMMENT '主键ID（Snowflake）',
    `create_by`       varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time`     datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time`     datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`        tinyint DEFAULT 0 COMMENT '删除标志(0-未删除,1-已删除)',
    `name`            varchar(100) NOT NULL COMMENT '数据源名称',
    `type`            varchar(50) NOT NULL COMMENT '数据源类型：mysql/postgresql/oracle/dameng/sqlserver/hive',
    `host`            varchar(255) NOT NULL COMMENT '主机地址',
    `port`            int NOT NULL COMMENT '端口号',
    `database_name`   varchar(100) COMMENT '数据库名',
    `username`        varchar(100) COMMENT '用户名',
    `password`        varchar(512) COMMENT '密码（__LUCK__ 密文或兼容明文）',
    `connection_url`  varchar(500) COMMENT '完整连接URL',
    `status`          varchar(20) DEFAULT 'active' COMMENT '状态：active/inactive',
    `test_status`     varchar(20) DEFAULT 'unknown' COMMENT '连接测试状态：success/failed/unknown',
    `description`     text COMMENT '描述',
    `model_id`        varchar(32) DEFAULT NULL COMMENT '嵌入模型配置ID（Snowflake）',
    `initialized_tables` text COMMENT '已初始化的表名列表（JSON格式存储，如["table1","table2"]）',
    PRIMARY KEY (`id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据源配置表';

-- -------------------------------------------
-- 逻辑外键配置表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `luck_logical_relation` (
    `id`                 varchar(32) NOT NULL COMMENT '主键ID（Snowflake）',
    `create_by`          varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time`        datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`          varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time`        datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`           tinyint DEFAULT 0 COMMENT '删除标志(0-未删除,1-已删除)',
    `datasource_id`      varchar(32) NOT NULL COMMENT '数据源ID',
    `source_table_name`  varchar(100) NOT NULL COMMENT '主表名',
    `source_column_name` varchar(100) NOT NULL COMMENT '主表字段名',
    `target_table_name`  varchar(100) NOT NULL COMMENT '关联表名',
    `target_column_name` varchar(100) NOT NULL COMMENT '关联表字段名',
    `relation_type`      varchar(20) COMMENT '关系类型：1:1/1:N/N:1',
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
    `id` varchar(32) NOT NULL COMMENT '主键ID（Snowflake）',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` tinyint DEFAULT 0 COMMENT '删除标志(0-未删除,1-已删除)',
    `title` varchar(255) NOT NULL COMMENT '知识标题',
    `type` varchar(20) NOT NULL COMMENT '知识类型：DOCUMENT-文档，QA-问答对，FAQ-常见问题',
    `question` text DEFAULT NULL COMMENT '问题（FAQ和QA类型时使用）',
    `content` mediumtext DEFAULT NULL COMMENT '知识正文（DOCUMENT 存解析全文；QA/FAQ 存答案）',
    `enabled` tinyint DEFAULT 1 COMMENT '是否生效（0:不生效, 1:生效）',
    `embedding_status` varchar(20) DEFAULT 'PENDING' COMMENT '向量化状态：PENDING待处理，PROCESSING处理中，COMPLETED已完成，FAILED失败',
    `error_msg` varchar(500) DEFAULT NULL COMMENT '操作失败的错误信息',
    `source_filename` varchar(255) DEFAULT NULL COMMENT '原始文件名',
    `file_path` varchar(500) DEFAULT NULL COMMENT '文件存储路径',
    `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
    `file_type` varchar(100) DEFAULT NULL COMMENT '文件类型',
    `splitter_type` varchar(20) DEFAULT 'token' COMMENT '分块策略类型：token, recursive, sentence, paragraph, semantic',
    `model_id` varchar(32) DEFAULT NULL COMMENT '嵌入模型配置ID（Snowflake）',
    `is_resource_cleaned` tinyint DEFAULT 0 COMMENT '物理资源是否已清理（0:未清理, 1:已清理）',
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
    `id` varchar(32) NOT NULL COMMENT '主键ID（Snowflake）',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` tinyint DEFAULT 0 COMMENT '删除标志(0-未删除,1-已删除)',
    `title` varchar(255) NOT NULL COMMENT '知识标题',
    `type` varchar(20) NOT NULL COMMENT '知识类型：DOCUMENT-文档，QA-问答对，FAQ-常见问题',
    `question` text DEFAULT NULL COMMENT '问题（FAQ和QA类型时使用）',
    `content` mediumtext DEFAULT NULL COMMENT '知识正文（DOCUMENT 存解析全文；QA/FAQ 存答案）',
    `enabled` tinyint DEFAULT 1 COMMENT '是否生效（0:不生效, 1:生效）',
    `embedding_status` varchar(20) DEFAULT 'PENDING' COMMENT '向量化状态：PENDING待处理，PROCESSING处理中，COMPLETED已完成，FAILED失败',
    `error_msg` varchar(500) DEFAULT NULL COMMENT '操作失败的错误信息',
    `source_filename` varchar(255) DEFAULT NULL COMMENT '原始文件名',
    `file_path` varchar(500) DEFAULT NULL COMMENT '文件存储路径',
    `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
    `file_type` varchar(100) DEFAULT NULL COMMENT '文件类型',
    `splitter_type` varchar(20) DEFAULT 'token' COMMENT '分块策略类型：token, recursive, sentence, paragraph, semantic',
    `model_id` varchar(32) DEFAULT NULL COMMENT '嵌入模型配置ID（Snowflake）',
    `is_resource_cleaned` tinyint DEFAULT 0 COMMENT '物理资源是否已清理（0:未清理, 1:已清理）',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_enabled` (`enabled`),
    KEY `idx_embedding_status` (`embedding_status`),
    KEY `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能体知识表';

-- -------------------------------------------
-- 报表文件表（数据库存储）
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `luck_report_template` (
    `id` varchar(32) NOT NULL COMMENT '主键ID（Snowflake）',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` tinyint DEFAULT 0 COMMENT '删除标志(0-未删除,1-已删除)',
    `title` varchar(255) NOT NULL COMMENT '报表标题',
    `template` mediumtext DEFAULT NULL COMMENT '报表模板内容（XML）',
    PRIMARY KEY (`id`),
    KEY `idx_del_flag` (`del_flag`),
    KEY `idx_title` (`title`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表文件表（数据库存储）';

-- -------------------------------------------
-- 角色与报表绑定关系表（精简版）
-- file_path 存储"provider 前缀 + 报表路径"的完整字符串：
--   - 'file:test.ureport.xml'  文件系统存储
--   - 'db:1'                   数据库存储（db: provider 用主键 id 作为路径）
--   - 'classpath:foo.ureport.xml'
--   - '*'                      通配：表示该角色可访问所有报表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `luck_report_role` (
    `id` varchar(32) NOT NULL COMMENT '主键ID（Snowflake）',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` tinyint DEFAULT 0 COMMENT '删除标志(0-未删除,1-已删除)',
    `role_code` varchar(128) NOT NULL COMMENT '角色编码（第三方系统角色 ID）',
    `file_path` varchar(512) NOT NULL COMMENT '报表完整路径：<provider>:<path>，* 代表全部',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_file` (`role_code`, `file_path`),
    KEY `idx_file_path` (`file_path`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色与报表绑定关系表（精简版）';

-- -------------------------------------------
-- 公共数据集表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `luck_report_dataset` (
    `id`            varchar(32) NOT NULL COMMENT '主键ID（Snowflake）',
    `create_by`     varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time`   datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time`   datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`      tinyint DEFAULT 0 COMMENT '删除标志(0-未删除,1-已删除)',
    `name`          varchar(100) NOT NULL COMMENT '数据集名称（全局唯一）',
    `type`          varchar(20) NOT NULL COMMENT '类型：sql-SQL数据集，json-JSON数据集',
    `datasource_id` varchar(32) DEFAULT NULL COMMENT '绑定的公共数据源ID（sql类型必填，json类型为空）',
    `sql_content`   longtext COMMENT 'SQL语句（sql类型使用）',
    `json_content`  longtext COMMENT 'JSON数组内容（json类型使用）',
    `parameters`    text COMMENT 'SQL参数定义JSON数组：[{"name":"deptId","type":"String","defaultValue":"1"}]',
    `fields`        text COMMENT '字段列表JSON数组：[{"name":"username"},...]，可为空',
    `description`   varchar(500) DEFAULT NULL COMMENT '描述',
    `status`        varchar(20) NOT NULL DEFAULT 'active' COMMENT '状态：active-启用，inactive-禁用',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pd_name` (`name`),
    INDEX `idx_pd_datasource_id` (`datasource_id`),
    INDEX `idx_pd_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公共数据集表';
