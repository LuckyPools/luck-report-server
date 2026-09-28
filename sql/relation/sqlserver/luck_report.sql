-- =============================================
-- Luck Report 数据库初始化脚本
-- 数据库类型：SQL Server
-- =============================================

-- -------------------------------------------
-- 会话表
-- -------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'luck_chat_session')
CREATE TABLE luck_chat_session (
    id NVARCHAR(36) NOT NULL,
    create_by NVARCHAR(64) DEFAULT NULL,
    create_time DATETIME2 DEFAULT SYSDATETIME(),
    update_by NVARCHAR(64) DEFAULT NULL,
    update_time DATETIME2 DEFAULT SYSDATETIME(),
    del_flag BIT DEFAULT 0,
    title NVARCHAR(255) DEFAULT N'新对话',
    status NVARCHAR(50) DEFAULT 'active',
    is_pinned BIT DEFAULT 0,
    user_id NVARCHAR(64) DEFAULT NULL,
    CONSTRAINT pk_luck_chat_session PRIMARY KEY (id)
);

CREATE INDEX idx_cs_user_id ON luck_chat_session (user_id);
CREATE INDEX idx_cs_status ON luck_chat_session (status);
CREATE INDEX idx_cs_is_pinned ON luck_chat_session (is_pinned);
CREATE INDEX idx_cs_create_time ON luck_chat_session (create_time);

EXEC sp_addextendedproperty 'MS_Description', N'聊天会话表', 'SCHEMA', 'dbo', 'TABLE', 'luck_chat_session';
EXEC sp_addextendedproperty 'MS_Description', N'会话ID', 'SCHEMA', 'dbo', 'TABLE', 'luck_chat_session', 'COLUMN', 'id';
EXEC sp_addextendedproperty 'MS_Description', N'创建人', 'SCHEMA', 'dbo', 'TABLE', 'luck_chat_session', 'COLUMN', 'create_by';
EXEC sp_addextendedproperty 'MS_Description', N'创建时间', 'SCHEMA', 'dbo', 'TABLE', 'luck_chat_session', 'COLUMN', 'create_time';
EXEC sp_addextendedproperty 'MS_Description', N'更新人', 'SCHEMA', 'dbo', 'TABLE', 'luck_chat_session', 'COLUMN', 'update_by';
EXEC sp_addextendedproperty 'MS_Description', N'更新时间', 'SCHEMA', 'dbo', 'TABLE', 'luck_chat_session', 'COLUMN', 'update_time';
EXEC sp_addextendedproperty 'MS_Description', N'删除标志：0-未删除，1-已删除', 'SCHEMA', 'dbo', 'TABLE', 'luck_chat_session', 'COLUMN', 'del_flag';
EXEC sp_addextendedproperty 'MS_Description', N'会话标题', 'SCHEMA', 'dbo', 'TABLE', 'luck_chat_session', 'COLUMN', 'title';
EXEC sp_addextendedproperty 'MS_Description', N'状态：1-active，2-archived', 'SCHEMA', 'dbo', 'TABLE', 'luck_chat_session', 'COLUMN', 'status';
EXEC sp_addextendedproperty 'MS_Description', N'是否置顶：0-否，1-是', 'SCHEMA', 'dbo', 'TABLE', 'luck_chat_session', 'COLUMN', 'is_pinned';
EXEC sp_addextendedproperty 'MS_Description', N'用户ID', 'SCHEMA', 'dbo', 'TABLE', 'luck_chat_session', 'COLUMN', 'user_id';

GO

-- -------------------------------------------
-- 消息表
-- -------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'luck_chat_message')
CREATE TABLE luck_chat_message (
    id NVARCHAR(32) NOT NULL,
    create_by NVARCHAR(64) DEFAULT NULL,
    create_time DATETIME2 DEFAULT SYSDATETIME(),
    update_by NVARCHAR(64) DEFAULT NULL,
    update_time DATETIME2 DEFAULT SYSDATETIME(),
    del_flag BIT DEFAULT 0,
    session_id NVARCHAR(36) NOT NULL,
    role NVARCHAR(20) NOT NULL,
    content NVARCHAR(MAX),
    message_type NVARCHAR(50) DEFAULT 'text',
    metadata NVARCHAR(MAX) DEFAULT NULL,
    CONSTRAINT pk_luck_chat_message PRIMARY KEY (id),
    CONSTRAINT fk_luck_chat_message_session FOREIGN KEY (session_id) REFERENCES luck_chat_session (id) ON DELETE CASCADE
);

CREATE INDEX idx_cm_session_id ON luck_chat_message (session_id);
CREATE INDEX idx_cm_role ON luck_chat_message (role);
CREATE INDEX idx_cm_message_type ON luck_chat_message (message_type);
CREATE INDEX idx_cm_create_time ON luck_chat_message (create_time);

