-- =============================================
-- Luck Report 数据库初始化脚本
-- 数据库类型：Oracle
-- =============================================

-- -------------------------------------------
-- 会话表
-- -------------------------------------------
CREATE TABLE luck_chat_session (
    id VARCHAR2(36) NOT NULL,
    create_by VARCHAR2(64) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT SYSTIMESTAMP,
    update_by VARCHAR2(64) DEFAULT NULL,
    update_time TIMESTAMP DEFAULT SYSTIMESTAMP,
    del_flag NUMBER(1) DEFAULT 0,
    title VARCHAR2(255) DEFAULT '新对话',
    status VARCHAR2(50) DEFAULT 'active',
    is_pinned NUMBER(1) DEFAULT 0,
    user_id VARCHAR2(64) DEFAULT NULL,
    CONSTRAINT pk_luck_chat_session PRIMARY KEY (id)
);

CREATE INDEX idx_cs_user_id ON luck_chat_session (user_id);
CREATE INDEX idx_cs_status ON luck_chat_session (status);
CREATE INDEX idx_cs_is_pinned ON luck_chat_session (is_pinned);
CREATE INDEX idx_cs_create_time ON luck_chat_session (create_time);

COMMENT ON TABLE luck_chat_session IS '聊天会话表';
COMMENT ON COLUMN luck_chat_session.id IS '会话ID';
COMMENT ON COLUMN luck_chat_session.create_by IS '创建人';
COMMENT ON COLUMN luck_chat_session.create_time IS '创建时间';
COMMENT ON COLUMN luck_chat_session.update_by IS '更新人';
COMMENT ON COLUMN luck_chat_session.update_time IS '更新时间';
COMMENT ON COLUMN luck_chat_session.del_flag IS '删除标志：0-未删除，1-已删除';
COMMENT ON COLUMN luck_chat_session.title IS '会话标题';
COMMENT ON COLUMN luck_chat_session.status IS '状态：1-active，2-archived';
COMMENT ON COLUMN luck_chat_session.is_pinned IS '是否置顶：0-否，1-是';
COMMENT ON COLUMN luck_chat_session.user_id IS '用户ID';

-- -------------------------------------------
-- 消息表
-- -------------------------------------------
CREATE TABLE luck_chat_message (
    id VARCHAR2(32) NOT NULL,
    create_by VARCHAR2(64) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT SYSTIMESTAMP,
    update_by VARCHAR2(64) DEFAULT NULL,
    update_time TIMESTAMP DEFAULT SYSTIMESTAMP,
    del_flag NUMBER(1) DEFAULT 0,
    session_id VARCHAR2(36) NOT NULL,
    role VARCHAR2(20) NOT NULL,
    content CLOB,
    message_type VARCHAR2(50) DEFAULT 'text',
    metadata CLOB DEFAULT NULL,
    CONSTRAINT pk_luck_chat_message PRIMARY KEY (id),
    CONSTRAINT fk_luck_chat_message_session FOREIGN KEY (session_id) REFERENCES luck_chat_session (id) ON DELETE CASCADE
);

CREATE INDEX idx_cm_session_id ON luck_chat_message (session_id);
CREATE INDEX idx_cm_role ON luck_chat_message (role);
CREATE INDEX idx_cm_message_type ON luck_chat_message (message_type);
CREATE INDEX idx_cm_create_time ON luck_chat_message (create_time);

COMMENT ON TABLE luck_chat_message IS '聊天消息表';
COMMENT ON COLUMN luck_chat_message.id IS '消息ID';
COMMENT ON COLUMN luck_chat_message.create_by IS '创建人';
COMMENT ON COLUMN luck_chat_message.create_time IS '创建时间';
COMMENT ON COLUMN luck_chat_message.update_by IS '更新人';
COMMENT ON COLUMN luck_chat_message.update_time IS '更新时间';
COMMENT ON COLUMN luck_chat_message.del_flag IS '删除标志：0-未删除，1-已删除';
COMMENT ON COLUMN luck_chat_message.session_id IS '会话ID';
COMMENT ON COLUMN luck_chat_message.role IS '角色：1-user，2-assistant，3-system，4-tool_result';
COMMENT ON COLUMN luck_chat_message.content IS '消息内容';
COMMENT ON COLUMN luck_chat_message.message_type IS '消息类型：1-text，2-tool_call，3-tool_result，4-error';
COMMENT ON COLUMN luck_chat_message.metadata IS '元数据';

