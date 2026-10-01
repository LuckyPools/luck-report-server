-- =============================================
-- Luck Report 数据库初始化脚本
-- 数据库类型：PostgreSQL
-- 约定：列不设 DEFAULT；审计时间 / 开关 / 枚举等一律由应用写入
-- 日期类型统一：TIMESTAMP
-- =============================================

-- -------------------------------------------
-- 会话表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS luck_chat_session (
    id VARCHAR(36) NOT NULL,
    create_by VARCHAR(128),
    create_time TIMESTAMP,
    update_by VARCHAR(128),
    update_time TIMESTAMP,
    del_flag SMALLINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    is_pinned SMALLINT NOT NULL,
    user_id VARCHAR(128),
    CONSTRAINT pk_luck_chat_session PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_user_id_del_pinned_update ON luck_chat_session (user_id, del_flag, is_pinned, update_time);

COMMENT ON TABLE luck_chat_session IS '聊天会话表';
COMMENT ON COLUMN luck_chat_session.id IS '会话ID';
COMMENT ON COLUMN luck_chat_session.create_by IS '创建人';
COMMENT ON COLUMN luck_chat_session.create_time IS '创建时间';
COMMENT ON COLUMN luck_chat_session.update_by IS '更新人';
COMMENT ON COLUMN luck_chat_session.update_time IS '更新时间';
COMMENT ON COLUMN luck_chat_session.del_flag IS '删除标志：0-未删除，1-已删除';
COMMENT ON COLUMN luck_chat_session.title IS '会话标题';
COMMENT ON COLUMN luck_chat_session.is_pinned IS '是否置顶：0-否，1-是';
COMMENT ON COLUMN luck_chat_session.user_id IS '用户ID';

-- -------------------------------------------
-- 消息表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS luck_chat_message (
    id VARCHAR(32) NOT NULL,
    create_by VARCHAR(128),
    create_time TIMESTAMP,
    update_by VARCHAR(128),
    update_time TIMESTAMP,
    del_flag SMALLINT NOT NULL,
    session_id VARCHAR(36) NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT,
    message_type VARCHAR(50) NOT NULL,
    metadata JSONB,
    CONSTRAINT pk_luck_chat_message PRIMARY KEY (id),
    CONSTRAINT fk_luck_chat_message_session FOREIGN KEY (session_id) REFERENCES luck_chat_session (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_session_del_create ON luck_chat_message (session_id, del_flag, create_time);

COMMENT ON TABLE luck_chat_message IS '聊天消息表';
COMMENT ON COLUMN luck_chat_message.id IS '消息ID';
COMMENT ON COLUMN luck_chat_message.create_by IS '创建人';
COMMENT ON COLUMN luck_chat_message.create_time IS '创建时间';
COMMENT ON COLUMN luck_chat_message.update_by IS '更新人';
COMMENT ON COLUMN luck_chat_message.update_time IS '更新时间';
COMMENT ON COLUMN luck_chat_message.del_flag IS '删除标志：0-未删除，1-已删除';
COMMENT ON COLUMN luck_chat_message.session_id IS '会话ID';
COMMENT ON COLUMN luck_chat_message.role IS '角色：user / assistant / system / tool_result';
COMMENT ON COLUMN luck_chat_message.content IS '消息内容';
COMMENT ON COLUMN luck_chat_message.message_type IS '消息类型：text / tool_call / tool_result / error';
COMMENT ON COLUMN luck_chat_message.metadata IS '元数据';

-- -------------------------------------------
-- 模型配置表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS luck_model_config (
    id VARCHAR(32) NOT NULL,
    create_by VARCHAR(128),
    create_time TIMESTAMP,
    update_by VARCHAR(128),
    update_time TIMESTAMP,
    del_flag SMALLINT NOT NULL,
    provider VARCHAR(255) NOT NULL,
    base_url VARCHAR(255) NOT NULL,
    api_key VARCHAR(1024) NOT NULL,
    model_name VARCHAR(255) NOT NULL,
    config_name VARCHAR(50),
    sort INT,
    temperature DECIMAL(10,2),
    is_enabled SMALLINT NOT NULL,
    context_window_tokens INT,
    model_type VARCHAR(20) NOT NULL,
    api_path VARCHAR(255),
    is_proxy_enabled SMALLINT NOT NULL,
    proxy_host VARCHAR(255),
    proxy_port INT,
    proxy_username VARCHAR(255),
    proxy_password VARCHAR(1024),
    CONSTRAINT pk_luck_model_config PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_model_type_enabled_del_sort ON luck_model_config (model_type, is_enabled, del_flag, sort);

COMMENT ON TABLE luck_model_config IS '大模型配置表';
COMMENT ON COLUMN luck_model_config.id IS '配置ID';
COMMENT ON COLUMN luck_model_config.create_by IS '创建人';
COMMENT ON COLUMN luck_model_config.create_time IS '创建时间';
COMMENT ON COLUMN luck_model_config.update_by IS '更新人';
COMMENT ON COLUMN luck_model_config.update_time IS '更新时间';
COMMENT ON COLUMN luck_model_config.del_flag IS '删除标志：0-未删除，1-已删除';
COMMENT ON COLUMN luck_model_config.provider IS '厂商标识';
COMMENT ON COLUMN luck_model_config.base_url IS 'API基础地址';
COMMENT ON COLUMN luck_model_config.api_key IS 'API密钥（应用层加密存储）';
COMMENT ON COLUMN luck_model_config.model_name IS '模型名称';
COMMENT ON COLUMN luck_model_config.config_name IS '自定义名称';
COMMENT ON COLUMN luck_model_config.sort IS '排序字段';
COMMENT ON COLUMN luck_model_config.temperature IS '温度参数';
COMMENT ON COLUMN luck_model_config.is_enabled IS '是否启用：0-禁用，1-启用';
COMMENT ON COLUMN luck_model_config.context_window_tokens IS '上下文窗口大小';
COMMENT ON COLUMN luck_model_config.model_type IS '模型类型：CHAT / EMBEDDING / RERANK';
COMMENT ON COLUMN luck_model_config.api_path IS 'API路径';
COMMENT ON COLUMN luck_model_config.is_proxy_enabled IS '是否启用代理：0-禁用，1-启用';
COMMENT ON COLUMN luck_model_config.proxy_host IS '代理主机地址';
COMMENT ON COLUMN luck_model_config.proxy_port IS '代理端口';
COMMENT ON COLUMN luck_model_config.proxy_username IS '代理用户名';
COMMENT ON COLUMN luck_model_config.proxy_password IS '代理密码（应用层加密存储）';

-- -------------------------------------------
-- 数据源配置表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS luck_report_datasource (
    id VARCHAR(32) NOT NULL,
    create_by VARCHAR(128),
    create_time TIMESTAMP,
    update_by VARCHAR(128),
    update_time TIMESTAMP,
    del_flag SMALLINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    host VARCHAR(255) NOT NULL,
    port INT NOT NULL,
    database_name VARCHAR(100),
    username VARCHAR(100),
    password VARCHAR(1024),
    connection_url VARCHAR(500),
    is_enabled SMALLINT NOT NULL,
    test_status VARCHAR(20) NOT NULL,
    description VARCHAR(500),
    initialized_tables TEXT,
    CONSTRAINT pk_luck_report_datasource PRIMARY KEY (id),
    CONSTRAINT uk_ds_name UNIQUE (name)
);

CREATE INDEX IF NOT EXISTS idx_ds_enabled ON luck_report_datasource (is_enabled);
CREATE INDEX IF NOT EXISTS idx_ds_type ON luck_report_datasource (type);

COMMENT ON TABLE luck_report_datasource IS '数据源配置表';
COMMENT ON COLUMN luck_report_datasource.id IS '主键ID';
COMMENT ON COLUMN luck_report_datasource.create_by IS '创建人';
COMMENT ON COLUMN luck_report_datasource.create_time IS '创建时间';
COMMENT ON COLUMN luck_report_datasource.update_by IS '更新人';
COMMENT ON COLUMN luck_report_datasource.update_time IS '更新时间';
COMMENT ON COLUMN luck_report_datasource.del_flag IS '删除标志：0-未删除，1-已删除';
COMMENT ON COLUMN luck_report_datasource.name IS '数据源名称';
COMMENT ON COLUMN luck_report_datasource.type IS '数据源类型：mysql / postgresql / oracle / dameng / sqlserver / hive';
COMMENT ON COLUMN luck_report_datasource.host IS '主机地址';
COMMENT ON COLUMN luck_report_datasource.port IS '端口号';
COMMENT ON COLUMN luck_report_datasource.database_name IS '数据库名';
COMMENT ON COLUMN luck_report_datasource.username IS '用户名';
COMMENT ON COLUMN luck_report_datasource.password IS '密码（应用层加密存储）';
COMMENT ON COLUMN luck_report_datasource.connection_url IS '完整连接URL';
COMMENT ON COLUMN luck_report_datasource.is_enabled IS '是否启用：0-禁用，1-启用';
COMMENT ON COLUMN luck_report_datasource.test_status IS '连接测试状态：success / failed / unknown';
COMMENT ON COLUMN luck_report_datasource.description IS '描述';
COMMENT ON COLUMN luck_report_datasource.initialized_tables IS '已初始化的表名列表（JSON）';

-- -------------------------------------------
-- 逻辑外键配置表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS luck_logical_relation (
    id VARCHAR(32) NOT NULL,
    create_by VARCHAR(128),
    create_time TIMESTAMP,
    update_by VARCHAR(128),
    update_time TIMESTAMP,
    del_flag SMALLINT NOT NULL,
    datasource_id VARCHAR(32) NOT NULL,
    source_table_name VARCHAR(100) NOT NULL,
    source_column_name VARCHAR(100) NOT NULL,
    target_table_name VARCHAR(100) NOT NULL,
    target_column_name VARCHAR(100) NOT NULL,
    relation_type VARCHAR(20),
    description VARCHAR(500),
    CONSTRAINT pk_luck_logical_relation PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_datasource_id ON luck_logical_relation (datasource_id);
CREATE INDEX IF NOT EXISTS idx_source_table ON luck_logical_relation (source_table_name);
CREATE INDEX IF NOT EXISTS idx_target_table ON luck_logical_relation (target_table_name);

COMMENT ON TABLE luck_logical_relation IS '逻辑外键配置表';
COMMENT ON COLUMN luck_logical_relation.id IS '主键ID';
COMMENT ON COLUMN luck_logical_relation.create_by IS '创建人';
COMMENT ON COLUMN luck_logical_relation.create_time IS '创建时间';
COMMENT ON COLUMN luck_logical_relation.update_by IS '更新人';
COMMENT ON COLUMN luck_logical_relation.update_time IS '更新时间';
COMMENT ON COLUMN luck_logical_relation.del_flag IS '删除标志：0-未删除，1-已删除';
COMMENT ON COLUMN luck_logical_relation.datasource_id IS '数据源ID';
COMMENT ON COLUMN luck_logical_relation.source_table_name IS '主表名';
COMMENT ON COLUMN luck_logical_relation.source_column_name IS '主表字段名';
COMMENT ON COLUMN luck_logical_relation.target_table_name IS '关联表名';
COMMENT ON COLUMN luck_logical_relation.target_column_name IS '关联表字段名';
COMMENT ON COLUMN luck_logical_relation.relation_type IS '关系类型：1:1 / 1:N / N:1';
COMMENT ON COLUMN luck_logical_relation.description IS '业务描述';

-- -------------------------------------------
-- 业务知识表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS luck_business_knowledge (
    id VARCHAR(32) NOT NULL,
    create_by VARCHAR(128),
    create_time TIMESTAMP,
    update_by VARCHAR(128),
    update_time TIMESTAMP,
    del_flag SMALLINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    question TEXT,
    content TEXT,
    is_enabled SMALLINT NOT NULL,
    embedding_status VARCHAR(20) NOT NULL,
    error_msg VARCHAR(500),
    source_filename VARCHAR(255),
    file_size BIGINT,
    file_type VARCHAR(100),
    splitter_type VARCHAR(20) NOT NULL,
    model_id VARCHAR(32),
    CONSTRAINT pk_luck_business_knowledge PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_bk_type ON luck_business_knowledge (type);
CREATE INDEX IF NOT EXISTS idx_bk_enabled ON luck_business_knowledge (is_enabled);
CREATE INDEX IF NOT EXISTS idx_bk_embedding_status ON luck_business_knowledge (embedding_status);
CREATE INDEX IF NOT EXISTS idx_bk_del_flag ON luck_business_knowledge (del_flag);

COMMENT ON TABLE luck_business_knowledge IS '业务知识表';
COMMENT ON COLUMN luck_business_knowledge.id IS '主键ID';
COMMENT ON COLUMN luck_business_knowledge.create_by IS '创建人';
COMMENT ON COLUMN luck_business_knowledge.create_time IS '创建时间';
COMMENT ON COLUMN luck_business_knowledge.update_by IS '更新人';
COMMENT ON COLUMN luck_business_knowledge.update_time IS '更新时间';
COMMENT ON COLUMN luck_business_knowledge.del_flag IS '删除标志：0-未删除，1-已删除';
COMMENT ON COLUMN luck_business_knowledge.title IS '知识标题';
COMMENT ON COLUMN luck_business_knowledge.type IS '知识类型：DOCUMENT / QA / FAQ';
COMMENT ON COLUMN luck_business_knowledge.question IS '问题';
COMMENT ON COLUMN luck_business_knowledge.content IS '知识正文';
COMMENT ON COLUMN luck_business_knowledge.is_enabled IS '是否生效：0-不生效，1-生效';
COMMENT ON COLUMN luck_business_knowledge.embedding_status IS '向量化状态：PENDING / PROCESSING / COMPLETED / FAILED';
COMMENT ON COLUMN luck_business_knowledge.error_msg IS '错误信息';
COMMENT ON COLUMN luck_business_knowledge.source_filename IS '原始文件名';
COMMENT ON COLUMN luck_business_knowledge.file_size IS '文件大小';
COMMENT ON COLUMN luck_business_knowledge.file_type IS '文件类型';
COMMENT ON COLUMN luck_business_knowledge.splitter_type IS '分块策略：token / recursive / sentence / paragraph / semantic';
COMMENT ON COLUMN luck_business_knowledge.model_id IS '嵌入模型配置ID';

-- -------------------------------------------
-- 智能体知识表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS luck_agent_knowledge (
    id VARCHAR(32) NOT NULL,
    create_by VARCHAR(128),
    create_time TIMESTAMP,
    update_by VARCHAR(128),
    update_time TIMESTAMP,
    del_flag SMALLINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    question TEXT,
    content TEXT,
    is_enabled SMALLINT NOT NULL,
    embedding_status VARCHAR(20) NOT NULL,
    error_msg VARCHAR(500),
    source_filename VARCHAR(255),
    file_size BIGINT,
    file_type VARCHAR(100),
    splitter_type VARCHAR(20) NOT NULL,
    model_id VARCHAR(32),
    CONSTRAINT pk_luck_agent_knowledge PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_ak_type ON luck_agent_knowledge (type);
CREATE INDEX IF NOT EXISTS idx_ak_enabled ON luck_agent_knowledge (is_enabled);
CREATE INDEX IF NOT EXISTS idx_ak_embedding_status ON luck_agent_knowledge (embedding_status);
CREATE INDEX IF NOT EXISTS idx_ak_del_flag ON luck_agent_knowledge (del_flag);

COMMENT ON TABLE luck_agent_knowledge IS '智能体知识表';
COMMENT ON COLUMN luck_agent_knowledge.id IS '主键ID';
COMMENT ON COLUMN luck_agent_knowledge.create_by IS '创建人';
COMMENT ON COLUMN luck_agent_knowledge.create_time IS '创建时间';
COMMENT ON COLUMN luck_agent_knowledge.update_by IS '更新人';
COMMENT ON COLUMN luck_agent_knowledge.update_time IS '更新时间';
COMMENT ON COLUMN luck_agent_knowledge.del_flag IS '删除标志：0-未删除，1-已删除';
COMMENT ON COLUMN luck_agent_knowledge.title IS '知识标题';
COMMENT ON COLUMN luck_agent_knowledge.type IS '知识类型：DOCUMENT / QA / FAQ';
COMMENT ON COLUMN luck_agent_knowledge.question IS '问题';
COMMENT ON COLUMN luck_agent_knowledge.content IS '知识正文';
COMMENT ON COLUMN luck_agent_knowledge.is_enabled IS '是否生效：0-不生效，1-生效';
COMMENT ON COLUMN luck_agent_knowledge.embedding_status IS '向量化状态：PENDING / PROCESSING / COMPLETED / FAILED';
COMMENT ON COLUMN luck_agent_knowledge.error_msg IS '错误信息';
COMMENT ON COLUMN luck_agent_knowledge.source_filename IS '原始文件名';
COMMENT ON COLUMN luck_agent_knowledge.file_size IS '文件大小';
COMMENT ON COLUMN luck_agent_knowledge.file_type IS '文件类型';
COMMENT ON COLUMN luck_agent_knowledge.splitter_type IS '分块策略：token / recursive / sentence / paragraph / semantic';
COMMENT ON COLUMN luck_agent_knowledge.model_id IS '嵌入模型配置ID';

-- -------------------------------------------
-- 报表文件表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS luck_report_template (
    id VARCHAR(32) NOT NULL,
    create_by VARCHAR(128),
    create_time TIMESTAMP,
    update_by VARCHAR(128),
    update_time TIMESTAMP,
    del_flag SMALLINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    template TEXT,
    CONSTRAINT pk_luck_report_template PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_rt_del_flag ON luck_report_template (del_flag);
CREATE INDEX IF NOT EXISTS idx_rt_update_time ON luck_report_template (update_time);

COMMENT ON TABLE luck_report_template IS '报表文件表';
COMMENT ON COLUMN luck_report_template.id IS '主键ID';
COMMENT ON COLUMN luck_report_template.create_by IS '创建人';
COMMENT ON COLUMN luck_report_template.create_time IS '创建时间';
COMMENT ON COLUMN luck_report_template.update_by IS '更新人';
COMMENT ON COLUMN luck_report_template.update_time IS '更新时间';
COMMENT ON COLUMN luck_report_template.del_flag IS '删除标志：0-未删除，1-已删除';
COMMENT ON COLUMN luck_report_template.title IS '报表标题';
COMMENT ON COLUMN luck_report_template.template IS '报表模板内容';

-- -------------------------------------------
-- 角色与报表绑定关系表（纯关联，无审计字段）
-- file_path 示例：file:test.ureport.xml / db:1 / classpath:foo.ureport.xml / *
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS luck_report_role (
    role_code VARCHAR(128) NOT NULL,
    file_path VARCHAR(512) NOT NULL,
    CONSTRAINT pk_luck_report_role PRIMARY KEY (role_code, file_path)
);

CREATE INDEX IF NOT EXISTS idx_file_path ON luck_report_role (file_path);

COMMENT ON TABLE luck_report_role IS '角色与报表绑定关系表';
COMMENT ON COLUMN luck_report_role.role_code IS '角色编码';
COMMENT ON COLUMN luck_report_role.file_path IS '报表完整路径';

-- -------------------------------------------
-- 公共数据集表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS luck_report_dataset (
    id VARCHAR(32) NOT NULL,
    create_by VARCHAR(128),
    create_time TIMESTAMP,
    update_by VARCHAR(128),
    update_time TIMESTAMP,
    del_flag SMALLINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    datasource_id VARCHAR(32),
    sql_content TEXT,
    json_content TEXT,
    parameters TEXT,
    fields TEXT,
    description VARCHAR(500),
    is_enabled SMALLINT NOT NULL,
    CONSTRAINT pk_luck_report_dataset PRIMARY KEY (id),
    CONSTRAINT uk_pd_name UNIQUE (name)
);

CREATE INDEX IF NOT EXISTS idx_pd_datasource_id ON luck_report_dataset (datasource_id);
CREATE INDEX IF NOT EXISTS idx_pd_enabled ON luck_report_dataset (is_enabled);

COMMENT ON TABLE luck_report_dataset IS '公共数据集表';
COMMENT ON COLUMN luck_report_dataset.id IS '主键ID';
COMMENT ON COLUMN luck_report_dataset.create_by IS '创建人';
COMMENT ON COLUMN luck_report_dataset.create_time IS '创建时间';
COMMENT ON COLUMN luck_report_dataset.update_by IS '更新人';
COMMENT ON COLUMN luck_report_dataset.update_time IS '更新时间';
COMMENT ON COLUMN luck_report_dataset.del_flag IS '删除标志：0-未删除，1-已删除';
COMMENT ON COLUMN luck_report_dataset.name IS '数据集名称';
COMMENT ON COLUMN luck_report_dataset.type IS '类型：sql / json';
COMMENT ON COLUMN luck_report_dataset.datasource_id IS '绑定的公共数据源ID';
COMMENT ON COLUMN luck_report_dataset.sql_content IS 'SQL语句';
COMMENT ON COLUMN luck_report_dataset.json_content IS 'JSON数组内容';
COMMENT ON COLUMN luck_report_dataset.parameters IS 'SQL参数定义（JSON）';
COMMENT ON COLUMN luck_report_dataset.fields IS '字段列表（JSON）';
COMMENT ON COLUMN luck_report_dataset.description IS '描述';
COMMENT ON COLUMN luck_report_dataset.is_enabled IS '是否启用：0-禁用，1-启用';