EXEC sp_addextendedproperty 'MS_Description', N'聊天消息表', 'SCHEMA', 'dbo', 'TABLE', 'luck_chat_message';
EXEC sp_addextendedproperty 'MS_Description', N'消息ID', 'SCHEMA', 'dbo', 'TABLE', 'luck_chat_message', 'COLUMN', 'id';
EXEC sp_addextendedproperty 'MS_Description', N'创建人', 'SCHEMA', 'dbo', 'TABLE', 'luck_chat_message', 'COLUMN', 'create_by';
EXEC sp_addextendedproperty 'MS_Description', N'创建时间', 'SCHEMA', 'dbo', 'TABLE', 'luck_chat_message', 'COLUMN', 'create_time';
EXEC sp_addextendedproperty 'MS_Description', N'更新人', 'SCHEMA', 'dbo', 'TABLE', 'luck_chat_message', 'COLUMN', 'update_by';
EXEC sp_addextendedproperty 'MS_Description', N'更新时间', 'SCHEMA', 'dbo', 'TABLE', 'luck_chat_message', 'COLUMN', 'update_time';
EXEC sp_addextendedproperty 'MS_Description', N'删除标志：0-未删除，1-已删除', 'SCHEMA', 'dbo', 'TABLE', 'luck_chat_message', 'COLUMN', 'del_flag';
EXEC sp_addextendedproperty 'MS_Description', N'会话ID', 'SCHEMA', 'dbo', 'TABLE', 'luck_chat_message', 'COLUMN', 'session_id';
EXEC sp_addextendedproperty 'MS_Description', N'角色：1-user，2-assistant，3-system，4-tool_result', 'SCHEMA', 'dbo', 'TABLE', 'luck_chat_message', 'COLUMN', 'role';
EXEC sp_addextendedproperty 'MS_Description', N'消息内容', 'SCHEMA', 'dbo', 'TABLE', 'luck_chat_message', 'COLUMN', 'content';
EXEC sp_addextendedproperty 'MS_Description', N'消息类型：1-text，2-tool_call，3-tool_result，4-error', 'SCHEMA', 'dbo', 'TABLE', 'luck_chat_message', 'COLUMN', 'message_type';
EXEC sp_addextendedproperty 'MS_Description', N'元数据', 'SCHEMA', 'dbo', 'TABLE', 'luck_chat_message', 'COLUMN', 'metadata';

GO

-- -------------------------------------------
-- 模型配置表
-- -------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'luck_model_config')
CREATE TABLE luck_model_config (
    id NVARCHAR(32) NOT NULL,
    create_by NVARCHAR(64) DEFAULT NULL,
    create_time DATETIME2 DEFAULT NULL,
    update_by NVARCHAR(64) DEFAULT NULL,
    update_time DATETIME2 DEFAULT NULL,
    del_flag INT DEFAULT 0,
    provider NVARCHAR(255) NOT NULL,
    base_url NVARCHAR(255) NOT NULL,
    api_key NVARCHAR(255) NOT NULL,
    model_name NVARCHAR(255) NOT NULL,
    config_name NVARCHAR(50) DEFAULT NULL,
    sort INT DEFAULT 0,
    temperature DECIMAL(10,2) DEFAULT 0.00,
    is_active BIT DEFAULT 0,
    context_window_tokens INT DEFAULT 128000,
    model_type NVARCHAR(20) NOT NULL DEFAULT 'CHAT',
    api_path NVARCHAR(255) DEFAULT NULL,
    proxy_enabled BIT DEFAULT 0,
    proxy_host NVARCHAR(255) DEFAULT NULL,
    proxy_port INT DEFAULT NULL,
    proxy_username NVARCHAR(255) DEFAULT NULL,
    proxy_password NVARCHAR(255) DEFAULT NULL,
    CONSTRAINT pk_luck_model_config PRIMARY KEY (id)
);

CREATE INDEX idx_mc_model_type ON luck_model_config (model_type);
CREATE INDEX idx_mc_is_active ON luck_model_config (is_active);
CREATE INDEX idx_mc_provider ON luck_model_config (provider);
CREATE INDEX idx_mc_sort ON luck_model_config (sort);
CREATE INDEX idx_mc_del_flag ON luck_model_config (del_flag);