-- -------------------------------------------
-- 模型配置表
-- -------------------------------------------
CREATE TABLE luck_model_config (
    id VARCHAR2(32) NOT NULL,
    create_by VARCHAR2(64) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT NULL,
    update_by VARCHAR2(64) DEFAULT NULL,
    update_time TIMESTAMP DEFAULT NULL,
    del_flag NUMBER(10) DEFAULT 0,
    provider VARCHAR2(255) NOT NULL,
    base_url VARCHAR2(255) NOT NULL,
    api_key VARCHAR2(255) NOT NULL,
    model_name VARCHAR2(255) NOT NULL,
    config_name VARCHAR2(50) DEFAULT NULL,
    sort NUMBER(10) DEFAULT 0,
    temperature NUMBER(10,2) DEFAULT 0.00,
    is_active NUMBER(1) DEFAULT 0,
    context_window_tokens NUMBER(10) DEFAULT 128000,
    model_type VARCHAR2(20) DEFAULT 'CHAT' NOT NULL,
    api_path VARCHAR2(255) DEFAULT NULL,
    proxy_enabled NUMBER(1) DEFAULT 0,
    proxy_host VARCHAR2(255) DEFAULT NULL,
    proxy_port NUMBER(10) DEFAULT NULL,
    proxy_username VARCHAR2(255) DEFAULT NULL,
    proxy_password VARCHAR2(255) DEFAULT NULL,
    CONSTRAINT pk_luck_model_config PRIMARY KEY (id)
);

CREATE INDEX idx_mc_model_type ON luck_model_config (model_type);
CREATE INDEX idx_mc_is_active ON luck_model_config (is_active);
CREATE INDEX idx_mc_provider ON luck_model_config (provider);
CREATE INDEX idx_mc_sort ON luck_model_config (sort);
CREATE INDEX idx_mc_del_flag ON luck_model_config (del_flag);

COMMENT ON TABLE luck_model_config IS '大模型配置表';
COMMENT ON COLUMN luck_model_config.id IS '配置ID';
COMMENT ON COLUMN luck_model_config.create_by IS '创建人';
COMMENT ON COLUMN luck_model_config.create_time IS '创建时间';
COMMENT ON COLUMN luck_model_config.update_by IS '更新人';
COMMENT ON COLUMN luck_model_config.update_time IS '更新时间';
COMMENT ON COLUMN luck_model_config.del_flag IS '删除标志：0-未删除，1-已删除';
COMMENT ON COLUMN luck_model_config.provider IS '厂商标识';
COMMENT ON COLUMN luck_model_config.base_url IS 'API基础地址';
COMMENT ON COLUMN luck_model_config.api_key IS 'API密钥';
COMMENT ON COLUMN luck_model_config.model_name IS '模型名称';
COMMENT ON COLUMN luck_model_config.config_name IS '自定义名称';
COMMENT ON COLUMN luck_model_config.sort IS '排序字段';
COMMENT ON COLUMN luck_model_config.temperature IS '温度参数';
COMMENT ON COLUMN luck_model_config.is_active IS '是否激活：0-未使用，1-当前使用';
COMMENT ON COLUMN luck_model_config.context_window_tokens IS '上下文窗口大小';
COMMENT ON COLUMN luck_model_config.model_type IS '模型类型：1-CHAT，2-EMBEDDING，3-RERANK';
COMMENT ON COLUMN luck_model_config.api_path IS 'API路径';
COMMENT ON COLUMN luck_model_config.proxy_enabled IS '是否启用代理：0-禁用，1-启用';
COMMENT ON COLUMN luck_model_config.proxy_host IS '代理主机地址';
COMMENT ON COLUMN luck_model_config.proxy_port IS '代理端口';
COMMENT ON COLUMN luck_model_config.proxy_username IS '代理用户名';
COMMENT ON COLUMN luck_model_config.proxy_password IS '代理密码';

