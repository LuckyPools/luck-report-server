package com.luck.report.web.modules.knowledge.handler.splitter;

import com.luck.report.web.config.KnowledgeChunkProperties;
import com.luck.report.web.modules.chat.service.impl.EmbeddingService;
import com.luck.report.web.modules.knowledge.domain.enums.SplitterType;
import com.luck.report.web.modules.knowledge.handler.parser.TokenTextSplitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 文本分块器工厂
 * 根据 splitterType 选择对应的分块策略
 *
 * 支持的策略：
 * - token: 按固定字符数切分，适用于代码、日志等
 * - recursive: Dify 风格递归分隔符切分
 * - structure: FastGPT 结构感知（标题/代码/表）
 * - table: FastGPT Markdown 表行窗
 * - sentence / paragraph / semantic: 既有策略
 *
 * chunkSize / chunkOverlap 来自 {@code luck-report.vector} 配置（字符级）。
 *
 * @author luck
 */
@Component
public class TextSplitterFactory {

    private static final Logger log = LoggerFactory.getLogger(TextSplitterFactory.class);

    private final EmbeddingService embeddingService;
    private final KnowledgeChunkProperties chunkProperties;

    public TextSplitterFactory(EmbeddingService embeddingService, KnowledgeChunkProperties chunkProperties) {
        this.embeddingService = embeddingService;
        this.chunkProperties = chunkProperties;
    }

    /**
     * 获取分块器
     *
     * @param splitterType 分块策略类型（blank → recursive；非法 → 业务异常）
     * @param modelId      嵌入模型ID（仅 semantic 类型需要）
     * @return 对应的分块器实例
     */
    public TextSplitter getSplitter(String splitterType, String modelId) {
        SplitterType type = SplitterType.fromValue(splitterType);
        int chunkSize = chunkProperties.getChunkSize();
        int overlap = chunkProperties.getChunkOverlap();

        log.debug("创建分块器 type={}, chunkSize={}, overlap={}", type.getValue(), chunkSize, overlap);

        switch (type) {
            case TOKEN:
                return new TokenTextSplitter(chunkSize, overlap);
            case RECURSIVE:
                return new RecursiveTextSplitter(chunkSize, overlap);
            case STRUCTURE:
                return new StructureTextSplitter(chunkSize, overlap);
            case TABLE:
                return new TableTextSplitter(chunkSize);
            case SENTENCE:
                return new SentenceTextSplitter(chunkSize);
            case PARAGRAPH:
                return new ParagraphTextSplitter(chunkSize, overlap);
            case SEMANTIC:
                return new SemanticTextSplitter(embeddingService, modelId, chunkSize);
            default:
                return new RecursiveTextSplitter(chunkSize, overlap);
        }
    }
}