EXEC sp_addextendedproperty 'MS_Description', N'大模型配置表', 'SCHEMA', 'dbo', 'TABLE', 'luck_model_config';
EXEC sp_addextendedproperty 'MS_Description', N'配置ID', 'SCHEMA', 'dbo', 'TABLE', 'luck_model_config', 'COLUMN', 'id';
EXEC sp_addextendedproperty 'MS_Description', N'创建人', 'SCHEMA', 'dbo', 'TABLE', 'luck_model_config', 'COLUMN', 'create_by';
EXEC sp_addextendedproperty 'MS_Description', N'创建时间', 'SCHEMA', 'dbo', 'TABLE', 'luck_model_config', 'COLUMN', 'create_time';
EXEC sp_addextendedproperty 'MS_Description', N'更新人', 'SCHEMA', 'dbo', 'TABLE', 'luck_model_config', 'COLUMN', 'update_by';
EXEC sp_addextendedproperty 'MS_Description', N'更新时间', 'SCHEMA', 'dbo', 'TABLE', 'luck_model_config', 'COLUMN', 'update_time';
EXEC sp_addextendedproperty 'MS_Description', N'删除标志：0-未删除，1-已删除', 'SCHEMA', 'dbo', 'TABLE', 'luck_model_config', 'COLUMN', 'del_flag';
EXEC sp_addextendedproperty 'MS_Description', N'厂商标识', 'SCHEMA', 'dbo', 'TABLE', 'luck_model_config', 'COLUMN', 'provider';
EXEC sp_addextendedproperty 'MS_Description', N'API基础地址', 'SCHEMA', 'dbo', 'TABLE', 'luck_model_config', 'COLUMN', 'base_url';
EXEC sp_addextendedproperty 'MS_Description', N'API密钥', 'SCHEMA', 'dbo', 'TABLE', 'luck_model_config', 'COLUMN', 'api_key';
EXEC sp_addextendedproperty 'MS_Description', N'模型名称', 'SCHEMA', 'dbo', 'TABLE', 'luck_model_config', 'COLUMN', 'model_name';
EXEC sp_addextendedproperty 'MS_Description', N'自定义名称', 'SCHEMA', 'dbo', 'TABLE', 'luck_model_config', 'COLUMN', 'config_name';
EXEC sp_addextendedproperty 'MS_Description', N'排序字段', 'SCHEMA', 'dbo', 'TABLE', 'luck_model_config', 'COLUMN', 'sort';
EXEC sp_addextendedproperty 'MS_Description', N'温度参数', 'SCHEMA', 'dbo', 'TABLE', 'luck_model_config', 'COLUMN', 'temperature';
EXEC sp_addextendedproperty 'MS_Description', N'是否激活：0-未使用，1-当前使用', 'SCHEMA', 'dbo', 'TABLE', 'luck_model_config', 'COLUMN', 'is_active';
EXEC sp_addextendedproperty 'MS_Description', N'上下文窗口大小', 'SCHEMA', 'dbo', 'TABLE', 'luck_model_config', 'COLUMN', 'context_window_tokens';
EXEC sp_addextendedproperty 'MS_Description', N'模型类型：1-CHAT，2-EMBEDDING，3-RERANK', 'SCHEMA', 'dbo', 'TABLE', 'luck_model_config', 'COLUMN', 'model_type';
EXEC sp_addextendedproperty 'MS_Description', N'API路径', 'SCHEMA', 'dbo', 'TABLE', 'luck_model_config', 'COLUMN', 'api_path';
EXEC sp_addextendedproperty 'MS_Description', N'是否启用代理：0-禁用，1-启用', 'SCHEMA', 'dbo', 'TABLE', 'luck_model_config', 'COLUMN', 'proxy_enabled';
EXEC sp_addextendedproperty 'MS_Description', N'代理主机地址', 'SCHEMA', 'dbo', 'TABLE', 'luck_model_config', 'COLUMN', 'proxy_host';
EXEC sp_addextendedproperty 'MS_Description', N'代理端口', 'SCHEMA', 'dbo', 'TABLE', 'luck_model_config', 'COLUMN', 'proxy_port';
EXEC sp_addextendedproperty 'MS_Description', N'代理用户名', 'SCHEMA', 'dbo', 'TABLE', 'luck_model_config', 'COLUMN', 'proxy_username';
EXEC sp_addextendedproperty 'MS_Description', N'代理密码', 'SCHEMA', 'dbo', 'TABLE', 'luck_model_config', 'COLUMN', 'proxy_password';

GO

-- -------------------------------------------
-- 数据源配置表
-- -------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'luck_report_datasource')
CREATE TABLE luck_report_datasource (
    id              NVARCHAR(32) NOT NULL,
    create_by       NVARCHAR(64) DEFAULT NULL,
    create_time     DATETIME2 DEFAULT SYSDATETIME(),
    update_by       NVARCHAR(64) DEFAULT NULL,
    update_time     DATETIME2 DEFAULT SYSDATETIME(),
    del_flag        BIT DEFAULT 0,
    name            NVARCHAR(100) NOT NULL,
    type            NVARCHAR(50) NOT NULL,
    host            NVARCHAR(255) NOT NULL,
    port            INT NOT NULL,
    database_name   NVARCHAR(100),
    username        NVARCHAR(100),
    password        NVARCHAR(512),
    connection_url  NVARCHAR(500),
    status          NVARCHAR(20) DEFAULT 'active',
    test_status     NVARCHAR(20) DEFAULT 'unknown',
    description     NVARCHAR(MAX),
    model_id        NVARCHAR(32) DEFAULT NULL,
    initialized_tables NVARCHAR(MAX),
    CONSTRAINT pk_luck_report_datasource PRIMARY KEY (id)
);

CREATE INDEX idx_ds_status ON luck_report_datasource (status);
CREATE INDEX idx_ds_type ON luck_report_datasource (type);