-- -------------------------------------------
-- 数据源配置表
-- -------------------------------------------
CREATE TABLE luck_report_datasource (
    id VARCHAR2(32) NOT NULL,
    create_by VARCHAR2(64) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT SYSTIMESTAMP,
    update_by VARCHAR2(64) DEFAULT NULL,
    update_time TIMESTAMP DEFAULT SYSTIMESTAMP,
    del_flag NUMBER(1) DEFAULT 0,
    name VARCHAR2(100) NOT NULL,
    type VARCHAR2(50) NOT NULL,
    host VARCHAR2(255) NOT NULL,
    port NUMBER(10) NOT NULL,
    database_name VARCHAR2(100),
    username VARCHAR2(100),
    password VARCHAR2(512),
    connection_url VARCHAR2(500),
    status VARCHAR2(20) DEFAULT 'active',
    test_status VARCHAR2(20) DEFAULT 'unknown',
    description CLOB,
    model_id VARCHAR2(32) DEFAULT NULL,
    initialized_tables CLOB,
    CONSTRAINT pk_luck_report_datasource PRIMARY KEY (id)
);

CREATE INDEX idx_ds_status ON luck_report_datasource (status);
CREATE INDEX idx_ds_type ON luck_report_datasource (type);

COMMENT ON TABLE luck_report_datasource IS '数据源配置表';
COMMENT ON COLUMN luck_report_datasource.id IS '主键ID';
COMMENT ON COLUMN luck_report_datasource.create_by IS '创建人';
COMMENT ON COLUMN luck_report_datasource.create_time IS '创建时间';
COMMENT ON COLUMN luck_report_datasource.update_by IS '更新人';
COMMENT ON COLUMN luck_report_datasource.update_time IS '更新时间';
COMMENT ON COLUMN luck_report_datasource.del_flag IS '删除标志：0-未删除，1-已删除';
COMMENT ON COLUMN luck_report_datasource.name IS '数据源名称';
COMMENT ON COLUMN luck_report_datasource.type IS '数据源类型：1-mysql，2-postgresql，3-oracle，4-dameng，5-sqlserver，6-hive';
COMMENT ON COLUMN luck_report_datasource.host IS '主机地址';
COMMENT ON COLUMN luck_report_datasource.port IS '端口号';
COMMENT ON COLUMN luck_report_datasource.database_name IS '数据库名';
COMMENT ON COLUMN luck_report_datasource.username IS '用户名';
COMMENT ON COLUMN luck_report_datasource.password IS '密码';
COMMENT ON COLUMN luck_report_datasource.connection_url IS '完整连接URL';
COMMENT ON COLUMN luck_report_datasource.status IS '状态：1-active，2-inactive';
COMMENT ON COLUMN luck_report_datasource.test_status IS '连接测试状态：1-success，2-failed，3-unknown';
COMMENT ON COLUMN luck_report_datasource.description IS '描述';
COMMENT ON COLUMN luck_report_datasource.model_id IS '嵌入模型配置ID';
COMMENT ON COLUMN luck_report_datasource.initialized_tables IS '已初始化的表名列表';

-- -------------------------------------------
-- 逻辑外键配置表
-- -------------------------------------------
CREATE TABLE luck_logical_relation (
    id VARCHAR2(32) NOT NULL,
    create_by VARCHAR2(64) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT SYSTIMESTAMP,
    update_by VARCHAR2(64) DEFAULT NULL,
    update_time TIMESTAMP DEFAULT SYSTIMESTAMP,
    del_flag NUMBER(1) DEFAULT 0,
    datasource_id VARCHAR2(32) NOT NULL,
    source_table_name VARCHAR2(100) NOT NULL,
    source_column_name VARCHAR2(100) NOT NULL,
    target_table_name VARCHAR2(100) NOT NULL,
    target_column_name VARCHAR2(100) NOT NULL,
    relation_type VARCHAR2(20),
    description CLOB,
    CONSTRAINT pk_luck_logical_relation PRIMARY KEY (id)
);

