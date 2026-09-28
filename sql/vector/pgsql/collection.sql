-- =============================================
-- 向量文档表（PostgreSQL + pgvector）
-- 前置条件：数据库中已安装 pgvector 插件
-- 注意：扩展和表都安装在 luck_report_vector schema 下
-- =============================================

-- 创建 schema
CREATE SCHEMA IF NOT EXISTS luck_report_vector;

-- 安装 vector 扩展到 luck_report_vector schema
CREATE EXTENSION IF NOT EXISTS vector SCHEMA luck_report_vector;

-- 验证扩展安装
SELECT extname, extnamespace::regnamespace AS schema, extversion FROM pg_extension WHERE extname = 'vector';

CREATE TABLE IF NOT EXISTS luck_report_vector.luck_vector_document (
    id          VARCHAR(64)    NOT NULL,
    vector      luck_report_vector.vector(1024) NOT NULL,
    content     TEXT           DEFAULT NULL,
    content_tsv tsvector       DEFAULT NULL,
    metadata    JSONB          NOT NULL DEFAULT '{}',
    vector_type VARCHAR(32)    NOT NULL DEFAULT 'UNKNOWN',
    created_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
    );

-- 给表和字段加注释（PostgreSQL 专用写法）
COMMENT ON TABLE  luck_report_vector.luck_vector_document IS '向量文档表（含 content 字段，存储分块文本）';
COMMENT ON COLUMN luck_report_vector.luck_vector_document.id IS '文档唯一ID';
COMMENT ON COLUMN luck_report_vector.luck_vector_document.vector IS '向量数据，text-embedding-v3 输出 1024 维';
COMMENT ON COLUMN luck_report_vector.luck_vector_document.content IS '文档内容（分块文本），用于检索结果直接返回';
COMMENT ON COLUMN luck_report_vector.luck_vector_document.content_tsv IS '全文倒排（tsvector）';
COMMENT ON COLUMN luck_report_vector.luck_vector_document.metadata IS '元数据，JSON格式，支持 jsonb 操作符过滤';
COMMENT ON COLUMN luck_report_vector.luck_vector_document.vector_type IS '知识类型: COMPONENT/TEMPLATE/DATASOURCE/BUSINESS';
COMMENT ON COLUMN luck_report_vector.luck_vector_document.created_at IS '创建时间';

-- 普通索引
CREATE INDEX IF NOT EXISTS idx_luck_vector_document_type
    ON luck_report_vector.luck_vector_document (vector_type);

-- 全文倒排 GIN
CREATE INDEX IF NOT EXISTS idx_luck_vector_document_content_tsv
    ON luck_report_vector.luck_vector_document
    USING GIN (content_tsv);

-- 向量相似度索引
CREATE INDEX IF NOT EXISTS idx_luck_vector_document_vector
    ON luck_report_vector.luck_vector_document
    USING hnsw (vector vector_cosine_ops);

-- =============================================
-- 增量迁移：旧表升级（全新部署可跳过，上面建表已包含）
-- =============================================
ALTER TABLE luck_report_vector.luck_vector_document
    ADD COLUMN IF NOT EXISTS content TEXT DEFAULT NULL;
COMMENT ON COLUMN luck_report_vector.luck_vector_document.content IS '文档内容（分块文本），用于检索结果直接返回';

ALTER TABLE luck_report_vector.luck_vector_document
    ADD COLUMN IF NOT EXISTS content_tsv tsvector;
COMMENT ON COLUMN luck_report_vector.luck_vector_document.content_tsv IS '全文倒排（tsvector）';

-- 存量数据回填全文倒排（仅 content_tsv 为空的行）
UPDATE luck_report_vector.luck_vector_document
SET content_tsv = to_tsvector('simple', coalesce(content, ''))
WHERE content_tsv IS NULL;

-- GIN 索引见上文 CREATE INDEX IF NOT EXISTS idx_luck_vector_document_content_tsv