EXEC sp_addextendedproperty 'MS_Description', N'数据源配置表', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_datasource';
EXEC sp_addextendedproperty 'MS_Description', N'主键ID', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_datasource', 'COLUMN', 'id';
EXEC sp_addextendedproperty 'MS_Description', N'创建人', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_datasource', 'COLUMN', 'create_by';
EXEC sp_addextendedproperty 'MS_Description', N'创建时间', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_datasource', 'COLUMN', 'create_time';
EXEC sp_addextendedproperty 'MS_Description', N'更新人', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_datasource', 'COLUMN', 'update_by';
EXEC sp_addextendedproperty 'MS_Description', N'更新时间', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_datasource', 'COLUMN', 'update_time';
EXEC sp_addextendedproperty 'MS_Description', N'删除标志：0-未删除，1-已删除', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_datasource', 'COLUMN', 'del_flag';
EXEC sp_addextendedproperty 'MS_Description', N'数据源名称', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_datasource', 'COLUMN', 'name';
EXEC sp_addextendedproperty 'MS_Description', N'数据源类型：1-mysql，2-postgresql，3-oracle，4-dameng，5-sqlserver，6-hive', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_datasource', 'COLUMN', 'type';
EXEC sp_addextendedproperty 'MS_Description', N'主机地址', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_datasource', 'COLUMN', 'host';
EXEC sp_addextendedproperty 'MS_Description', N'端口号', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_datasource', 'COLUMN', 'port';
EXEC sp_addextendedproperty 'MS_Description', N'数据库名', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_datasource', 'COLUMN', 'database_name';
EXEC sp_addextendedproperty 'MS_Description', N'用户名', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_datasource', 'COLUMN', 'username';
EXEC sp_addextendedproperty 'MS_Description', N'密码', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_datasource', 'COLUMN', 'password';
EXEC sp_addextendedproperty 'MS_Description', N'完整连接URL', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_datasource', 'COLUMN', 'connection_url';
EXEC sp_addextendedproperty 'MS_Description', N'状态：1-active，2-inactive', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_datasource', 'COLUMN', 'status';
EXEC sp_addextendedproperty 'MS_Description', N'连接测试状态：1-success，2-failed，3-unknown', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_datasource', 'COLUMN', 'test_status';
EXEC sp_addextendedproperty 'MS_Description', N'描述', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_datasource', 'COLUMN', 'description';
EXEC sp_addextendedproperty 'MS_Description', N'嵌入模型配置ID', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_datasource', 'COLUMN', 'model_id';
EXEC sp_addextendedproperty 'MS_Description', N'已初始化的表名列表', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_datasource', 'COLUMN', 'initialized_tables';

GO

-- -------------------------------------------
-- 逻辑外键配置表
-- -------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'luck_logical_relation')
CREATE TABLE luck_logical_relation (
    id                 NVARCHAR(32) NOT NULL,
    create_by          NVARCHAR(64) DEFAULT NULL,
    create_time        DATETIME2 DEFAULT SYSDATETIME(),
    update_by          NVARCHAR(64) DEFAULT NULL,
    update_time        DATETIME2 DEFAULT SYSDATETIME(),
    del_flag           BIT DEFAULT 0,
    datasource_id      NVARCHAR(32) NOT NULL,
    source_table_name  NVARCHAR(100) NOT NULL,
    source_column_name NVARCHAR(100) NOT NULL,
    target_table_name  NVARCHAR(100) NOT NULL,
    target_column_name NVARCHAR(100) NOT NULL,
    relation_type      NVARCHAR(20),
    description        NVARCHAR(MAX),
    CONSTRAINT pk_luck_logical_relation PRIMARY KEY (id)
);

CREATE INDEX idx_lr_datasource_id ON luck_logical_relation (datasource_id);
CREATE INDEX idx_lr_source_table ON luck_logical_relation (source_table_name);
CREATE INDEX idx_lr_target_table ON luck_logical_relation (target_table_name);