CREATE INDEX idx_lr_datasource_id ON luck_logical_relation (datasource_id);
CREATE INDEX idx_lr_source_table ON luck_logical_relation (source_table_name);
CREATE INDEX idx_lr_target_table ON luck_logical_relation (target_table_name);

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
COMMENT ON COLUMN luck_logical_relation.relation_type IS '关系类型：1-1:1，2-1:N，3-N:1';
COMMENT ON COLUMN luck_logical_relation.description IS '业务描述';

-- -------------------------------------------
-- 业务知识表
-- -------------------------------------------
CREATE TABLE luck_business_knowledge (
    id VARCHAR2(32) NOT NULL,
    create_by VARCHAR2(64) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT SYSTIMESTAMP,
    update_by VARCHAR2(64) DEFAULT NULL,
    update_time TIMESTAMP DEFAULT SYSTIMESTAMP,
    del_flag NUMBER(1) DEFAULT 0,
    title VARCHAR2(255) NOT NULL,
    type VARCHAR2(20) NOT NULL,
    question CLOB DEFAULT NULL,
    content CLOB DEFAULT NULL,
    enabled NUMBER(1) DEFAULT 1,
    embedding_status VARCHAR2(20) DEFAULT 'PENDING',
    error_msg VARCHAR2(500) DEFAULT NULL,
    source_filename VARCHAR2(255) DEFAULT NULL,
    file_path VARCHAR2(500) DEFAULT NULL,
    file_size NUMBER(19) DEFAULT NULL,
    file_type VARCHAR2(100) DEFAULT NULL,
    splitter_type VARCHAR2(20) DEFAULT 'token',
    model_id VARCHAR2(32) DEFAULT NULL,
    is_resource_cleaned NUMBER(1) DEFAULT 0,
    CONSTRAINT pk_luck_business_knowledge PRIMARY KEY (id)
);

CREATE INDEX idx_bk_type ON luck_business_knowledge (type);
CREATE INDEX idx_bk_enabled ON luck_business_knowledge (enabled);
CREATE INDEX idx_bk_embedding_status ON luck_business_knowledge (embedding_status);
CREATE INDEX idx_bk_del_flag ON luck_business_knowledge (del_flag);

COMMENT ON TABLE luck_business_knowledge IS '业务知识表';
COMMENT ON COLUMN luck_business_knowledge.id IS '主键ID';
COMMENT ON COLUMN luck_business_knowledge.create_by IS '创建人';
COMMENT ON COLUMN luck_business_knowledge.create_time IS '创建时间';
COMMENT ON COLUMN luck_business_knowledge.update_by IS '更新人';
COMMENT ON COLUMN luck_business_knowledge.update_time IS '更新时间';
COMMENT ON COLUMN luck_business_knowledge.del_flag IS '删除标志：0-未删除，1-已删除';
COMMENT ON COLUMN luck_business_knowledge.title IS '知识标题';
COMMENT ON COLUMN luck_business_knowledge.type IS '知识类型：1-DOCUMENT，2-QA，3-FAQ';
COMMENT ON COLUMN luck_business_knowledge.question IS '问题';
COMMENT ON COLUMN luck_business_knowledge.content IS '知识正文';
COMMENT ON COLUMN luck_business_knowledge.enabled IS '是否生效：0-不生效，1-生效';
COMMENT ON COLUMN luck_business_knowledge.embedding_status IS '向量化状态：1-PENDING，2-PROCESSING，3-COMPLETED，4-FAILED';
COMMENT ON COLUMN luck_business_knowledge.error_msg IS '错误信息';
COMMENT ON COLUMN luck_business_knowledge.source_filename IS '原始文件名';
COMMENT ON COLUMN luck_business_knowledge.file_path IS '文件存储路径';
COMMENT ON COLUMN luck_business_knowledge.file_size IS '文件大小';
COMMENT ON COLUMN luck_business_knowledge.file_type IS '文件类型';
COMMENT ON COLUMN luck_business_knowledge.splitter_type IS '分块策略类型：1-token，2-recursive，3-sentence，4-paragraph，5-semantic';
COMMENT ON COLUMN luck_business_knowledge.model_id IS '嵌入模型配置ID';
COMMENT ON COLUMN luck_business_knowledge.is_resource_cleaned IS '物理资源是否已清理：0-未清理，1-已清理';

