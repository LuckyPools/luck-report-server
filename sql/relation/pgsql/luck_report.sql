-- =============================================
-- Luck Report 数据库初始化脚本
-- 数据库类型：PostgreSQL
-- 说明：与 MySQL 版本表结构对齐（含 model_config.api_path、知识库 content 大字段等）
-- =============================================

-- -------------------------------------------
-- 会话表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS luck_chat_session (
    id VARCHAR(36) NOT NULL,
    create_by VARCHAR(64) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag SMALLINT DEFAULT 0,
    title VARCHAR(255) DEFAULT '新对话',
    status VARCHAR(50) DEFAULT 'active',
    is_pinned SMALLINT DEFAULT 0,
    user_id VARCHAR(64) DEFAULT NULL,
    CONSTRAINT pk_luck_chat_session PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_cs_user_id ON luck_chat_session (user_id);
CREATE INDEX IF NOT EXISTS idx_cs_status ON luck_chat_session (status);
CREATE INDEX IF NOT EXISTS idx_cs_is_pinned ON luck_chat_session (is_pinned);
CREATE INDEX IF NOT EXISTS idx_cs_create_time ON luck_chat_session (create_time);

COMMENT ON TABLE luck_chat_session IS '聊天会话表';
COMMENT ON COLUMN luck_chat_session.id IS '会话ID（UUID）';
COMMENT ON COLUMN luck_chat_session.create_by IS '创建人';
COMMENT ON COLUMN luck_chat_session.create_time IS '创建时间';
COMMENT ON COLUMN luck_chat_session.update_by IS '更新人';
COMMENT ON COLUMN luck_chat_session.update_time IS '更新时间';
COMMENT ON COLUMN luck_chat_session.del_flag IS '删除标志(0-未删除,1-已删除)';
COMMENT ON COLUMN luck_chat_session.title IS '会话标题';
COMMENT ON COLUMN luck_chat_session.status IS '状态：active-活跃，archived-归档';
COMMENT ON COLUMN luck_chat_session.is_pinned IS '是否置顶：0-否，1-是';
COMMENT ON COLUMN luck_chat_session.user_id IS '用户ID（字符串形式，兼容数字主键、UUID、工号等）';

-- -------------------------------------------
-- 消息表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS luck_chat_message (
    id VARCHAR(32) NOT NULL,
    create_by VARCHAR(64) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag SMALLINT DEFAULT 0,
    session_id VARCHAR(36) NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT,
    message_type VARCHAR(50) DEFAULT 'text',
    metadata JSONB DEFAULT NULL,
    CONSTRAINT pk_luck_chat_message PRIMARY KEY (id),
    CONSTRAINT fk_luck_chat_message_session FOREIGN KEY (session_id) REFERENCES luck_chat_session (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_cm_session_id ON luck_chat_message (session_id);
CREATE INDEX IF NOT EXISTS idx_cm_role ON luck_chat_message (role);
CREATE INDEX IF NOT EXISTS idx_cm_message_type ON luck_chat_message (message_type);
CREATE INDEX IF NOT EXISTS idx_cm_create_time ON luck_chat_message (create_time);

COMMENT ON TABLE luck_chat_message IS '聊天消息表';
COMMENT ON COLUMN luck_chat_message.id IS '消息ID（Snowflake）';
COMMENT ON COLUMN luck_chat_message.create_by IS '创建人';
COMMENT ON COLUMN luck_chat_message.create_time IS '创建时间';
COMMENT ON COLUMN luck_chat_message.update_by IS '更新人';
COMMENT ON COLUMN luck_chat_message.update_time IS '更新时间';
COMMENT ON COLUMN luck_chat_message.del_flag IS '删除标志(0-未删除,1-已删除)';
COMMENT ON COLUMN luck_chat_message.session_id IS '会话ID';
COMMENT ON COLUMN luck_chat_message.role IS '角色：user-用户，assistant-助手，system-系统，tool_result-工具结果';
COMMENT ON COLUMN luck_chat_message.content IS '消息内容';
COMMENT ON COLUMN luck_chat_message.message_type IS '消息类型：text-文本，tool_call-工具调用，tool_result-工具结果，error-错误';
COMMENT ON COLUMN luck_chat_message.metadata IS '元数据（JSONB格式，存储tool_calls数组或tool_call_id等）';

-- -------------------------------------------
-- 模型配置表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS luck_model_config (
    id VARCHAR(32) NOT NULL,
    create_by VARCHAR(64) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT NULL,
    update_by VARCHAR(64) DEFAULT NULL,
    update_time TIMESTAMP DEFAULT NULL,
    del_flag INT DEFAULT 0,
    provider VARCHAR(255) NOT NULL,
    base_url VARCHAR(255) NOT NULL,
    api_key VARCHAR(255) NOT NULL,
    model_name VARCHAR(255) NOT NULL,
    config_name VARCHAR(50) DEFAULT NULL,
    sort INT DEFAULT 0,
    temperature DECIMAL(10,2) DEFAULT 0.00 CHECK (temperature >= 0),
    is_active SMALLINT DEFAULT 0,
    context_window_tokens INT DEFAULT 128000,
    model_type VARCHAR(20) NOT NULL DEFAULT 'CHAT',
    api_path VARCHAR(255) DEFAULT NULL,
    proxy_enabled SMALLINT DEFAULT 0,
    proxy_host VARCHAR(255) DEFAULT NULL,
    proxy_port INT DEFAULT NULL,
    proxy_username VARCHAR(255) DEFAULT NULL,
    proxy_password VARCHAR(255) DEFAULT NULL,
    CONSTRAINT pk_luck_model_config PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_mc_model_type ON luck_model_config (model_type);
CREATE INDEX IF NOT EXISTS idx_mc_is_active ON luck_model_config (is_active);
CREATE INDEX IF NOT EXISTS idx_mc_provider ON luck_model_config (provider);
CREATE INDEX IF NOT EXISTS idx_mc_sort ON luck_model_config (sort);
CREATE INDEX IF NOT EXISTS idx_mc_del_flag ON luck_model_config (del_flag);

COMMENT ON TABLE luck_model_config IS '大模型配置表';
COMMENT ON COLUMN luck_model_config.id IS '配置ID（Snowflake）';
COMMENT ON COLUMN luck_model_config.create_by IS '创建人';
COMMENT ON COLUMN luck_model_config.create_time IS '创建时间';
COMMENT ON COLUMN luck_model_config.update_by IS '更新人';
COMMENT ON COLUMN luck_model_config.update_time IS '更新时间';
COMMENT ON COLUMN luck_model_config.del_flag IS '删除标志(0-未删除,1-已删除)';
COMMENT ON COLUMN luck_model_config.provider IS '厂商标识(如 alibaba、openai、deepseek),方便前端展示回显';
COMMENT ON COLUMN luck_model_config.base_url IS 'API基础地址(如 https://dashscope.aliyuncs.com/compatible-mode/v1)';
COMMENT ON COLUMN luck_model_config.api_key IS 'API密钥';
COMMENT ON COLUMN luck_model_config.model_name IS '模型名称(如 qwen3.5-plus、text-embedding-v3)';
COMMENT ON COLUMN luck_model_config.config_name IS '自定义名称,最多50个字';
COMMENT ON COLUMN luck_model_config.sort IS '排序字段,数字越小越靠前';
COMMENT ON COLUMN luck_model_config.temperature IS '温度参数,控制生成随机性,0~1';
COMMENT ON COLUMN luck_model_config.is_active IS '是否激活:true-当前使用,false-未使用';
COMMENT ON COLUMN luck_model_config.context_window_tokens IS '上下文窗口大小（token），用于对话压缩判断';
COMMENT ON COLUMN luck_model_config.model_type IS '模型类型(CHAT/EMBEDDING/RERANK)';
COMMENT ON COLUMN luck_model_config.api_path IS 'API路径，拼在base_url后；空则按类型使用默认路径';
COMMENT ON COLUMN luck_model_config.proxy_enabled IS '是否启用代理:0-禁用,1-启用';
COMMENT ON COLUMN luck_model_config.proxy_host IS '代理主机地址';
COMMENT ON COLUMN luck_model_config.proxy_port IS '代理端口';
COMMENT ON COLUMN luck_model_config.proxy_username IS '代理用户名(可选)';
COMMENT ON COLUMN luck_model_config.proxy_password IS '代理密码(可选)';

-- -------------------------------------------
-- 数据源配置表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS luck_report_datasource (
    id VARCHAR(32) NOT NULL,
    create_by VARCHAR(64) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag SMALLINT DEFAULT 0,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    host VARCHAR(255) NOT NULL,
    port INT NOT NULL,
    database_name VARCHAR(100),
    username VARCHAR(100),
    password VARCHAR(512),
    connection_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'active',
    test_status VARCHAR(20) DEFAULT 'unknown',
    description TEXT,
    model_id VARCHAR(32) DEFAULT NULL,
    initialized_tables TEXT,
    CONSTRAINT pk_luck_report_datasource PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_ds_status ON luck_report_datasource (status);
CREATE INDEX IF NOT EXISTS idx_ds_type ON luck_report_datasource (type);

COMMENT ON TABLE luck_report_datasource IS '数据源配置表';
COMMENT ON COLUMN luck_report_datasource.id IS '主键ID（Snowflake）';
COMMENT ON COLUMN luck_report_datasource.create_by IS '创建人';
COMMENT ON COLUMN luck_report_datasource.create_time IS '创建时间';
COMMENT ON COLUMN luck_report_datasource.update_by IS '更新人';
COMMENT ON COLUMN luck_report_datasource.update_time IS '更新时间';
COMMENT ON COLUMN luck_report_datasource.del_flag IS '删除标志(0-未删除,1-已删除)';
COMMENT ON COLUMN luck_report_datasource.name IS '数据源名称';
COMMENT ON COLUMN luck_report_datasource.type IS '数据源类型：mysql/postgresql/oracle/dameng/sqlserver/hive';
COMMENT ON COLUMN luck_report_datasource.host IS '主机地址';
COMMENT ON COLUMN luck_report_datasource.port IS '端口号';
COMMENT ON COLUMN luck_report_datasource.database_name IS '数据库名';
COMMENT ON COLUMN luck_report_datasource.username IS '用户名';
COMMENT ON COLUMN luck_report_datasource.password IS '密码（__LUCK__ 密文或兼容明文）';
COMMENT ON COLUMN luck_report_datasource.connection_url IS '完整连接URL';
COMMENT ON COLUMN luck_report_datasource.status IS '状态：active/inactive';
COMMENT ON COLUMN luck_report_datasource.test_status IS '连接测试状态：success/failed/unknown';
COMMENT ON COLUMN luck_report_datasource.description IS '描述';
COMMENT ON COLUMN luck_report_datasource.model_id IS '嵌入模型配置ID（Snowflake）';
COMMENT ON COLUMN luck_report_datasource.initialized_tables IS '已初始化的表名列表（JSON格式存储，如["table1","table2"]）';

-- -------------------------------------------
-- 逻辑外键配置表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS luck_logical_relation (
    id VARCHAR(32) NOT NULL,
    create_by VARCHAR(64) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag SMALLINT DEFAULT 0,
    datasource_id VARCHAR(32) NOT NULL,
    source_table_name VARCHAR(100) NOT NULL,
    source_column_name VARCHAR(100) NOT NULL,
    target_table_name VARCHAR(100) NOT NULL,
    target_column_name VARCHAR(100) NOT NULL,
    relation_type VARCHAR(20),
    description TEXT,
    CONSTRAINT pk_luck_logical_relation PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_lr_datasource_id ON luck_logical_relation (datasource_id);
CREATE INDEX IF NOT EXISTS idx_lr_source_table ON luck_logical_relation (source_table_name);
CREATE INDEX IF NOT EXISTS idx_lr_target_table ON luck_logical_relation (target_table_name);

COMMENT ON TABLE luck_logical_relation IS '逻辑外键配置表';
COMMENT ON COLUMN luck_logical_relation.id IS '主键ID（Snowflake）';
COMMENT ON COLUMN luck_logical_relation.create_by IS '创建人';
COMMENT ON COLUMN luck_logical_relation.create_time IS '创建时间';
COMMENT ON COLUMN luck_logical_relation.update_by IS '更新人';
COMMENT ON COLUMN luck_logical_relation.update_time IS '更新时间';
COMMENT ON COLUMN luck_logical_relation.del_flag IS '删除标志(0-未删除,1-已删除)';
COMMENT ON COLUMN luck_logical_relation.datasource_id IS '数据源ID';
COMMENT ON COLUMN luck_logical_relation.source_table_name IS '主表名';
COMMENT ON COLUMN luck_logical_relation.source_column_name IS '主表字段名';
COMMENT ON COLUMN luck_logical_relation.target_table_name IS '关联表名';
COMMENT ON COLUMN luck_logical_relation.target_column_name IS '关联表字段名';
COMMENT ON COLUMN luck_logical_relation.relation_type IS '关系类型：1:1/1:N/N:1';
COMMENT ON COLUMN luck_logical_relation.description IS '业务描述';

-- -------------------------------------------
-- 业务知识表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS luck_business_knowledge (
    id VARCHAR(32) NOT NULL,
    create_by VARCHAR(64) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag SMALLINT DEFAULT 0,
    title VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    question TEXT DEFAULT NULL,
    content TEXT DEFAULT NULL,
    enabled SMALLINT DEFAULT 1,
    embedding_status VARCHAR(20) DEFAULT 'PENDING',
    error_msg VARCHAR(500) DEFAULT NULL,
    source_filename VARCHAR(255) DEFAULT NULL,
    file_path VARCHAR(500) DEFAULT NULL,
    file_size BIGINT DEFAULT NULL,
    file_type VARCHAR(100) DEFAULT NULL,
    splitter_type VARCHAR(20) DEFAULT 'token',
    model_id VARCHAR(32) DEFAULT NULL,
    is_resource_cleaned SMALLINT DEFAULT 0,
    CONSTRAINT pk_luck_business_knowledge PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_bk_type ON luck_business_knowledge (type);
CREATE INDEX IF NOT EXISTS idx_bk_enabled ON luck_business_knowledge (enabled);
CREATE INDEX IF NOT EXISTS idx_bk_embedding_status ON luck_business_knowledge (embedding_status);
CREATE INDEX IF NOT EXISTS idx_bk_del_flag ON luck_business_knowledge (del_flag);

COMMENT ON TABLE luck_business_knowledge IS '业务知识表';
COMMENT ON COLUMN luck_business_knowledge.id IS '主键ID（Snowflake）';
COMMENT ON COLUMN luck_business_knowledge.create_by IS '创建人';
COMMENT ON COLUMN luck_business_knowledge.create_time IS '创建时间';
COMMENT ON COLUMN luck_business_knowledge.update_by IS '更新人';
COMMENT ON COLUMN luck_business_knowledge.update_time IS '更新时间';
COMMENT ON COLUMN luck_business_knowledge.del_flag IS '删除标志(0-未删除,1-已删除)';
COMMENT ON COLUMN luck_business_knowledge.title IS '知识标题';
COMMENT ON COLUMN luck_business_knowledge.type IS '知识类型：DOCUMENT-文档，QA-问答对，FAQ-常见问题';
COMMENT ON COLUMN luck_business_knowledge.question IS '问题（FAQ和QA类型时使用）';
COMMENT ON COLUMN luck_business_knowledge.content IS '知识正文（DOCUMENT 存解析全文；QA/FAQ 存答案）';
COMMENT ON COLUMN luck_business_knowledge.enabled IS '是否生效（0:不生效, 1:生效）';
COMMENT ON COLUMN luck_business_knowledge.embedding_status IS '向量化状态：PENDING待处理，PROCESSING处理中，COMPLETED已完成，FAILED失败';
COMMENT ON COLUMN luck_business_knowledge.error_msg IS '操作失败的错误信息';
COMMENT ON COLUMN luck_business_knowledge.source_filename IS '原始文件名';
COMMENT ON COLUMN luck_business_knowledge.file_path IS '文件存储路径';
COMMENT ON COLUMN luck_business_knowledge.file_size IS '文件大小（字节）';
COMMENT ON COLUMN luck_business_knowledge.file_type IS '文件类型';
COMMENT ON COLUMN luck_business_knowledge.splitter_type IS '分块策略类型：token, recursive, sentence, paragraph, semantic';
COMMENT ON COLUMN luck_business_knowledge.model_id IS '嵌入模型配置ID（Snowflake）';
COMMENT ON COLUMN luck_business_knowledge.is_resource_cleaned IS '物理资源是否已清理（0:未清理, 1:已清理）';

-- -------------------------------------------
-- 智能体知识表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS luck_agent_knowledge (
    id VARCHAR(32) NOT NULL,
    create_by VARCHAR(64) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag SMALLINT DEFAULT 0,
    title VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    question TEXT DEFAULT NULL,
    content TEXT DEFAULT NULL,
    enabled SMALLINT DEFAULT 1,
    embedding_status VARCHAR(20) DEFAULT 'PENDING',
    error_msg VARCHAR(500) DEFAULT NULL,
    source_filename VARCHAR(255) DEFAULT NULL,
    file_path VARCHAR(500) DEFAULT NULL,
    file_size BIGINT DEFAULT NULL,
    file_type VARCHAR(100) DEFAULT NULL,
    splitter_type VARCHAR(20) DEFAULT 'token',
    model_id VARCHAR(32) DEFAULT NULL,
    is_resource_cleaned SMALLINT DEFAULT 0,
    CONSTRAINT pk_luck_agent_knowledge PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_ak_type ON luck_agent_knowledge (type);
CREATE INDEX IF NOT EXISTS idx_ak_enabled ON luck_agent_knowledge (enabled);
CREATE INDEX IF NOT EXISTS idx_ak_embedding_status ON luck_agent_knowledge (embedding_status);
CREATE INDEX IF NOT EXISTS idx_ak_del_flag ON luck_agent_knowledge (del_flag);

COMMENT ON TABLE luck_agent_knowledge IS '智能体知识表';
COMMENT ON COLUMN luck_agent_knowledge.id IS '主键ID（Snowflake）';
COMMENT ON COLUMN luck_agent_knowledge.create_by IS '创建人';
COMMENT ON COLUMN luck_agent_knowledge.create_time IS '创建时间';
COMMENT ON COLUMN luck_agent_knowledge.update_by IS '更新人';
COMMENT ON COLUMN luck_agent_knowledge.update_time IS '更新时间';
COMMENT ON COLUMN luck_agent_knowledge.del_flag IS '删除标志(0-未删除,1-已删除)';
COMMENT ON COLUMN luck_agent_knowledge.title IS '知识标题';
COMMENT ON COLUMN luck_agent_knowledge.type IS '知识类型：DOCUMENT-文档，QA-问答对，FAQ-常见问题';
COMMENT ON COLUMN luck_agent_knowledge.question IS '问题（FAQ和QA类型时使用）';
COMMENT ON COLUMN luck_agent_knowledge.content IS '知识正文（DOCUMENT 存解析全文；QA/FAQ 存答案）';
COMMENT ON COLUMN luck_agent_knowledge.enabled IS '是否生效（0:不生效, 1:生效）';
COMMENT ON COLUMN luck_agent_knowledge.embedding_status IS '向量化状态：PENDING待处理，PROCESSING处理中，COMPLETED已完成，FAILED失败';
COMMENT ON COLUMN luck_agent_knowledge.error_msg IS '操作失败的错误信息';
COMMENT ON COLUMN luck_agent_knowledge.source_filename IS '原始文件名';
COMMENT ON COLUMN luck_agent_knowledge.file_path IS '文件存储路径';
COMMENT ON COLUMN luck_agent_knowledge.file_size IS '文件大小（字节）';
COMMENT ON COLUMN luck_agent_knowledge.file_type IS '文件类型';
COMMENT ON COLUMN luck_agent_knowledge.splitter_type IS '分块策略类型：token, recursive, sentence, paragraph, semantic';
COMMENT ON COLUMN luck_agent_knowledge.model_id IS '嵌入模型配置ID（Snowflake）';
COMMENT ON COLUMN luck_agent_knowledge.is_resource_cleaned IS '物理资源是否已清理（0:未清理, 1:已清理）';

-- -------------------------------------------
-- 报表文件表（数据库存储）
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS luck_report_template (
    id VARCHAR(32) NOT NULL,
    create_by VARCHAR(64) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag SMALLINT DEFAULT 0,
    title VARCHAR(255) NOT NULL,
    template TEXT DEFAULT NULL,
    CONSTRAINT pk_luck_report_template PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_rt_del_flag ON luck_report_template (del_flag);
CREATE INDEX IF NOT EXISTS idx_rt_title ON luck_report_template (title);
CREATE INDEX IF NOT EXISTS idx_rt_create_time ON luck_report_template (create_time);

COMMENT ON TABLE luck_report_template IS '报表文件表（数据库存储）';
COMMENT ON COLUMN luck_report_template.id IS '主键ID（Snowflake）';
COMMENT ON COLUMN luck_report_template.create_by IS '创建人';
COMMENT ON COLUMN luck_report_template.create_time IS '创建时间';
COMMENT ON COLUMN luck_report_template.update_by IS '更新人';
COMMENT ON COLUMN luck_report_template.update_time IS '更新时间';
COMMENT ON COLUMN luck_report_template.del_flag IS '删除标志(0-未删除,1-已删除)';
COMMENT ON COLUMN luck_report_template.title IS '报表标题';
COMMENT ON COLUMN luck_report_template.template IS '报表模板内容（XML）';

-- -------------------------------------------
-- 角色与报表绑定关系表（精简版）
-- file_path 存储"provider 前缀 + 报表路径"的完整字符串：
--   - 'file:test.ureport.xml'  文件系统存储
--   - 'db:1'                   数据库存储（db: provider 用主键 id 作为路径）
--   - 'classpath:foo.ureport.xml'
--   - '*'                      通配：表示该角色可访问所有报表
-- 说明：与 postgresql Mapper 一致，主键为 (role_code, file_path)
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS luck_report_role (
    role_code VARCHAR(128) NOT NULL,
    file_path VARCHAR(512) NOT NULL,
    CONSTRAINT pk_luck_report_role PRIMARY KEY (role_code, file_path)
);

CREATE INDEX IF NOT EXISTS idx_lrr_file_path ON luck_report_role (file_path);

COMMENT ON TABLE luck_report_role IS '角色与报表绑定关系表（精简版）';
COMMENT ON COLUMN luck_report_role.role_code IS '角色编码（第三方系统角色 ID）';
COMMENT ON COLUMN luck_report_role.file_path IS '报表完整路径：<provider>:<path>，* 代表全部';

-- -------------------------------------------
-- 公共数据集表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS luck_report_dataset (
    id VARCHAR(32) NOT NULL,
    create_by VARCHAR(64) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    del_flag SMALLINT DEFAULT 0,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    datasource_id VARCHAR(32) DEFAULT NULL,
    sql_content TEXT,
    json_content TEXT,
    parameters TEXT,
    fields TEXT,
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    CONSTRAINT pk_luck_report_dataset PRIMARY KEY (id),
    CONSTRAINT uk_pd_name UNIQUE (name)
);

CREATE INDEX IF NOT EXISTS idx_pd_datasource_id ON luck_report_dataset (datasource_id);
CREATE INDEX IF NOT EXISTS idx_pd_status ON luck_report_dataset (status);

COMMENT ON TABLE luck_report_dataset IS '公共数据集表';
COMMENT ON COLUMN luck_report_dataset.id IS '主键ID（Snowflake）';
COMMENT ON COLUMN luck_report_dataset.create_by IS '创建人';
COMMENT ON COLUMN luck_report_dataset.create_time IS '创建时间';
COMMENT ON COLUMN luck_report_dataset.update_by IS '更新人';
COMMENT ON COLUMN luck_report_dataset.update_time IS '更新时间';
COMMENT ON COLUMN luck_report_dataset.del_flag IS '删除标志(0-未删除,1-已删除)';
COMMENT ON COLUMN luck_report_dataset.name IS '数据集名称（全局唯一）';
COMMENT ON COLUMN luck_report_dataset.type IS '类型：sql-SQL数据集，json-JSON数据集';
COMMENT ON COLUMN luck_report_dataset.datasource_id IS '绑定的公共数据源ID（sql类型必填，json类型为空）';
COMMENT ON COLUMN luck_report_dataset.sql_content IS 'SQL语句（sql类型使用）';
COMMENT ON COLUMN luck_report_dataset.json_content IS 'JSON数组内容（json类型使用）';
COMMENT ON COLUMN luck_report_dataset.parameters IS 'SQL参数定义JSON数组';
COMMENT ON COLUMN luck_report_dataset.fields IS '字段列表JSON数组，可为空';
COMMENT ON COLUMN luck_report_dataset.description IS '描述';
COMMENT ON COLUMN luck_report_dataset.status IS '状态：active-启用，inactive-禁用';