EXEC sp_addextendedproperty 'MS_Description', N'逻辑外键配置表', 'SCHEMA', 'dbo', 'TABLE', 'luck_logical_relation';
EXEC sp_addextendedproperty 'MS_Description', N'主键ID', 'SCHEMA', 'dbo', 'TABLE', 'luck_logical_relation', 'COLUMN', 'id';
EXEC sp_addextendedproperty 'MS_Description', N'创建人', 'SCHEMA', 'dbo', 'TABLE', 'luck_logical_relation', 'COLUMN', 'create_by';
EXEC sp_addextendedproperty 'MS_Description', N'创建时间', 'SCHEMA', 'dbo', 'TABLE', 'luck_logical_relation', 'COLUMN', 'create_time';
EXEC sp_addextendedproperty 'MS_Description', N'更新人', 'SCHEMA', 'dbo', 'TABLE', 'luck_logical_relation', 'COLUMN', 'update_by';
EXEC sp_addextendedproperty 'MS_Description', N'更新时间', 'SCHEMA', 'dbo', 'TABLE', 'luck_logical_relation', 'COLUMN', 'update_time';
EXEC sp_addextendedproperty 'MS_Description', N'删除标志：0-未删除，1-已删除', 'SCHEMA', 'dbo', 'TABLE', 'luck_logical_relation', 'COLUMN', 'del_flag';
EXEC sp_addextendedproperty 'MS_Description', N'数据源ID', 'SCHEMA', 'dbo', 'TABLE', 'luck_logical_relation', 'COLUMN', 'datasource_id';
EXEC sp_addextendedproperty 'MS_Description', N'主表名', 'SCHEMA', 'dbo', 'TABLE', 'luck_logical_relation', 'COLUMN', 'source_table_name';
EXEC sp_addextendedproperty 'MS_Description', N'主表字段名', 'SCHEMA', 'dbo', 'TABLE', 'luck_logical_relation', 'COLUMN', 'source_column_name';
EXEC sp_addextendedproperty 'MS_Description', N'关联表名', 'SCHEMA', 'dbo', 'TABLE', 'luck_logical_relation', 'COLUMN', 'target_table_name';
EXEC sp_addextendedproperty 'MS_Description', N'关联表字段名', 'SCHEMA', 'dbo', 'TABLE', 'luck_logical_relation', 'COLUMN', 'target_column_name';
EXEC sp_addextendedproperty 'MS_Description', N'关系类型：1-1:1，2-1:N，3-N:1', 'SCHEMA', 'dbo', 'TABLE', 'luck_logical_relation', 'COLUMN', 'relation_type';
EXEC sp_addextendedproperty 'MS_Description', N'业务描述', 'SCHEMA', 'dbo', 'TABLE', 'luck_logical_relation', 'COLUMN', 'description';

GO

-- -------------------------------------------
-- 业务知识表
-- -------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'luck_business_knowledge')
CREATE TABLE luck_business_knowledge (
    id NVARCHAR(32) NOT NULL,
    create_by NVARCHAR(64) DEFAULT NULL,
    create_time DATETIME2 DEFAULT SYSDATETIME(),
    update_by NVARCHAR(64) DEFAULT NULL,
    update_time DATETIME2 DEFAULT SYSDATETIME(),
    del_flag BIT DEFAULT 0,
    title NVARCHAR(255) NOT NULL,
    type NVARCHAR(20) NOT NULL,
    question NVARCHAR(MAX) DEFAULT NULL,
    content NVARCHAR(MAX) DEFAULT NULL,
    enabled BIT DEFAULT 1,
    embedding_status NVARCHAR(20) DEFAULT 'PENDING',
    error_msg NVARCHAR(500) DEFAULT NULL,
    source_filename NVARCHAR(255) DEFAULT NULL,
    file_path NVARCHAR(500) DEFAULT NULL,
    file_size BIGINT DEFAULT NULL,
    file_type NVARCHAR(100) DEFAULT NULL,
    splitter_type NVARCHAR(20) DEFAULT 'token',
    model_id NVARCHAR(32) DEFAULT NULL,
    is_resource_cleaned BIT DEFAULT 0,
    CONSTRAINT pk_luck_business_knowledge PRIMARY KEY (id)
);

CREATE INDEX idx_bk_type ON luck_business_knowledge (type);
CREATE INDEX idx_bk_enabled ON luck_business_knowledge (enabled);
CREATE INDEX idx_bk_embedding_status ON luck_business_knowledge (embedding_status);
CREATE INDEX idx_bk_del_flag ON luck_business_knowledge (del_flag);