-- -------------------------------------------
-- 智能体知识表
-- -------------------------------------------
CREATE TABLE luck_agent_knowledge (
    id VARCHAR2(32) NOT NULL,
    create_by VARCHAR2(64) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT SYSTIMESTAMP,
    update_by VARCHAR2(64) DEFAULT NULL,
    update_time TIMESTAMP DEFAULT SYSTIMESTAMP,
    del_flag NUMBER(1) DEFAULT 0,
    title VARCHAR2(255) NOT NULL,
    type VARCHAR2(20) NOT NULL,
    question CLOB DEFAULT NULL,
    content CLOB DEFAULT NULL,
    enabled NUMBER(1) DEFAULT 1,
    embedding_status VARCHAR2(20) DEFAULT 'PENDING',
    error_msg VARCHAR2(500) DEFAULT NULL,
    source_filename VARCHAR2(255) DEFAULT NULL,
    file_path VARCHAR2(500) DEFAULT NULL,
    file_size NUMBER(19) DEFAULT NULL,
    file_type VARCHAR2(100) DEFAULT NULL,
    splitter_type VARCHAR2(20) DEFAULT 'token',
    model_id VARCHAR2(32) DEFAULT NULL,
    is_resource_cleaned NUMBER(1) DEFAULT 0,
    CONSTRAINT pk_luck_agent_knowledge PRIMARY KEY (id)
);

CREATE INDEX idx_ak_type ON luck_agent_knowledge (type);
CREATE INDEX idx_ak_enabled ON luck_agent_knowledge (enabled);
CREATE INDEX idx_ak_embedding_status ON luck_agent_knowledge (embedding_status);
CREATE INDEX idx_ak_del_flag ON luck_agent_knowledge (del_flag);

COMMENT ON TABLE luck_agent_knowledge IS '智能体知识表';
COMMENT ON COLUMN luck_agent_knowledge.id IS '主键ID';
COMMENT ON COLUMN luck_agent_knowledge.create_by IS '创建人';
COMMENT ON COLUMN luck_agent_knowledge.create_time IS '创建时间';
COMMENT ON COLUMN luck_agent_knowledge.update_by IS '更新人';
COMMENT ON COLUMN luck_agent_knowledge.update_time IS '更新时间';
COMMENT ON COLUMN luck_agent_knowledge.del_flag IS '删除标志：0-未删除，1-已删除';
COMMENT ON COLUMN luck_agent_knowledge.title IS '知识标题';
COMMENT ON COLUMN luck_agent_knowledge.type IS '知识类型：1-DOCUMENT，2-QA，3-FAQ';
COMMENT ON COLUMN luck_agent_knowledge.question IS '问题';
COMMENT ON COLUMN luck_agent_knowledge.content IS '知识正文';
COMMENT ON COLUMN luck_agent_knowledge.enabled IS '是否生效：0-不生效，1-生效';
COMMENT ON COLUMN luck_agent_knowledge.embedding_status IS '向量化状态：1-PENDING，2-PROCESSING，3-COMPLETED，4-FAILED';
COMMENT ON COLUMN luck_agent_knowledge.error_msg IS '错误信息';
COMMENT ON COLUMN luck_agent_knowledge.source_filename IS '原始文件名';
COMMENT ON COLUMN luck_agent_knowledge.file_path IS '文件存储路径';
COMMENT ON COLUMN luck_agent_knowledge.file_size IS '文件大小';
COMMENT ON COLUMN luck_agent_knowledge.file_type IS '文件类型';
COMMENT ON COLUMN luck_agent_knowledge.splitter_type IS '分块策略类型：1-token，2-recursive，3-sentence，4-paragraph，5-semantic';
COMMENT ON COLUMN luck_agent_knowledge.model_id IS '嵌入模型配置ID';
COMMENT ON COLUMN luck_agent_knowledge.is_resource_cleaned IS '物理资源是否已清理：0-未清理，1-已清理';

