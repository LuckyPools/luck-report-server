-- content → content_tsv + GIN；并清理旧触发器

ALTER TABLE luck_report_vector.luck_vector_document
    ADD COLUMN IF NOT EXISTS content_tsv tsvector;

UPDATE luck_report_vector.luck_vector_document
SET content_tsv = to_tsvector('simple', coalesce(content, ''))
WHERE content_tsv IS NULL;

CREATE INDEX IF NOT EXISTS idx_luck_vector_document_content_tsv
    ON luck_report_vector.luck_vector_document
    USING GIN (content_tsv);