EXEC sp_addextendedproperty 'MS_Description', N'业务知识表', 'SCHEMA', 'dbo', 'TABLE', 'luck_business_knowledge';
EXEC sp_addextendedproperty 'MS_Description', N'主键ID', 'SCHEMA', 'dbo', 'TABLE', 'luck_business_knowledge', 'COLUMN', 'id';
EXEC sp_addextendedproperty 'MS_Description', N'创建人', 'SCHEMA', 'dbo', 'TABLE', 'luck_business_knowledge', 'COLUMN', 'create_by';
EXEC sp_addextendedproperty 'MS_Description', N'创建时间', 'SCHEMA', 'dbo', 'TABLE', 'luck_business_knowledge', 'COLUMN', 'create_time';
EXEC sp_addextendedproperty 'MS_Description', N'更新人', 'SCHEMA', 'dbo', 'TABLE', 'luck_business_knowledge', 'COLUMN', 'update_by';
EXEC sp_addextendedproperty 'MS_Description', N'更新时间', 'SCHEMA', 'dbo', 'TABLE', 'luck_business_knowledge', 'COLUMN', 'update_time';
EXEC sp_addextendedproperty 'MS_Description', N'删除标志：0-未删除，1-已删除', 'SCHEMA', 'dbo', 'TABLE', 'luck_business_knowledge', 'COLUMN', 'del_flag';
EXEC sp_addextendedproperty 'MS_Description', N'知识标题', 'SCHEMA', 'dbo', 'TABLE', 'luck_business_knowledge', 'COLUMN', 'title';
EXEC sp_addextendedproperty 'MS_Description', N'知识类型：1-DOCUMENT，2-QA，3-FAQ', 'SCHEMA', 'dbo', 'TABLE', 'luck_business_knowledge', 'COLUMN', 'type';
EXEC sp_addextendedproperty 'MS_Description', N'问题', 'SCHEMA', 'dbo', 'TABLE', 'luck_business_knowledge', 'COLUMN', 'question';
EXEC sp_addextendedproperty 'MS_Description', N'知识正文', 'SCHEMA', 'dbo', 'TABLE', 'luck_business_knowledge', 'COLUMN', 'content';
EXEC sp_addextendedproperty 'MS_Description', N'是否生效：0-不生效，1-生效', 'SCHEMA', 'dbo', 'TABLE', 'luck_business_knowledge', 'COLUMN', 'enabled';
EXEC sp_addextendedproperty 'MS_Description', N'向量化状态：1-PENDING，2-PROCESSING，3-COMPLETED，4-FAILED', 'SCHEMA', 'dbo', 'TABLE', 'luck_business_knowledge', 'COLUMN', 'embedding_status';
EXEC sp_addextendedproperty 'MS_Description', N'错误信息', 'SCHEMA', 'dbo', 'TABLE', 'luck_business_knowledge', 'COLUMN', 'error_msg';
EXEC sp_addextendedproperty 'MS_Description', N'原始文件名', 'SCHEMA', 'dbo', 'TABLE', 'luck_business_knowledge', 'COLUMN', 'source_filename';
EXEC sp_addextendedproperty 'MS_Description', N'文件存储路径', 'SCHEMA', 'dbo', 'TABLE', 'luck_business_knowledge', 'COLUMN', 'file_path';
EXEC sp_addextendedproperty 'MS_Description', N'文件大小', 'SCHEMA', 'dbo', 'TABLE', 'luck_business_knowledge', 'COLUMN', 'file_size';
EXEC sp_addextendedproperty 'MS_Description', N'文件类型', 'SCHEMA', 'dbo', 'TABLE', 'luck_business_knowledge', 'COLUMN', 'file_type';
EXEC sp_addextendedproperty 'MS_Description', N'分块策略类型：1-token，2-recursive，3-sentence，4-paragraph，5-semantic', 'SCHEMA', 'dbo', 'TABLE', 'luck_business_knowledge', 'COLUMN', 'splitter_type';
EXEC sp_addextendedproperty 'MS_Description', N'嵌入模型配置ID', 'SCHEMA', 'dbo', 'TABLE', 'luck_business_knowledge', 'COLUMN', 'model_id';
EXEC sp_addextendedproperty 'MS_Description', N'物理资源是否已清理：0-未清理，1-已清理', 'SCHEMA', 'dbo', 'TABLE', 'luck_business_knowledge', 'COLUMN', 'is_resource_cleaned';

GO

-- -------------------------------------------
-- 智能体知识表
-- -------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'luck_agent_knowledge')
CREATE TABLE luck_agent_knowledge (
    id NVARCHAR(32) NOT NULL,
    create_by NVARCHAR(64) DEFAULT NULL,
    create_time DATETIME2 DEFAULT SYSDATETIME(),
    update_by NVARCHAR(64) DEFAULT NULL,
    update_time DATETIME2 DEFAULT SYSDATETIME(),
    del_flag BIT DEFAULT 0,
    title NVARCHAR(255) NOT NULL,
    type NVARCHAR(20) NOT NULL,
    question NVARCHAR(MAX) DEFAULT NULL,
    content NVARCHAR(MAX) DEFAULT NULL,
    enabled BIT DEFAULT 1,
    embedding_status NVARCHAR(20) DEFAULT 'PENDING',
    error_msg NVARCHAR(500) DEFAULT NULL,
    source_filename NVARCHAR(255) DEFAULT NULL,
    file_path NVARCHAR(500) DEFAULT NULL,
    file_size BIGINT DEFAULT NULL,
    file_type NVARCHAR(100) DEFAULT NULL,
    splitter_type NVARCHAR(20) DEFAULT 'token',
    model_id NVARCHAR(32) DEFAULT NULL,
    is_resource_cleaned BIT DEFAULT 0,
    CONSTRAINT pk_luck_agent_knowledge PRIMARY KEY (id)
);

CREATE INDEX idx_ak_type ON luck_agent_knowledge (type);
CREATE INDEX idx_ak_enabled ON luck_agent_knowledge (enabled);
CREATE INDEX idx_ak_embedding_status ON luck_agent_knowledge (embedding_status);
CREATE INDEX idx_ak_del_flag ON luck_agent_knowledge (del_flag);