-- -------------------------------------------
-- 报表文件表
-- -------------------------------------------
CREATE TABLE luck_report_template (
    id VARCHAR2(32) NOT NULL,
    create_by VARCHAR2(64) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT SYSTIMESTAMP,
    update_by VARCHAR2(64) DEFAULT NULL,
    update_time TIMESTAMP DEFAULT SYSTIMESTAMP,
    del_flag NUMBER(1) DEFAULT 0,
    title VARCHAR2(255) NOT NULL,
    template CLOB DEFAULT NULL,
    CONSTRAINT pk_luck_report_template PRIMARY KEY (id)
);

CREATE INDEX idx_rt_del_flag ON luck_report_template (del_flag);
CREATE INDEX idx_rt_title ON luck_report_template (title);
CREATE INDEX idx_rt_create_time ON luck_report_template (create_time);

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
-- 角色与报表绑定关系表
-- file_path 示例：file:test.ureport.xml / db:1 / classpath:foo.ureport.xml / *
-- -------------------------------------------
CREATE TABLE luck_report_role (
    role_code VARCHAR2(128) NOT NULL,
    file_path VARCHAR2(512) NOT NULL,
    CONSTRAINT pk_luck_report_role PRIMARY KEY (role_code, file_path)
);

CREATE INDEX idx_lrr_file_path ON luck_report_role (file_path);

COMMENT ON TABLE luck_report_role IS '角色与报表绑定关系表';
COMMENT ON COLUMN luck_report_role.role_code IS '角色编码';
COMMENT ON COLUMN luck_report_role.file_path IS '报表完整路径';

-- -------------------------------------------
-- 公共数据集表
-- -------------------------------------------
CREATE TABLE luck_report_dataset (
    id VARCHAR2(32) NOT NULL,
    create_by VARCHAR2(64) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT SYSTIMESTAMP,
    update_by VARCHAR2(64) DEFAULT NULL,
    update_time TIMESTAMP DEFAULT SYSTIMESTAMP,
    del_flag NUMBER(1) DEFAULT 0,
    name VARCHAR2(100) NOT NULL,
    type VARCHAR2(20) NOT NULL,
    datasource_id VARCHAR2(32) DEFAULT NULL,
    sql_content CLOB,
    json_content CLOB,
    parameters CLOB,
    fields CLOB,
    description VARCHAR2(500),
    status VARCHAR2(20) DEFAULT 'active' NOT NULL,
    CONSTRAINT pk_luck_report_dataset PRIMARY KEY (id),
    CONSTRAINT uk_pd_name UNIQUE (name)
);

CREATE INDEX idx_pd_datasource_id ON luck_report_dataset (datasource_id);
CREATE INDEX idx_pd_status ON luck_report_dataset (status);

COMMENT ON TABLE luck_report_dataset IS '公共数据集表';
COMMENT ON COLUMN luck_report_dataset.id IS '主键ID';
COMMENT ON COLUMN luck_report_dataset.create_by IS '创建人';
COMMENT ON COLUMN luck_report_dataset.create_time IS '创建时间';
COMMENT ON COLUMN luck_report_dataset.update_by IS '更新人';
COMMENT ON COLUMN luck_report_dataset.update_time IS '更新时间';
COMMENT ON COLUMN luck_report_dataset.del_flag IS '删除标志：0-未删除，1-已删除';
COMMENT ON COLUMN luck_report_dataset.name IS '数据集名称';
COMMENT ON COLUMN luck_report_dataset.type IS '类型：1-sql，2-json';
COMMENT ON COLUMN luck_report_dataset.datasource_id IS '绑定的公共数据源ID';
COMMENT ON COLUMN luck_report_dataset.sql_content IS 'SQL语句';
COMMENT ON COLUMN luck_report_dataset.json_content IS 'JSON数组内容';
COMMENT ON COLUMN luck_report_dataset.parameters IS 'SQL参数定义';
COMMENT ON COLUMN luck_report_dataset.fields IS '字段列表';
COMMENT ON COLUMN luck_report_dataset.description IS '描述';
COMMENT ON COLUMN luck_report_dataset.status IS '状态：1-active，2-inactive';