EXEC sp_addextendedproperty 'MS_Description', N'智能体知识表', 'SCHEMA', 'dbo', 'TABLE', 'luck_agent_knowledge';
EXEC sp_addextendedproperty 'MS_Description', N'主键ID', 'SCHEMA', 'dbo', 'TABLE', 'luck_agent_knowledge', 'COLUMN', 'id';
EXEC sp_addextendedproperty 'MS_Description', N'创建人', 'SCHEMA', 'dbo', 'TABLE', 'luck_agent_knowledge', 'COLUMN', 'create_by';
EXEC sp_addextendedproperty 'MS_Description', N'创建时间', 'SCHEMA', 'dbo', 'TABLE', 'luck_agent_knowledge', 'COLUMN', 'create_time';
EXEC sp_addextendedproperty 'MS_Description', N'更新人', 'SCHEMA', 'dbo', 'TABLE', 'luck_agent_knowledge', 'COLUMN', 'update_by';
EXEC sp_addextendedproperty 'MS_Description', N'更新时间', 'SCHEMA', 'dbo', 'TABLE', 'luck_agent_knowledge', 'COLUMN', 'update_time';
EXEC sp_addextendedproperty 'MS_Description', N'删除标志：0-未删除，1-已删除', 'SCHEMA', 'dbo', 'TABLE', 'luck_agent_knowledge', 'COLUMN', 'del_flag';
EXEC sp_addextendedproperty 'MS_Description', N'知识标题', 'SCHEMA', 'dbo', 'TABLE', 'luck_agent_knowledge', 'COLUMN', 'title';
EXEC sp_addextendedproperty 'MS_Description', N'知识类型：1-DOCUMENT，2-QA，3-FAQ', 'SCHEMA', 'dbo', 'TABLE', 'luck_agent_knowledge', 'COLUMN', 'type';
EXEC sp_addextendedproperty 'MS_Description', N'问题', 'SCHEMA', 'dbo', 'TABLE', 'luck_agent_knowledge', 'COLUMN', 'question';
EXEC sp_addextendedproperty 'MS_Description', N'知识正文', 'SCHEMA', 'dbo', 'TABLE', 'luck_agent_knowledge', 'COLUMN', 'content';
EXEC sp_addextendedproperty 'MS_Description', N'是否生效：0-不生效，1-生效', 'SCHEMA', 'dbo', 'TABLE', 'luck_agent_knowledge', 'COLUMN', 'enabled';
EXEC sp_addextendedproperty 'MS_Description', N'向量化状态：1-PENDING，2-PROCESSING，3-COMPLETED，4-FAILED', 'SCHEMA', 'dbo', 'TABLE', 'luck_agent_knowledge', 'COLUMN', 'embedding_status';
EXEC sp_addextendedproperty 'MS_Description', N'错误信息', 'SCHEMA', 'dbo', 'TABLE', 'luck_agent_knowledge', 'COLUMN', 'error_msg';
EXEC sp_addextendedproperty 'MS_Description', N'原始文件名', 'SCHEMA', 'dbo', 'TABLE', 'luck_agent_knowledge', 'COLUMN', 'source_filename';
EXEC sp_addextendedproperty 'MS_Description', N'文件存储路径', 'SCHEMA', 'dbo', 'TABLE', 'luck_agent_knowledge', 'COLUMN', 'file_path';
EXEC sp_addextendedproperty 'MS_Description', N'文件大小', 'SCHEMA', 'dbo', 'TABLE', 'luck_agent_knowledge', 'COLUMN', 'file_size';
EXEC sp_addextendedproperty 'MS_Description', N'文件类型', 'SCHEMA', 'dbo', 'TABLE', 'luck_agent_knowledge', 'COLUMN', 'file_type';
EXEC sp_addextendedproperty 'MS_Description', N'分块策略类型：1-token，2-recursive，3-sentence，4-paragraph，5-semantic', 'SCHEMA', 'dbo', 'TABLE', 'luck_agent_knowledge', 'COLUMN', 'splitter_type';
EXEC sp_addextendedproperty 'MS_Description', N'嵌入模型配置ID', 'SCHEMA', 'dbo', 'TABLE', 'luck_agent_knowledge', 'COLUMN', 'model_id';
EXEC sp_addextendedproperty 'MS_Description', N'物理资源是否已清理：0-未清理，1-已清理', 'SCHEMA', 'dbo', 'TABLE', 'luck_agent_knowledge', 'COLUMN', 'is_resource_cleaned';

GO

-- -------------------------------------------
-- 报表文件表
-- -------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'luck_report_template')
CREATE TABLE luck_report_template (
    id NVARCHAR(32) NOT NULL,
    create_by NVARCHAR(64) DEFAULT NULL,
    create_time DATETIME2 DEFAULT SYSDATETIME(),
    update_by NVARCHAR(64) DEFAULT NULL,
    update_time DATETIME2 DEFAULT SYSDATETIME(),
    del_flag BIT DEFAULT 0,
    title NVARCHAR(255) NOT NULL,
    template NVARCHAR(MAX) DEFAULT NULL,
    CONSTRAINT pk_luck_report_template PRIMARY KEY (id)
);

CREATE INDEX idx_rt_del_flag ON luck_report_template (del_flag);
CREATE INDEX idx_rt_title ON luck_report_template (title);
CREATE INDEX idx_rt_create_time ON luck_report_template (create_time);

EXEC sp_addextendedproperty 'MS_Description', N'报表文件表', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_template';
EXEC sp_addextendedproperty 'MS_Description', N'主键ID', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_template', 'COLUMN', 'id';
EXEC sp_addextendedproperty 'MS_Description', N'创建人', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_template', 'COLUMN', 'create_by';
EXEC sp_addextendedproperty 'MS_Description', N'创建时间', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_template', 'COLUMN', 'create_time';
EXEC sp_addextendedproperty 'MS_Description', N'更新人', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_template', 'COLUMN', 'update_by';
EXEC sp_addextendedproperty 'MS_Description', N'更新时间', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_template', 'COLUMN', 'update_time';
EXEC sp_addextendedproperty 'MS_Description', N'删除标志：0-未删除，1-已删除', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_template', 'COLUMN', 'del_flag';
EXEC sp_addextendedproperty 'MS_Description', N'报表标题', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_template', 'COLUMN', 'title';
EXEC sp_addextendedproperty 'MS_Description', N'报表模板内容', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_template', 'COLUMN', 'template';

GO

-- -------------------------------------------
-- 角色与报表绑定关系表
-- file_path 示例：file:test.ureport.xml / db:1 / classpath:foo.ureport.xml / *
-- -------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'luck_report_role')
CREATE TABLE luck_report_role (
    role_code NVARCHAR(128) NOT NULL,
    file_path NVARCHAR(512) NOT NULL,
    CONSTRAINT pk_luck_report_role PRIMARY KEY (role_code, file_path)
);

CREATE INDEX idx_lrr_file_path ON luck_report_role (file_path);

EXEC sp_addextendedproperty 'MS_Description', N'角色与报表绑定关系表', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_role';
EXEC sp_addextendedproperty 'MS_Description', N'角色编码', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_role', 'COLUMN', 'role_code';
EXEC sp_addextendedproperty 'MS_Description', N'报表完整路径', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_role', 'COLUMN', 'file_path';

GO

-- -------------------------------------------
-- 公共数据集表
-- -------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'luck_report_dataset')
CREATE TABLE luck_report_dataset (
    id              NVARCHAR(32) NOT NULL,
    create_by       NVARCHAR(64) DEFAULT NULL,
    create_time     DATETIME2 DEFAULT SYSDATETIME(),
    update_by       NVARCHAR(64) DEFAULT NULL,
    update_time     DATETIME2 DEFAULT SYSDATETIME(),
    del_flag        BIT DEFAULT 0,
    name            NVARCHAR(100) NOT NULL,
    type            NVARCHAR(20) NOT NULL,
    datasource_id   NVARCHAR(32) DEFAULT NULL,
    sql_content     NVARCHAR(MAX),
    json_content    NVARCHAR(MAX),
    parameters      NVARCHAR(MAX),
    fields          NVARCHAR(MAX),
    description     NVARCHAR(500),
    status          NVARCHAR(20) NOT NULL DEFAULT 'active',
    CONSTRAINT pk_luck_report_dataset PRIMARY KEY (id),
    CONSTRAINT uk_pd_name UNIQUE (name)
);

CREATE INDEX idx_pd_datasource_id ON luck_report_dataset (datasource_id);
CREATE INDEX idx_pd_status ON luck_report_dataset (status);

EXEC sp_addextendedproperty 'MS_Description', N'公共数据集表', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_dataset';
EXEC sp_addextendedproperty 'MS_Description', N'主键ID', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_dataset', 'COLUMN', 'id';
EXEC sp_addextendedproperty 'MS_Description', N'创建人', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_dataset', 'COLUMN', 'create_by';
EXEC sp_addextendedproperty 'MS_Description', N'创建时间', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_dataset', 'COLUMN', 'create_time';
EXEC sp_addextendedproperty 'MS_Description', N'更新人', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_dataset', 'COLUMN', 'update_by';
EXEC sp_addextendedproperty 'MS_Description', N'更新时间', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_dataset', 'COLUMN', 'update_time';
EXEC sp_addextendedproperty 'MS_Description', N'删除标志：0-未删除，1-已删除', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_dataset', 'COLUMN', 'del_flag';
EXEC sp_addextendedproperty 'MS_Description', N'数据集名称', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_dataset', 'COLUMN', 'name';
EXEC sp_addextendedproperty 'MS_Description', N'类型：1-sql，2-json', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_dataset', 'COLUMN', 'type';
EXEC sp_addextendedproperty 'MS_Description', N'绑定的公共数据源ID', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_dataset', 'COLUMN', 'datasource_id';
EXEC sp_addextendedproperty 'MS_Description', N'SQL语句', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_dataset', 'COLUMN', 'sql_content';
EXEC sp_addextendedproperty 'MS_Description', N'JSON数组内容', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_dataset', 'COLUMN', 'json_content';
EXEC sp_addextendedproperty 'MS_Description', N'SQL参数定义', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_dataset', 'COLUMN', 'parameters';
EXEC sp_addextendedproperty 'MS_Description', N'字段列表', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_dataset', 'COLUMN', 'fields';
EXEC sp_addextendedproperty 'MS_Description', N'描述', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_dataset', 'COLUMN', 'description';
EXEC sp_addextendedproperty 'MS_Description', N'状态：1-active，2-inactive', 'SCHEMA', 'dbo', 'TABLE', 'luck_report_dataset', 'COLUMN', 'status';

GO
